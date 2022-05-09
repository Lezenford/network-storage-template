package ru.gb.storage.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler <Message> {
    private RandomAccessFile accessFile;

    private int counterMsg=0;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof AuthMessage) {
            AuthMessage authMessage = (AuthMessage) msg;
            System.out.println("Authorization");
            //
            // допилить обаботку авторизации
            //
            ctx.writeAndFlush(msg);
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
            if (accessFile ==null){
                final File file = new File(frMessage.getPath());
                accessFile = new RandomAccessFile(file,"r");
                sendTailFile(ctx);
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
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client is disconnect");
        if (accessFile != null){
            ctx.close();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
//        AuthMessage answer = new AuthMessage();
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection. For HELP send //help");
        ctx.writeAndFlush(answer);

    }

}
