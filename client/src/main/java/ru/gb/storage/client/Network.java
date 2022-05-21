package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.handler.JsonDecoder;
import ru.gb.storage.handler.JsonEncoder;
import ru.gb.storage.message.Message;

public class Network {
    private final int PORT = 9000;
    private final String HOST = "localhost";
    public SocketChannel sChannel;
    private String nameFileIn;
    private String nameFileOut;
    private Callback onSendMSG;

    public Network(Callback onSendMSG) {
        this.onSendMSG = onSendMSG;
        Thread t = new Thread(()-> {
            final NioEventLoopGroup group = new NioEventLoopGroup(1);
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                sChannel = ch;
                                ch.pipeline().addLast(
                                        new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                        new LengthFieldPrepender(3),
                                        new JsonDecoder(),
                                        new JsonEncoder(),
                                        (if (onSendMSG !=null) {
                                            new ClientHandler(onSendMSG)
                                        }else{
                                        new SimpleChannelInboundHandler<Message>() {
                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
                                                System.out.println(msg);
                                        }}})
                                );
                            }
                        });
                System.out.println("Client is start");
                Channel channel;
                channel = (Channel) bootstrap.connect(HOST, PORT).sync().channel();
                ((io.netty.channel.Channel) channel).closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
