package ru.gb.storage.client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.message.FileContentMessage;
import ru.gb.storage.message.FileRequestMessage;
import ru.gb.storage.message.Message;
import ru.gb.storage.message.TextMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler  extends SimpleChannelInboundHandler<Message> {

    private static Network network;
    private RandomAccessFile accessFile;

    public static void setNetwork(Network network) {
        ClientHandler.network = network;
    }

    @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws IOException,NullPointerException, RuntimeException, FileNotFoundException,IllegalStateException {
            if (msg instanceof TextMessage){
                TextMessage textmsg = (TextMessage) msg;
                if (textmsg.getText().equals("/successAuth")) {
                    System.out.println("Success Auth");
                    network.sendReqAuth(msg);
                } else {
                    System.out.println("Authorisation is failed. Try again");
//                    Alert err = new Alert(Alert.AlertType.ERROR,"Authorisation is failed. Try again", ButtonType.OK);
//                    err.showAndWait();
                }
            }
            if (msg instanceof FileContentMessage) {
                System.out.println("File transfer Start to LocalPC ");
                FileContentMessage fcMessage = (FileContentMessage) msg;
                try {
                    accessFile =  new RandomAccessFile(Network.getPathFile(), "rw");
                    accessFile.seek(fcMessage.getStartPosition());
                    accessFile.write(fcMessage.getContent());
                    if (fcMessage.isLast()) {
                        Path pathThis = (Path)Paths.get(network.getPathLeft());
                        network.controller.leftPanContr.updateList(pathThis);
                        System.out.println("\nFile transfer to LocalPC Finish");
                    }
                }
                finally {
                    if (accessFile == null) {
                        accessFile.close();
                    }
                }
            }
            if (msg instanceof FileRequestMessage){
                FileRequestMessage frMessage = (FileRequestMessage) msg;
                if (accessFile == null) {
                    System.out.println("File transfer to Network Finish");
                    final File file = new File(frMessage.getPath());
                    accessFile = new RandomAccessFile(file, "r");
                    sendTailFile(ctx);
                    Path pathThis = (Path)Paths.get(network.getPathRight());
                    network.controller.rightPanContr.updateList(pathThis);
                    System.out.println("File transfer to Network Finish");
                }
            }
        }

    private void sendTailFile(ChannelHandlerContext ctx) throws IOException {
        if (accessFile != null){
            final byte[] fileContent;
            final long lengthSector = accessFile.length()-accessFile.getFilePointer();
            if (lengthSector > 64*1024){
                fileContent = new byte[64*1024];
            } else{
                fileContent= new byte[(int)lengthSector];
            }
            final FileContentMessage flMessage = new FileContentMessage();
            flMessage.setStartPosition(accessFile.getFilePointer());
            accessFile.read(fileContent);
            flMessage.setContent(fileContent);
            final boolean last = accessFile.getFilePointer() == accessFile.length();
            flMessage.setLast(last);
            ctx.channel().writeAndFlush(flMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!last) {
                        sendTailFile(ctx);
                    }
                }
            });
            if (last){
                accessFile.close();
                accessFile=null;
            }
        }
    }
}


