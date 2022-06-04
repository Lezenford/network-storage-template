package ru.gb.storage.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.message.*;
import ru.gb.storage.server.Database.Database;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler <Message> {
    private RandomAccessFile accessFile;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) msg;
            try {
//                if (!Database.isConnected()) {
                    Database.connect();
//                }
                if (authMessage.isSignUp()) {
                    if (Database.signUp(authMessage.getLogin(), authMessage.getPass())) {
                        System.out.println("New Client full registration");
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("/correctSignUp");
                        ctx.writeAndFlush(textMessage);
                    } else {
                        TextMessage textMessage = new TextMessage();
                        textMessage.setText("/incorSignUp");
                        ctx.writeAndFlush(textMessage);
                    }
                }
                if (Database.login(authMessage.getLogin(), authMessage.getPass())) {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setText("/successAuth");
                    ctx.writeAndFlush(textMessage);
                    Database.disconnect();
                } else {
                    TextMessage textMessage = new TextMessage();
                    textMessage.setText("/incorLogPass");
                    ctx.writeAndFlush(textMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (msg instanceof TextMessage){
            TextMessage txtMessage = (TextMessage) msg;
            System.out.println("Incoming text message: " + txtMessage.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DateMessage){
            DateMessage dateMessage = (DateMessage) msg;
            System.out.println("Incoming date message: " + dateMessage.getDate());
            ctx.writeAndFlush(msg);
        }

        if (msg instanceof FileRequestMessage){
            FileRequestMessage frMessage = (FileRequestMessage) msg;
            try {
                if (accessFile == null) {
                    final File file = new File(frMessage.getPath());
                    accessFile = new RandomAccessFile(file, "r");
                    sendTailFile(ctx);
                }
            }catch (NullPointerException nullPointerException){
                System.out.println("\n nullpointexception "+nullPointerException);
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws IOException {
        System.out.println("Client channel is Inactive");
//        System.out.println("Client is disconnect");
        if (accessFile != null){
            accessFile.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
//        AuthMessage answer = new AuthMessage();
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection.");
        ctx.writeAndFlush(answer);

    }

}
