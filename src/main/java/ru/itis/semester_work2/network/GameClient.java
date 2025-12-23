package ru.itis.semester_work2.network;

import ru.itis.semester_work2.protocol.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class GameClient {
    private SocketChannel socketChannel;
    private Selector selector;
    private final ByteBuffer buffer = ByteBuffer.allocate(4096);
    private boolean connected = false;
    private ClientEventListener listener;

    public void connect(String host, int port) throws IOException {
        selector = Selector.open();
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(host, port));
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        System.out.println("Подключение к серверу " + host + ":" + port);
    }

    public void update() throws IOException {
        if (selector.select(16) > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) continue;

                if (key.isConnectable()) {
                    handleConnect(key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
            }
        }
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (channel.finishConnect()) {
            connected = true;
            channel.register(selector, SelectionKey.OP_READ);
            System.out.println("Подключено к серверу!");

            if (listener != null) {
                listener.onConnected();
            }
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        buffer.clear();

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            disconnect();
            return;
        }

        buffer.flip();
        String json = StandardCharsets.UTF_8.decode(buffer).toString().trim();

        if (json.isEmpty()) return;

        String[] messages = json.split("\n");
        for (String msg : messages) {
            if (!msg.isEmpty()) {
                Message message = MessageHandler.deserialize(msg);
                handleMessage(message);
            }
        }
    }

    private void handleMessage(Message message) {
        if (listener == null) return;

        switch (message.getType()) {
            case GAME_START -> listener.onGameStart((GameStartMessage) message);
            case GAME_STATE -> listener.onGameState((GameStateMessage) message);
            case GAME_OVER -> listener.onGameOver((GameOverMessage) message);
        }
    }

    public void send(Message message) throws IOException {
        if (!connected || socketChannel == null) return;

        String json = MessageHandler.serialize(message) + "\n";
        ByteBuffer buf = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8));
        socketChannel.write(buf);
    }

    public void disconnect() throws IOException {
        connected = false;
        if (socketChannel != null) {
            socketChannel.close();
        }
        if (selector != null) {
            selector.close();
        }
        System.out.println("Отключено от сервера");

        if (listener != null) {
            listener.onDisconnected();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setListener(ClientEventListener listener) {
        this.listener = listener;
    }
}

