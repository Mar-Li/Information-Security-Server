package client;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class ClientListenRunnable implements Runnable {
    private ServerSocket serverSocket;
    private Client client;

    public ClientListenRunnable(ServerSocket listenSocket, Client client) {
        this.serverSocket = listenSocket;
        this.client = client;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ChatRunnable(clientSocket, client)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
