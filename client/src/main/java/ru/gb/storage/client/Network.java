package ru.gb.storage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.handler.JsonDecoder;
import ru.gb.storage.handler.JsonEncoder;
import ru.gb.storage.message.AuthMessage;
import ru.gb.storage.message.Message;
import ru.gb.storage.message.TextMessage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Network {
    private final int PORT = 9000;
    private final String HOST = "localhost";
    public SocketChannel sChannel;
    public String corPathDwnd;
    protected Controller controller;

    private String pathCli = "C:\\Clients\\";
    private String pathSrv = "C:";
    private String nameFileIn;
    private String nameFileOut;

    public Network(Controller controller) {
        this.controller = controller;
    }

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
            }
        });
        t.setDaemon(true);
        t.start();
    }


    public void sendReqAuth(Message msg) {
        if (msg instanceof TextMessage) {
            TextMessage tm = (TextMessage) msg;
            if (tm.getText().equals("success")) {
                controller.closeAuth();
                createSrvDir(pathSrv);
                sendDirOnCli(pathCli);
                sendCliDir(pathSrv);
            } else {
                controller.errorAuth();
            }
        }
    }

    private void sendCliDir(String pathSrv) {
    }

    private void sendDirOnCli(String pathCli) {
    }

    private void createSrvDir(String pathSrv) {
        String pathName=pathSrv;
        pathName+="\\";
        PanelController cliPanContr = (PanelController) controller.rightPanel.getProperties().get("Client");
        Path cliPath = Paths.get(cliPanContr.getCurrentPath(),cliPanContr.getSelectedFileName());
        pathName += String.valueOf(cliPath);
        pathSrv = pathName;
        File file = new File(pathName);
        System.out.println(pathSrv);
        file.mkdirs();
    }

    public void auth(AuthMessage msg) {
        sChannel.writeAndFlush(msg);
    }
}
