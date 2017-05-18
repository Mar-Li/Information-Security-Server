package client;


import GUI.MiddlePanel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class ClientListenRunnable implements Runnable {
    private ServerSocket serverSocket;
    private Client client;
    private MiddlePanel panel;

    public ClientListenRunnable(ServerSocket listenSocket, Client client, MiddlePanel panel) {
        this.serverSocket = listenSocket;
        this.client = client;
        this.panel = panel;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ChatRunnable(clientSocket, client, panel)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
