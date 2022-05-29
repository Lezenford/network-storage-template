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
    protected PanelController panelController;

    private static String pathSrv = "C:\\Clients\\";
    private static String pathCli = "C:";

    public Network(Controller controller) {
        this.controller = controller;
    }
    public Network(PanelController panelController) {
        this.panelController = panelController;
    }

    public static String getPathSrv() {
        return pathSrv;
    }
    public static void setPathSrv(String pathSrv) {
        Network.pathSrv = pathSrv;
    }
    public static String getPathCli() { return pathCli; }
    public static void setPathCli(String pathCli) { Network.pathCli = pathCli; }

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
                                         sChannel = ch;
                                             ch.pipeline().addLast(
                                                     new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                                     new LengthFieldPrepender(3),
                                                     new JsonDecoder(),
                                                     new JsonEncoder(),
                                                     new ClientHandler()
                                             );
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
        createSrvDir(pathSrv);
        controller.updateListPanel(pathSrv,pathCli);
    }

    private void createSrvDir(String pathSrv) {
        String myClient = controller.getNick();
        String pathName=pathSrv;
        pathName+="\\"+myClient;
        setPathSrv(pathName);
        File file = new File(pathName);
        System.out.println(pathSrv);
        file.mkdirs();
    }

    public void auth(AuthMessage msg) {
        sChannel.writeAndFlush(msg);
    }

    public void myCopyFile(Path srcPath, Path dstPath) {
        if (controller.leftPanContr.getSelectedFileName() != null){
            Network.setPathCli(String.valueOf(srcPath));
            Network.setPathSrv(String.valueOf(dstPath));
            FileRequestMessage message = new FileRequestMessage();
            message.setPath(String.valueOf(srcPath));
            getsChannel().writeAndFlush(message);
        }
        if (controller.leftPanContr.getSelectedFileName() != null){
            Network.setPathCli(String.valueOf(dstPath));
            Network.setPathSrv(String.valueOf(srcPath));
            FileRequestMessage message = new FileRequestMessage();
            message.setPath(String.valueOf(dstPath));
            getsChannel().writeAndFlush(message);
        }
    }
}
