package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import ru.gb.storage.message.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler  extends SimpleChannelInboundHandler<Message> {

    private static Network network;

    public static void setNetwork(Network network) {
        ClientHandler.network = network;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final FileRequestMessage frMessage = new FileRequestMessage();
//        frMessage.setPath(String.valueOf(nameFileIn));
            frMessage.setPath(String.valueOf(Network.getPathCli()));
            ctx.writeAndFlush(frMessage);
    }

    @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            if (msg instanceof TextMessage){
                TextMessage textmsg = (TextMessage) msg;
                if (textmsg.getText().equals("/successAuth")) {
                    System.out.println("Success Auth");
                    PanelController.setAuthTrue(true);
                    network.sendReqAuth(msg);
                } else {
                    Alert alert= new Alert(Alert.AlertType.ERROR, "Authorization on Server is fallen, try again", ButtonType.OK);
                    alert.showAndWait();
                }
            }
            if (msg instanceof FileContentMessage) {
                System.out.println("File transfer Start to local " + msg);
                FileContentMessage fcMessage = (FileContentMessage) msg;
//                try (final RandomAccessFile raf = new RandomAccessFile(nameFileOut, "rw")) {
                try (final RandomAccessFile raf = new RandomAccessFile(Network.getPathSrv(), "rw")) {
                        raf.seek(fcMessage.getStartPosition());
                    raf.write(fcMessage.getContent());
                    if (fcMessage.isLast()) {
                        Path pathThis = (Path)Paths.get(network.getPathSrv());
                        network.controller.panelController.updateList(pathThis);
                        System.out.println("File transfer Finish");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
}


