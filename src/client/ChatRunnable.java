package client;

import data.User;
import exception.NotFriendException;
import exception.ServiceNotFoundException;
import exception.UnknownUserException;
import server.Server;
import service.Service;
import util.CommonUtils;
import util.EncryptionUtils;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

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
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            byte[] received = (byte[]) in.readObject();
            MessageWrapper messageUnwrapper = new MessageWrapper(received, client.getPrivateKey(), client);
            String service = messageUnwrapper.getHeader().get("Service");
            MessageWrapper messageWrapper;
            switch (service) {
                case "friendRequest":
                    messageWrapper = processFriendRequest(messageUnwrapper);
                    break;
                case "InitChat":
                    messageWrapper = initChat(messageUnwrapper);
                    break;
                case "Chat":
                    messageWrapper = chat(messageUnwrapper);
                    break;
                default:
                    throw new ServiceNotFoundException();
            }
            out.writeObject(messageWrapper.getWrappedData());
            socket.close();
        } catch (UnknownUserException | InvalidKeyException | ServiceNotFoundException | NoSuchAlgorithmException | ClassNotFoundException | SignatureException | NotFriendException | IllegalBlockSizeException | BadPaddingException | IOException | NoSuchPaddingException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private MessageWrapper processFriendRequest(MessageWrapper messageUnwrapper) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, ClassNotFoundException {
        String fromUser = messageUnwrapper.getHeader().get("Username");
        System.out.println("=======Friend request=======");
        System.out.println(messageUnwrapper);
        int choice = JOptionPane.showConfirmDialog(null, "Accept add friend request from " + fromUser + "?", "Add Friend Request", JOptionPane.YES_NO_OPTION);
        String response;
        if (choice == JOptionPane.YES_OPTION) {
            byte[] body = messageUnwrapper.getBody();
            User user = (User) CommonUtils.stringToObject(EncryptionUtils.decryptWithRSA(body, client.getPrivateKey()));
            Friend friend = new Friend(user.getUsername(), user.getPort(), user.getIP().getHostAddress(), user.getPublicKey());
            client.addFriend(friend);
            response = "Accept";
        } else {
            response = "Reject";
        }
        MessageHeader header = new MessageHeader();
        header
                .add("Service", "friendRequest")
                .add("Response", response);
        return new MessageWrapper(header, new byte[0], Server.SERVER_PUBLIC_KEY, client.getPrivateKey());
    }

    private MessageWrapper chat(MessageWrapper messageUnwrapper) {
        //TODO: IM
        return messageUnwrapper;
    }

    private MessageWrapper initChat(MessageWrapper messageUnwrapper) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, ClassNotFoundException {
        try {
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
            return new MessageWrapper(header, body2, client.getFriendPublicKey(friendName), client.getPrivateKey());
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw e;
        } catch (Exception e) { // SignatureException | UnknownUserException | NotFriendException
            System.out.println(e.getMessage());
            e.printStackTrace();
            MessageHeader header = new MessageHeader();
            header
                    .add("Status", "404")
                    .add("Service", "ConfirmChat");
            //TODO: send plain text
//            MessageWrapper messageWrapper = new MessageWrapper();
            return null;
        }
    }
}
