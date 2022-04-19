package ru.gb.storage.server.mynetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.AuthMsg;
import ru.gb.storage.commons.message.Msg;
import ru.gb.storage.commons.message.TextMsg;

public final class SimpleNettyServer {

    private static SimpleNettyServer instance = null;
    private NioEventLoopGroup general;
    private NioEventLoopGroup worker;
    private ServerBootstrap server;

    private SimpleNettyServer() {
        this.general = new NioEventLoopGroup(1);
        this.worker = new NioEventLoopGroup();

        try {
            this.server = new ServerBootstrap();

            this.server.group(general, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {

                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(

                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Msg>() {

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Msg msg) throws Exception {
                                            if (msg instanceof AuthMsg) {

                                                AuthMsg authMsg = (AuthMsg) msg;
                                                System.out.println("Аутентификация пользователя");
                                                if (authMsg.getLogin() != null) {
                                                    System.out.println("Пользователь с логином " + authMsg.getLogin() + " найден");
                                                } else {
                                                    System.out.println("Пользователя с данным логином нет");
                                                }
                                                TextMsg textMsg = new TextMsg();
                                                textMsg.setText("Пользователь с ником : " + authMsg.getLogin() + " прошел аутентификацию");
                                                channelHandlerContext.writeAndFlush(textMsg);
                                            }

                                        }
                                    }

                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = server.bind(22333).sync();

            System.out.println("Сервер стартовал");
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }  finally {
            general.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

    public static void getInstance() {
        if (instance == null) {
            instance = new SimpleNettyServer();
        }
    }

    public static void main(String[] args) {
        SimpleNettyServer.getInstance();
    }

}
