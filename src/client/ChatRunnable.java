package client;

import exception.UnknownUserException;
import util.CommonUtils;
import util.EncryptionUtils;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class ChatRunnable implements Runnable {
    private Socket socket;
    private Client client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecretKey sessionKey;

    public ChatRunnable(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
        sessionKey = null;
    }

    @Override
    public void run() {
        if (sessionKey == null) { // shake hand
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                byte[] received = (byte[]) in.readObject();
                //do not know from whom, may throw Exception if signature check fails or not a friend or header lost
                MessageWrapper messageUnwrapper = new MessageWrapper(received, client.getPrivateKey(), client);
                //if success
                String friendName = messageUnwrapper.getHeader().get("Username");
                byte[] body = messageUnwrapper.getBody();
                String decryptedBody = EncryptionUtils.decryptWithRSA(body, client.getPrivateKey());
                sessionKey = (SecretKey) CommonUtils.stringToObject(decryptedBody);
                //respond
                MessageHeader header = new MessageHeader();
                header
                        .add("Service", "ConfirmChat")
                        .add("Username", client.username)
                        .add("Status", "200");
                byte[] body2 = EncryptionUtils.symmetricEncrypt("Confirm", sessionKey);
                MessageWrapper messageWrapper = new MessageWrapper(header, body2, client.getFriendPublicKey(friendName), client.getPrivateKey());
                out.writeObject(messageWrapper.getWrappedData());
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (Exception e) { // SignatureException | UnknownUserException | NotFriendException
                System.out.println(e.getMessage());
                e.printStackTrace();
                MessageHeader header = new MessageHeader();
                header
                        .add("Status", "404")
                        .add("Service", "ConfirmChat");
                //TODO: send plain text
            }
        }
        //chat

    }
}
