package ru.gb.storage.client.mynetty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
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

import java.util.Scanner;


public final class SimpleNettyClient {

    private static SimpleNettyClient instance = null;
    private NioEventLoopGroup general;
    private Bootstrap server;
    private Scanner scanner;

    private SimpleNettyClient() {
        this.general = new NioEventLoopGroup(1);
        this.scanner = new Scanner(System.in);

        try {
             this.server = new Bootstrap()
                    .group(general)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Msg>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Msg message) {
                                            System.out.println("Сообщение: " + message);
                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Клиент стартовал");
            System.out.println("Введите логин: ");
            String login = scanner.nextLine();
            System.out.println("Введите Пароль: ");
            String password = scanner.nextLine();
            Channel channel = server.connect("localhost", 22333).sync().channel();
            AuthMsg authMsg = new AuthMsg(login, password);
            channel.write(authMsg);
            channel.flush();
            TextMsg textMsg = new TextMsg();
            textMsg.setText("Конвертация");
            channel.write(textMsg);
            channel.flush();
            channel.closeFuture().sync();


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            general.shutdownGracefully();
        }

    }

    public static void getInstance() {
        if (instance == null) {
            instance = new SimpleNettyClient();
        }
    }

    public static void main(String[] args) {
        SimpleNettyClient.getInstance();
    }

}
