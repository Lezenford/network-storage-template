package ru.gb.storage.server.mynio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public final class ServerNio {

    private static ServerNio instance = null;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private ServerNio() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 22333));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Сервер стартовал");

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            for(Iterator<SelectionKey> iterator = selectionKeySet.iterator(); iterator.hasNext();) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("Новый селектор");
                    SocketChannel client = serverSocketChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("Подключился клиент");
                }
                if (selectionKey.isReadable()) {
                    System.out.println("Новый селектор для чтения");
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                    CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(64);
                    client.read(byteBuffer);
                    byteBuffer.flip();
                    String msg = decoder.decode(byteBuffer).toString();
                    System.out.println ("Получено: " + msg);
                    client.write(encoder.encode(CharBuffer.wrap(msg)));

                }
                iterator.remove();
            }
        }
    }

    public static void getInstance() throws IOException {
        if (instance == null) {
            instance = new ServerNio();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerNio.getInstance();
    }
}

