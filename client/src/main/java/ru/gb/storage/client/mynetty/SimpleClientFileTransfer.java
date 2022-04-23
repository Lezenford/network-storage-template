package ru.gb.storage.client.mynetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.FileContentMessage;
import ru.gb.storage.commons.message.FileRequestMessage;
import ru.gb.storage.commons.message.Msg;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public final class SimpleClientFileTransfer {

    private static SimpleClientFileTransfer instance = null;

    private SimpleClientFileTransfer() {
        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap server = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Msg>() {

                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            final FileRequestMessage frm = new FileRequestMessage();
                                            frm.setPath("D:\\password");
                                            ctx.writeAndFlush(frm);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Msg msg) throws Exception {
                                            if (msg instanceof FileContentMessage) {
                                                FileContentMessage fileContentMessage =  (FileContentMessage) msg;
                                                try (RandomAccessFile randomAccessFile = new RandomAccessFile("D:\\password-test", "rw")) {
                                                    randomAccessFile.seek(fileContentMessage.getStartPosition());
                                                    randomAccessFile.write(fileContentMessage.getContent());
                                                    if (fileContentMessage.isLastPosition()) {
                                                        channelHandlerContext.close();
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                            );
                        }
                    });
            System.out.println("Client started");
            Channel channel = server.connect("localhost", 22333).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void getInstance() {
        if (instance == null) {
            instance = new SimpleClientFileTransfer();
        }
    }

    public static void main(String[] args) {
         SimpleClientFileTransfer.getInstance();
    }
}
