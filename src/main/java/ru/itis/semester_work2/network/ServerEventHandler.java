package ru.itis.semester_work2.network;

import ru.itis.semester_work2.game.GameEngine;
import ru.itis.semester_work2.protocol.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.itis.semester_work2.game.GameConstants.*;

public class ServerEventHandler {
    private final Selector selector;
    private final ByteBuffer buffer;
    private final Map<SocketChannel, Integer> clients = new ConcurrentHashMap<>();
    private final Map<SocketChannel, StringBuilder> clientBuffers = new ConcurrentHashMap<>();
    private final Map<Integer, String> playerNames = new HashMap<>();
    private final AtomicInteger playerCounter = new AtomicInteger(1);

    private GameEngine gameEngine;
    private boolean gameStarted = false;
    private long lastUpdateTime;


    public ServerEventHandler(Selector selector) {
        this.selector = selector;
        this.buffer = ByteBuffer.allocate(4096);
    }


    public void handleEvents() throws IOException {
        if (selector.select(1) > 0) {
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

        if (gameStarted && gameEngine != null) {
            long now = System.currentTimeMillis();
            double deltaTime = (now - lastUpdateTime) / 1000.0;
            lastUpdateTime = now;

            gameEngine.update(deltaTime);

            broadcast(new GameStateMessage(gameEngine.getGameState()));

            if (gameEngine.isGameOver()) {
                int winnerId = gameEngine.getWinnerId();
                String winnerName = playerNames.getOrDefault(winnerId, "Игрок " + winnerId);
                broadcast(new GameOverMessage(winnerId, winnerName));
                gameStarted = false;
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
        clientBuffers.put(clientChannel, new StringBuilder());
    }

    private void readFromClient(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        buffer.clear();

        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                disconnectClient(key, clientChannel);
                return;
            }

            buffer.flip();
            String received = StandardCharsets.UTF_8.decode(buffer).toString();

            StringBuilder clientBuffer = clientBuffers.get(clientChannel);
            if (clientBuffer == null) {
                clientBuffer = new StringBuilder();
                clientBuffers.put(clientChannel, clientBuffer);
            }
            clientBuffer.append(received);

            String data = clientBuffer.toString();
            String[] messages = data.split("\n");
            for (String msg : messages) {
                msg = msg.trim();
                if (!msg.isEmpty()) {
                    try {
                        Message message = MessageHandler.deserialize(msg);
                        handleMessage(clientChannel, message);
                    } catch (Exception e) {
                        System.err.println("Ошибка парсинга сообщения: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            disconnectClient(key, clientChannel);
        }
    }

    private void handleMessage(SocketChannel clientChannel, Message message) throws IOException {
        int playerId = clients.get(clientChannel);

        switch (message.getType()) {
            case CONNECT -> {
                ConnectMessage connect = (ConnectMessage) message;
                playerNames.put(playerId, connect.getPlayerName());
                System.out.println("Игрок " + connect.getPlayerName() + " присоединился ID: " + playerId);

                if (playerNames.size() == 2 && !gameStarted) {
                    playerNames.clear();
                    startGame();
                }
            }
            case PLAYER_INPUT -> {
                if (gameStarted && gameEngine != null) {
                    PlayerInputMessage input = (PlayerInputMessage) message;
                    gameEngine.setPlayerInput(playerId, input);
                }
            }
        }
    }

    private void startGame() throws IOException {
        gameEngine = new GameEngine();
        gameStarted = true;
        lastUpdateTime = System.currentTimeMillis();

        System.out.println("Игра началась!");

        for (Map.Entry<SocketChannel, Integer> entry : clients.entrySet()) {
            GameStartMessage startMsg = new GameStartMessage(entry.getValue(), MAP_WIDTH, MAP_HEIGHT);
            sendTo(entry.getKey(), startMsg);
        }
    }

    private void sendTo(SocketChannel channel, Message message) throws IOException {
        String json = MessageHandler.serialize(message) + "\n";
        ByteBuffer buf = ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8));
        channel.write(buf);
    }

    private void disconnectClient(SelectionKey key, SocketChannel channel) throws IOException {
        Integer playerId = clients.remove(channel);
        clientBuffers.remove(channel);
        key.cancel();
        channel.close();
        System.out.println("Клиент отключён, ID: " + playerId);
    }

    public void broadcast(Message message) {
        String json = MessageHandler.serialize(message) + "\n";
        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        Iterator<Map.Entry<SocketChannel, Integer>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SocketChannel, Integer> entry = iterator.next();
            SocketChannel client = entry.getKey();
            try {
                ByteBuffer buf = ByteBuffer.wrap(data);
                while (buf.hasRemaining()) {
                    client.write(buf);
                }
            } catch (IOException e) {
                System.out.println("Клиент отключён (broadcast), ID: " + entry.getValue());
                iterator.remove();
                try {
                    client.close();
                } catch (IOException ignored) {}
            }
        }
    }
}