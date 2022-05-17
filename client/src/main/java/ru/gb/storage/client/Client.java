package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Client  extends Application {
    private final int PORT = 9000;
    private final String HOST = "localhost";
    private String nameFileIn="E:\\1";         // путь к файлу
    private String nameFileOut="E:\\2";         // путь к файлу

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start (Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 620);

//        Parent root =  fxmlLoader.load();
//        Scene scene = new Scene(root);
        stage.setTitle("MyDropRockerok");
        stage.setScene(scene);
        stage.setResizable(true);

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
//            Channel channel = (Channel) bootstrap.connect(HOST,PORT).sync().channel();
//            ((io.netty.channel.Channel) channel).closeFuture().sync();
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }finally {
//            group.shutdownGracefully();
//        }
    }
}
