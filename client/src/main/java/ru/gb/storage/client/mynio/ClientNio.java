package ru.gb.storage.client.mynio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ClientNio {

    private static ClientNio instance = null;
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
    private CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
    private Selector selector = null;
    private SocketChannel socket = null;
    private SelectionKey clientKey = null;


    private ClientNio() {
        try {
            selector = Selector.open();
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            clientKey = socket.register(selector, SelectionKey.OP_CONNECT);
            InetSocketAddress ip = new InetSocketAddress("localhost", 22333);
            socket.connect(ip);

            executorService.execute(() -> {
                try {
                    while (true) {
                        selector.select();
                        for(Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext();) {
                            SelectionKey key = it.next();
                            it.remove();
                            if (key.isConnectable()) {
                                SocketChannel channel = (SocketChannel) key.channel();
                                if (channel.isConnectionPending()) {
                                    channel.finishConnect();
                                }
                                channel.register(selector, SelectionKey.OP_READ);
                                System.out.println("Успешно подключились!");
                            } else if (key.isReadable()) {
                                SocketChannel channel = (SocketChannel) clientKey.channel();
                                ByteBuffer buffer = ByteBuffer.allocate(50);
                                channel.read(buffer);
                                buffer.flip();
                                String msg = decoder.decode(buffer).toString();
                                System.out.println("Ответ с сервера: " + msg);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));

        try {
            String readline;
            while ((readline = sin.readLine()) != null) {
                SocketChannel client = (SocketChannel) clientKey.channel();
                client.write(encoder.encode(CharBuffer.wrap(readline)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void getInstance() {
        if (instance == null) {
            instance = new ClientNio();
        }
    }

    public static void main(String[] args) {
        ClientNio.getInstance();
    }
}
