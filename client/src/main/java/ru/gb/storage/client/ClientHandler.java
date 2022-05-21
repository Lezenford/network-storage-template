package ru.gb.storage.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.storage.message.FileContentMessage;
import ru.gb.storage.message.FileRequestMessage;
import ru.gb.storage.message.Message;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
        private String  nameFileIn;
        private String  nameFileOut;
        private Callback onSendMSG;

        public ClientHandler(String srcPathStr, String dstPathStr) {
            this.nameFileIn = srcPathStr;
            this.nameFileOut = dstPathStr;
        }

    public ClientHandler(Callback onSendMSG) {
    }

    @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final FileRequestMessage frMessage = new FileRequestMessage();
            frMessage.setPath(String.valueOf(nameFileIn));
            ctx.writeAndFlush(frMessage);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
            System.out.println("File transfer Start " + msg);
            if (msg instanceof FileContentMessage) {
                FileContentMessage fcMessage = (FileContentMessage) msg;
                try (final RandomAccessFile raf = new RandomAccessFile(String.valueOf(nameFileOut), "rw")) {
                    raf.seek(fcMessage.getStartPosition());
                    raf.write(fcMessage.getContent());
                    if (fcMessage.isLast()) {
                        ctx.close();
                        System.out.println("File transfer Finish");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
}


