package ru.itis.semester_work2.network;

import ru.itis.semester_work2.protocol.ConnectMessage;
import ru.itis.semester_work2.protocol.Message;
import ru.itis.semester_work2.protocol.MessageHandler;
import ru.itis.semester_work2.protocol.PlayerInputMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerEventHandler {
    private final Selector selector;
    private final ByteBuffer buffer;
    private final Map<SocketChannel, Integer> clients = new ConcurrentHashMap<>();
    private final AtomicInteger playerCounter = new AtomicInteger(1);


    public ServerEventHandler(Selector selector) {
        this.selector = selector;
        this.buffer = ByteBuffer.allocate(1024);
    }


    public void handleEvents() throws IOException {
        if (selector.select(16) > 0) {
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    acceptClient(key);
                }
                if (key.isReadable()) {
                    readFromClient(key);
                }
            }

        }
    }

    private void acceptClient(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        int playerId = playerCounter.getAndIncrement();
        clients.put(clientChannel, playerId);

    }

    private void readFromClient(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();

        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                return;
            }
            buffer.flip();
            String json = StandardCharsets.UTF_8.decode(buffer).toString().trim();
            if (json.isEmpty()) {
                return;
            }
            Message message = MessageHandler.deserialize(json);

            handleMessage(clientChannel, message);
        } catch (IOException e) {
            disconnectClient(key, clientChannel);
        }
    }

    private void handleMessage(SocketChannel clientChannel, Message message) {
        int playerId = clients.get(clientChannel);

        switch (message.getType()) {
            case CONNECT -> {
                ConnectMessage connect = (ConnectMessage) message;
                System.out.println("Игрок " + connect.getPlayerName() + " присоединился ID: " + playerId);
                // TODO: создать игрока в игровом мире
            }
            case PLAYER_INPUT -> {
                PlayerInputMessage input = (PlayerInputMessage) message;
                input.setOwnerId(playerId);
                // TODO: применить ввод к игроку
            }
        }
    }

    private void disconnectClient(SelectionKey key, SocketChannel channel) throws IOException {
        Integer playerId = clients.remove(channel);
        key.cancel();
        channel.close();
        System.out.println("Клиент отключён, ID: " + playerId);
    }

    public void broadcast(Message message) throws IOException {
        String json = MessageHandler.serialize(message) + "\n";
        ByteBuffer buf = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8));

        for (SocketChannel client : clients.keySet()) {
            buf.rewind();
            client.write(buf);
        }
    }
}