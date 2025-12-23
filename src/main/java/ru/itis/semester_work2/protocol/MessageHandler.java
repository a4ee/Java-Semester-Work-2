package ru.itis.semester_work2.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MessageHandler {
    private static final Gson gson = new GsonBuilder().create();

    public static String serialize(Message message) {
        return gson.toJson(message);
    }

    public static Message deserialize(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        MessageType type = MessageType.valueOf(jsonObject.get("type").getAsString());

        return switch (type) {
            case CONNECT -> gson.fromJson(json, ConnectMessage.class);
            case GAME_START -> gson.fromJson(json, GameStartMessage.class);
            case PLAYER_INPUT -> gson.fromJson(json, PlayerInputMessage.class);
            case GAME_STATE -> gson.fromJson(json, GameStateMessage.class);
            case GAME_OVER -> gson.fromJson(json, GameOverMessage.class);
        };
    }
}

