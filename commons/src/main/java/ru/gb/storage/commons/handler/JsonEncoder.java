package ru.gb.storage.commons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.gb.storage.commons.message.Msg;

import java.util.List;

public class JsonEncoder extends MessageToMessageEncoder<Msg> {

    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Msg msg, List<Object> listOut) throws Exception {
        System.out.println("Исходящее сообщение: " + msg);
        String outMsg = mapper.writeValueAsString(msg);
        System.out.println("преобразовать сообщение в: " + outMsg);
        listOut.add(outMsg);
    }
}
