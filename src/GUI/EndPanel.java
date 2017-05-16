package GUI;

import client.Client;
import client.Friend;
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
    private MyFrame frame;
    private Friend friend;
    private JButton button;
    private JTextField textField;
    private JTextArea textArea;
    private JButton fileBtn;
    private Socket socket;

    public EndPanel(MyFrame frame) {
        this.frame = frame;
        button = new JButton("SEND");
        fileBtn = new JButton("File");
        textField = new JTextField();
        textArea = new JTextArea();
        setLayout(new FlowLayout());
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        ((DefaultCaret)textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        add(scrollPane);
        add(textField);
        add(button);
        add(fileBtn);
        button.addActionListener(this);
        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: choose file
            }
        });
    }

    public void connect() {
        //initialize connection
        try {
            socket = new Socket(friend.ip, friend.port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "InitChat")
                    .add("Username", client.username);
            SecretKey sessionKey = KeyGenerator.generateSymmetricKey();
            byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(sessionKey), this.friend.publicKey);
            MessageWrapper messageWrapper = new MessageWrapper(header, body, this.friend.publicKey, this.client.getPrivateKey());
            out.writeObject(messageWrapper.getWrappedData());
            //wait for response
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            byte[] received = (byte[]) in.readObject();
            MessageWrapper messageUnwrapper = new MessageWrapper(received, this.friend.publicKey, client.getPrivateKey());
            byte[] body2 = messageUnwrapper.getBody();
            String m = EncryptionUtils.symmetricDecrypt(body2, sessionKey);
            if (m.equals("Confirm")) {
                System.out.println("=======Begin Chatting========");
            } else {
                throw new Exception("Init Chat Failure");
            }

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {// | SignatureException | UnknownUserException
            System.out.println(e.getMessage());
            e.printStackTrace();
            //TODO: plain response
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
