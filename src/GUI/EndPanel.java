package GUI;

import client.Client;
import client.Friend;
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
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class EndPanel extends JPanel implements ActionListener{
    private Client client;
    private Friend friend;
    private JButton button;
    private JTextField textField;
    private JTextArea textArea;
    private JButton fileBtn;
    private Socket socket;
    private SecretKey sessionKey;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public EndPanel(Client client, Friend friend, Socket socket, SecretKey sessionKey, ObjectOutputStream out, ObjectInputStream in) throws IOException {
        this.client = client;
        this.friend = friend;
        this.socket = socket;
        this.sessionKey = sessionKey;
        this.out = out;
        this.in = in;
        button = new JButton("SEND");
        fileBtn = new JButton("FILE");
        textField = new JTextField();
        textArea = new JTextArea();
        setLayout(new BorderLayout());
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        ((DefaultCaret)textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        add(scrollPane, BorderLayout.CENTER);
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(textField);
        JPanel panel1 = new JPanel(new GridLayout(1, 2));
        panel1.add(button);
        panel1.add(fileBtn);
        panel.add(panel1);
        this.add(panel, BorderLayout.SOUTH);
        button.addActionListener(this);
        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: choose file
            }
        });
        new Thread(new ReceiveRunnable()).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        MessageHeader header = new MessageHeader();
        header
                .add("Service", "Chat")
                .add("Username", client.username);
        String message = textField.getText();
        System.out.println(message);
        try {
            byte[] body = EncryptionUtils.symmetricEncrypt(message, sessionKey);
            MessageWrapper messageWrapper = new MessageWrapper(header, body, friend.publicKey, client.getPrivateKey());
            out.writeObject(messageWrapper.getWrappedData());
            //show in dialog
            textArea.append(client.username + ": " + message + "\n");
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IOException e1) {
            e1.printStackTrace();
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReceiveRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println("In ReceiveRunnable");
            while (!socket.isClosed()) {
                try {
                    byte[] receivedBytes = (byte[]) in.readObject();//block
                    MessageWrapper messageUnwrapper = new MessageWrapper(receivedBytes, friend.publicKey, client.getPrivateKey());
                    System.out.println(messageUnwrapper);
                    String service = messageUnwrapper.getHeader().get("Service");
                    switch (service) {
                        case "Chat":
                            byte[] body = messageUnwrapper.getBody();
                            String message = EncryptionUtils.symmetricDecrypt(body, sessionKey);
                            textArea.append(friend.name + ": " + message + "\n");
                            break;
                        case "File":
                            break;
                        default:
                            throw new ServiceNotFoundException();
                    }
                } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | UnknownUserException | ServiceNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
