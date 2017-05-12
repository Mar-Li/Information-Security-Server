package server;

import util.KeyGenerator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by lifengshuang on 11/05/2017.
 */
public class Server {

    private static final int SERVER_PORT = 2333;
    public static PrivateKey SERVER_PRIVATE_KEY;
    public static PublicKey SERVER_PUBLIC_KEY;

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        SERVER_PRIVATE_KEY = KeyGenerator.loadPrivateKey("key/server.pri");
        SERVER_PUBLIC_KEY = KeyGenerator.loadPublicKey("key/server.pub");
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new RequestHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
