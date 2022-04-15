package ru.gb.storage.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class Server {

    private final int port;
    StringBuilder message = new StringBuilder();  // стрингбилдер для склеивания нашего сообшения для отправки клиенту

    public static void main(String[] args) throws InterruptedException {
        new Server(9000).start();
    }

    public Server(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("Channel registered");
                                        }

                                        @Override
                                        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("Channel unregistered");
                                        }

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("Channel active");
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                            System.out.println("Channel inactive");
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            System.out.print("Received message: ");
                                            final ByteBuf m = (ByteBuf) msg;

                                            for (int i = m.readerIndex(); i < m.writerIndex(); i++) {
                                                System.out.print((char) m.getByte(i)); //читаем данные из буфера так, чтобы не сдвинуть индексы
                                                message.append((char) m.getByte(i));
                                            }
                                            m.clear();
                                            System.out.flush();
                                            System.out.println();

                                            char[] chars = new char[message.length()];  // приводим сообщение в массиву чаров чтобы в дальнейшем отследить
                                                                                        // символы переноса строки
                                            message.toString().getChars(0, message.length(), chars, 0);

                                            for (int i = 0; i < chars.length - 1; i++) {
                                                if ((byte) chars[i] == 92 && (byte) chars[i + 1] == 110) {  // здесь взяты байтовые значения символов
                                                                                                            // "/" - 92 и "n" - 110
                                                    System.out.println("Find symbol line break");
                                                    message.delete(message.length() - 2, message.length());  // обрезаем наш месседж чтобы он не содержал сиволов /n
                                                    ctx.writeAndFlush(((ByteBuf) msg).writeBytes(message.toString().getBytes(StandardCharsets.UTF_8)));
                                                    message.delete(0, message.length());  // удаляем посностью наше сообщение которое копилось для отправки,
                                                                                            // чтобы можно было формировать новое сообщение.
                                                }
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws
                                                Exception {
                                            System.out.println("caused Exception");
                                            cause.printStackTrace();
                                            ctx.close();
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = server.bind(port).sync().channel();
            System.out.println("Server started");
            channel.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}









