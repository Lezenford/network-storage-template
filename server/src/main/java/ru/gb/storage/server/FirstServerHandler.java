package ru.gb.storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.common.message.*;

import java.awt.*;
import java.io.*;
import java.util.Arrays;

public class FirstServerHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("New active channel");
        TextMessage answer = new TextMessage();
        answer.setText("Successfully connection");
        ctx.writeAndFlush(answer);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof TextMessage) {
            TextMessage message = (TextMessage) msg;
            System.out.println("incoming text message: " + message.getText());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof DateMessage) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof AuthMessage) {
            AuthMessage message = (AuthMessage) msg;
            System.out.println("incoming auth message: " + message.getPassword() + " " + message.getPassword());
            ctx.writeAndFlush(msg);
        }
        if (msg instanceof FileRequestMessage) {
            FileRequestMessage frm = (FileRequestMessage) msg;
            final File file = new File(frm.getPath());
            try (final RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                while (raf.getFilePointer() != raf.length()) {
                    final byte[] fileContent;
                    final long available = raf.length() - raf.getFilePointer();
                    if (available > 100 * 1024) {
                        fileContent = new byte[100 * 1024];
                    } else {
                        fileContent = new byte[(int) available];
                    }
                    final FileContentMessage fileContentMessage = new FileContentMessage();
                    fileContentMessage.setStartPosition(raf.getFilePointer());
                    raf.read(fileContent);
                    fileContentMessage.setContent(fileContent);
                    fileContentMessage.setLastPosition(raf.getFilePointer() == raf.length());
                    ctx.writeAndFlush(fileContentMessage);
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        System.out.println("client disconnect");
    }


}

