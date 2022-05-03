package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import message.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FirstServerHandler extends SimpleChannelInboundHandler <Message> {
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
        if (msg instanceof FileRequestMessage){

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
            final File file = new File(frMessage.getPath());
            try (final RandomAccessFile accessFile = new RandomAccessFile(file, "r");){
                while (accessFile.getFilePointer()!= accessFile.length()){
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
                    flMessage.setLast(accessFile.getFilePointer() == accessFile.length());
                    ctx.writeAndFlush(flMessage);
                    System.out.println("Message OUT " + ++counterMsg);
                }
            } catch (IOException e){
                throw new RuntimeException(e);
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
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
//        AuthMessage answer = new AuthMessage();
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection");
        ctx.writeAndFlush(answer);
    }

}
