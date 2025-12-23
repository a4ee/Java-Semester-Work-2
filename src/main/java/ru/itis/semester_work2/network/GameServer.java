package ru.itis.semester_work2.network;


import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Scanner;

public class GameServer {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private boolean running;



    private void start(int port) throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ServerEventHandler eventHandler = new ServerEventHandler(selector);

        while (running) {
            eventHandler.handleEvents();
        }


    }



    public static void main(String[] args) {
        while (true){
            int port = setPort();
            try {
                GameServer server = new GameServer();
                server.start(port);
                break;
            }
            catch (BindException e){
                System.out.println("Порт: " + port + " занят, попробуйте другой");
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private static int setPort(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите порт для запуска сервера:");
        return Integer.parseInt(scanner.nextLine());
    }

}
