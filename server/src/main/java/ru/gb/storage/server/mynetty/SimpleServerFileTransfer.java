package ru.gb.storage.server.mynetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.storage.commons.handler.JsonDecoder;
import ru.gb.storage.commons.handler.JsonEncoder;
import ru.gb.storage.commons.message.FileContentMessage;
import ru.gb.storage.commons.message.FileRequestMessage;
import ru.gb.storage.commons.message.Msg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

public final class SimpleServerFileTransfer {

    private static SimpleServerFileTransfer instance = null;
    private NioEventLoopGroup general;
    private NioEventLoopGroup worker;
    private ServerBootstrap server;

    private SimpleServerFileTransfer() {
        this.general = new NioEventLoopGroup(1);
        this.worker = new NioEventLoopGroup();

        try {
            this.server = new ServerBootstrap();
            this.server.group(general, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Msg>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Msg msg) throws Exception {
                                            if (msg instanceof FileRequestMessage) {
                                                FileRequestMessage fileRequestMessage = (FileRequestMessage) msg;
                                                File file = new File(fileRequestMessage.getPath());
                                                viewFiles(file, 0);
                                                try (final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                                                    while (randomAccessFile.getFilePointer() != randomAccessFile.length()) {
                                                        byte[] fileContent;
                                                        final long available = randomAccessFile.length() - randomAccessFile.getFilePointer();
                                                        if (available > 64 * 1024) {
                                                            fileContent = new byte[64 * 1024];
                                                        } else {
                                                            fileContent = new byte[(int) available];
                                                        }
                                                        FileContentMessage fileContentMessage = new FileContentMessage();
                                                        fileContentMessage.setStartPosition(randomAccessFile.getFilePointer());
                                                        randomAccessFile.read(fileContent);
                                                        fileContentMessage.setContent(fileContent);
                                                        fileContentMessage.setLastPosition(randomAccessFile.getFilePointer() == randomAccessFile.length());
                                                        channelHandlerContext.writeAndFlush(fileContentMessage);
                                                    }
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }
                                    });

                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = server.bind(22333).sync();
            System.out.println("Server started");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            general.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    private void getInstance() {
        if (instance == null) {
            instance = new SimpleServerFileTransfer();
        }
    }

    public static void viewFiles(File file, int count) {
        StringBuilder prefix = new StringBuilder();
        prefix.append("\t".repeat(count));

        if (file.isFile()) {
            System.out.println(prefix + "File: " + file.getName());
        } else {
            System.out.println(prefix + "Dir: " + file.getName());
            count++;
            for (File fileLise : Objects.requireNonNull(file.listFiles())) {
                viewFiles(fileLise, count);
            }
        }
    }

    public static void main(String[] args) {
        new SimpleServerFileTransfer().getInstance();
    }


}
