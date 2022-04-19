package ru.gb.storage.commons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.gb.storage.commons.message.Msg;

import java.util.List;

public class JsonDecoder extends MessageToMessageDecoder<String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, String message, List<Object> listOut) throws Exception {
        System.out.println("Входящее сообщение: " + message);
        Msg msg = mapper.readValue(message, Msg.class);
        System.out.println("преобразовать входящее сообщение в: " + msg);
        listOut.add(msg);

    }
}
