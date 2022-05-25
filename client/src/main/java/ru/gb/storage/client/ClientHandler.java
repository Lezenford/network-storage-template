package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.message.AuthMessage;
import ru.gb.storage.message.FileContentMessage;
import ru.gb.storage.message.Message;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler  extends SimpleChannelInboundHandler<Message> {

    private static Network network;

//        private String  nameFileIn;
//        private String  nameFileOut;
//        public ClientHandler(String srcPathStr, String dstPathStr) {
//            this.nameFileIn = srcPathStr;
//            this.nameFileOut = dstPathStr;
//        }
//
//    public ClientHandler() {
//    }
//
//    @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            final FileRequestMessage frMessage = new FileRequestMessage();
//            frMessage.setPath(String.valueOf(nameFileIn));
//            ctx.writeAndFlush(frMessage);
//        }


    public static void setNetwork(Network network) {
        ClientHandler.network = network;
    }

    @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            if (msg instanceof AuthMessage){
                network.sendReqAuth(msg);
            }
            if (msg instanceof FileContentMessage) {
                System.out.println("File transfer Start " + msg);
                FileContentMessage fcMessage = (FileContentMessage) msg;
//                try (final RandomAccessFile raf = new RandomAccessFile(String.valueOf(nameFileOut), "rw")) {
                try (final RandomAccessFile raf = new RandomAccessFile(network.corPathDwnd, "rw")) {
                        raf.seek(fcMessage.getStartPosition());
                    raf.write(fcMessage.getContent());
                    if (fcMessage.isLast()) {
                        network.controller.panelController.updateList((Path) Paths.get("."));
                        System.out.println("File transfer Finish");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
}


