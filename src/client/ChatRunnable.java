package client;

import GUI.ChatFrame;
import GUI.MiddlePanel;
import data.User;
import exception.NotFriendException;
import exception.ServiceNotFoundException;
import exception.UnknownUserException;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.security.auth.callback.Callback;
import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class ChatRunnable implements Runnable, Callback {
    private Socket socket;
    private Client client;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private SecretKey sessionKey;
    public String friendResponse;
    private MiddlePanel panel;

    public ChatRunnable(Socket socket, Client client, MiddlePanel panel) {
        this.socket = socket;
        this.client = client;
        sessionKey = null;
        this.panel = panel;
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
                    out.writeObject(messageWrapper.getWrappedData());
                    socket.close();
                    break;
                case "InitChat":
                    messageWrapper = initChat(messageUnwrapper);
                    assert messageWrapper != null;
                    out.writeObject(messageWrapper.getWrappedData());
                    break;
                case "Chat":
                    throw new Exception("Should never arrive here!");
                default:
                    throw new ServiceNotFoundException();
            }
        } catch (UnknownUserException | InvalidKeyException | ServiceNotFoundException | NoSuchAlgorithmException | ClassNotFoundException | SignatureException | NotFriendException | IllegalBlockSizeException | BadPaddingException | IOException | NoSuchPaddingException | InvalidKeySpecException | InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MessageWrapper processFriendRequest(MessageWrapper messageUnwrapper) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, ClassNotFoundException, InvalidKeySpecException, InvocationTargetException, InterruptedException {
        String fromUser = messageUnwrapper.getHeader().get("Username");
        System.out.println("=======Friend request=======");
        System.out.println(messageUnwrapper);
        SwingUtilities.invokeAndWait(() -> {
            int choice = JOptionPane.showConfirmDialog(null, "Accept user " + fromUser + "?", "To " + client.username, JOptionPane.YES_NO_OPTION);
            friendResponse = (choice == JOptionPane.YES_OPTION? "Accept":"Reject");
        });
        if (this.friendResponse.equals("Accept")) {
            byte[] body = messageUnwrapper.getBody();
            User user = (User) CommonUtils.stringToObject(EncryptionUtils.decryptWithRSA(body, client.getPrivateKey()));
            Friend friend = new Friend(user.getUsername(), user.getPort(), user.getIP().getHostAddress(), user.getPublicKey());
            client.addFriend(friend);
            panel.refresh();
        }
        MessageHeader header = new MessageHeader();
        header
                .add("Service", "friendRequest")
                .add("Response", this.friendResponse);
        PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
        return new MessageWrapper(header, new byte[0], serverPublicKey, client.getPrivateKey());
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
            System.out.println(messageUnwrapper);
            System.out.println("=========InitChat with" + friendName + "========");
            //show dialog GUI
            new ChatFrame(client, client.getFriend(friendName), socket, sessionKey, out, in);
            return new MessageWrapper(header, body2, client.getFriendPublicKey(friendName), client.getPrivateKey());
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw e;
        } catch (Exception e) { // SignatureException | UnknownUserException | NotFriendException
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
