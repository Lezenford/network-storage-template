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
import ru.gb.storage.message.AuthMessage;
import ru.gb.storage.message.FileRequestMessage;
import ru.gb.storage.message.Message;

import java.io.File;
import java.nio.file.Path;

public class Network {
    private final int PORT = 9000;
    private final String HOST = "localhost";
    public static SocketChannel sChannel;
    protected Controller controller;

    private static String pathRight = "C:\\Clients\\";
    private static String pathLeft = "C:\\Clients\\LocalPC\\";
    private static String pathFile;

    public static String getPathFile() { return pathFile;  }
    public static void setPathFile(String pathFile) { Network.pathFile = pathFile; }

    public Network(Controller controller) {
        this.controller = controller;
    }

    private static boolean authTrue = false;
    public static boolean isAuthTrue() { return authTrue; }
    public void setAuthTrue(boolean authTrue) {
        this.authTrue = authTrue;
    }

    public static String getPathRight() {
        return pathRight;
    }
    public static void setPathRight(String pathRight) {
        Network.pathRight = pathRight;
    }
    public static String getPathLeft() { return pathLeft; }
    public static void setPathLeft(String pathLeft) { Network.pathLeft = pathLeft; }

    public static SocketChannel getsChannel() { return sChannel; }

    public void start() {
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
                         ch.pipeline().addLast(
                                 new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                 new LengthFieldPrepender(3),
                                 new JsonDecoder(),
                                 new JsonEncoder(),
                                 new ClientHandler()
                         );
                         sChannel = ch;
                         }
                    }
                    );
                ClientHandler.setNetwork(this);
                System.out.println("Client is start");
                Channel channel;
                channel = (Channel) bootstrap.connect(HOST, PORT).sync().channel();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                group.shutdownGracefully();
                sChannel.close();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void sendReqAuth(Message msg) {
        setAuthTrue(true);
        createRightDir(pathRight);
        controller.updateListPanel(pathRight,pathLeft);
    }

    private void createRightDir(String pathRight) {
        String myClient = controller.getNick();
        System.out.println(myClient);
        String pathName=pathRight;
        pathName+="\\"+myClient;
        setPathRight(pathName);
        File file = new File(pathName);
        System.out.println(pathRight);
        file.mkdirs();
    }

    public void auth(AuthMessage msg) {
        sChannel.writeAndFlush(msg);
    }

    public void myCopyFile(Path srcPath) {
            FileRequestMessage frMessage = new FileRequestMessage();
            System.out.println("file transfer to "+ String.valueOf(srcPath));
            System.out.println("Global Left / Right "+getPathLeft() +" / "+ getPathRight());
            frMessage.setPath(String.valueOf(srcPath));
            getsChannel().writeAndFlush(frMessage);
    }
}
