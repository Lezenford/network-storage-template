package ru.gb.storage.client;

import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
//import javafx.application.Application;
//import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import message.FileContentMessage;
import message.FileRequestMessage;
import message.Message;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Client  extends Application {
    private final int PORT = 9000;
    private final String HOST = "localhost";
    private String nameFileIn="E:\\1";         // путь к файлу
    private String nameFileOut="E:\\2";         // путь к файлу
    public static void main(String[] args) {
        Application.launch(args);
//        new Client().start();
    }

    public void start (@org.jetbrains.annotations.NotNull Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("/view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 440, 420);
        stage.setTitle("MyDropRockerok");
        stage.setScene(scene);
        stage.show();
//        final NioEventLoopGroup group = new NioEventLoopGroup(1);
//        try {
//            Bootstrap bootstrap = new Bootstrap()
//                    .group(group)
//                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch){
//                            ch.pipeline().addLast(
//                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
//                                    new LengthFieldPrepender(3),
//                                    new JsonDecoder(),
//                                    new JsonEncoder(),
//                                    new SimpleChannelInboundHandler<Message>() {
//                                        @Override
//                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                            final FileRequestMessage frMessage = new FileRequestMessage();
//                                            frMessage.setPath(nameFileIn);
//                                            ctx.writeAndFlush(frMessage);
//                                        }
//                                        @Override
//                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg){
//                                            System.out.println("File transfer Start "+ msg);
//                                            if(msg instanceof FileContentMessage) {
//                                                FileContentMessage fcMessage = (FileContentMessage) msg;
//                                                try(final RandomAccessFile raf= new RandomAccessFile(nameFileOut,"rw")){
//                                                    raf.seek(fcMessage.getStartPosition());
//                                                    raf.write(fcMessage.getContent());
//                                                    if (fcMessage.isLast()){
//                                                         ctx.close();
//                                                        System.out.println("File transfer Finish");
//                                                    }
//                                                }catch (IOException e){
//                                                    throw new RuntimeException(e);
//                                                }
//                                            }
//                                        }
//                                    }
//                            );
//                        }
//                    });
//            System.out.println("Client is start");
//            Channel channel = bootstrap.connect(HOST,PORT).sync().channel();
//            channel.closeFuture().sync();
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }finally {
//            group.shutdownGracefully();
//        }
    }
}
