package ru.gb.storage.client;

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
import ru.gb.storage.commons.message.AuthMessage;
import ru.gb.storage.commons.message.DataMessage;
import ru.gb.storage.commons.message.Message;
import ru.gb.storage.commons.message.TextMessage;

import java.time.LocalDateTime;
import java.util.Date;


public class Client  {
    public static void main(String[] args) {
        new Client().start();
    }

    public void start()  {

        final NioEventLoopGroup group = new NioEventLoopGroup(1);
        try{
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg)  {
                                            System.out.println("receive msg" + msg);

                                        }
                                    }
                            );
                        }
                    });

            System.out.println("Client started");
            Channel channel = bootstrap.connect("localhost", 8189).sync().channel();

            while (channel.isActive()){
                TextMessage textMessage = new TextMessage();
                textMessage.setText(String.format("[%s] %s", LocalDateTime.now(), Thread.currentThread().getName()));
                System.out.println("Try to send message: " + textMessage);
                channel.writeAndFlush(textMessage);

                DataMessage dateMessage = new DataMessage();
                dateMessage. setData(new Date());
                channel.write(dateMessage);
                System.out.println("Try to send message: " + dateMessage);
                channel.flush();
                Thread.sleep(3000);

                AuthMessage authMessage = new AuthMessage();
                authMessage.setLogin("roman");
                authMessage.setPassword(123);

                System.out.println(authMessage);
                channel.writeAndFlush(authMessage);
            }
            channel.closeFuture().sync();
        }catch (InterruptedException ex){
            ex.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
}
