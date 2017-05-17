package GUI;

import client.Client;
import client.Friend;
import data.User;
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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class MiddlePanel extends JPanel implements ActionListener{
    private MyFrame frame;
    private Client client;
    private JButton addFriendBtn;
    //table
    private JTable table;
    private DefaultTableModel tableModel;
    private String[] title = new String[1];
    private Object[][] data;

    public MiddlePanel(MyFrame frame) {
        this.frame = frame;
        addFriendBtn = new JButton("BeFriend");
        setLayout(new BorderLayout());
        add(addFriendBtn, BorderLayout.NORTH);
        if (client != null && client.getFriendList() != null) {
            createTable();
            add(new JScrollPane(table), BorderLayout.CENTER);
        }
        addFriendBtn.addActionListener(this);
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void refresh() {
        if (table == null) {
            createTable();
        } else {
            data = client.getFriendList();
            tableModel = new DefaultTableModel(data, title);
            table.setModel(tableModel);
        }
        this.repaint();
        this.updateUI();
    }

    private void createTable() {
        title[0] = "Name";
        data = client.getFriendList();
        //set JTable
        tableModel = new DefaultTableModel(data, title) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        //event
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.println("Clicked!");
                int index = table.rowAtPoint(e.getPoint());
                new Thread(new ConnectIM(client.getFriend(index))).start();
            }
        });
        this.add(table, BorderLayout.CENTER);
    }

    private class ConnectIM implements Runnable {
        private Friend friend;

        public ConnectIM(Friend friend) {
            this.friend = friend;
        }

        @Override
        public void run() {
            try {
                Socket socket = new Socket(friend.ip, friend.port);
                System.out.println(client.username +  " connect to " + friend.name);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                MessageHeader header = new MessageHeader();
                header
                        .add("Service", "InitChat")
                        .add("Username", client.username);
                SecretKey sessionKey = KeyGenerator.generateSymmetricKey();
                byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(sessionKey), this.friend.publicKey);
                MessageWrapper messageWrapper = new MessageWrapper(header, body, this.friend.publicKey, client.getPrivateKey());
                out.writeObject(messageWrapper.getWrappedData());
                //wait for response
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                byte[] received = (byte[]) in.readObject();
                MessageWrapper messageUnwrapper = new MessageWrapper(received, this.friend.publicKey, client.getPrivateKey());
                byte[] body2 = messageUnwrapper.getBody();
                String m = EncryptionUtils.symmetricDecrypt(body2, sessionKey);
                if (m.equals("Confirm")) {
                    System.out.println("=======Begin Chatting========");
                    new ChatFrame(client, friend, socket, sessionKey, out, in); //this socket should not be closed
                } else {
                    JOptionPane.showMessageDialog(null, "Init Chat Failure!");
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | ClassNotFoundException | SignatureException | UnknownUserException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //input target name
        String targetName = JOptionPane.showInputDialog("Friend's name:");
        if (targetName == null) {
            return;
        }
        try {
            //wrap message
            PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
            MessageHeader messageHeader = new MessageHeader();
            messageHeader
                    .add("Service", "addFriend")
                    .add("Friend", targetName)
                    .add("Username", client.username);
            byte[] body = EncryptionUtils.encryptWithRSA("", serverPublicKey);
            MessageWrapper request = new MessageWrapper(messageHeader, body, serverPublicKey, client.getPrivateKey());
            System.out.println(request);

            //connect to server
            Socket socket = new Socket("127.0.0.1", 2333);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(request.getWrappedData());
            System.out.println("sending Friend request to server");
            //wait for response
            new Thread(new WaitForResponse(socket, serverPublicKey, targetName, this)).start();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException e1) {
            e1.printStackTrace();
        }
    }

    private class WaitForResponse implements Runnable {
        private Socket socket;
        private PublicKey serverPublicKey;
        private String targetName;
        private MiddlePanel middlePanel;

        public WaitForResponse(Socket socket, PublicKey serverPublicKey, String targetName, MiddlePanel middlePanel) {
            this.socket = socket;
            this.serverPublicKey = serverPublicKey;
            this.targetName = targetName;
            this.middlePanel = middlePanel;
        }

        @Override
        public void run() {
            try {
                //get response
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Object object = in.readObject();
                byte[] receivedBytes = (byte[]) object;
                System.out.println("Get response from server");
                MessageWrapper response = new MessageWrapper(receivedBytes, serverPublicKey, client.getPrivateKey());
                //parse response
                String status = response.getHeader().get("Response");
                if (status.equals("Accept")) {
                    JOptionPane.showMessageDialog(null, targetName + " accepted your request!");
                    byte[] encryptedBody = response.getBody();
                    String decrypedBody = EncryptionUtils.decryptWithRSA(encryptedBody, client.getPrivateKey());
                    User user = (User) CommonUtils.stringToObject(decrypedBody);
                    Friend friend = new Friend(user.getUsername(), user.getPort(), user.getIP().getHostAddress(), user.getPublicKey());
                    System.out.println("Friend's info is\n" + user);
                    client.addFriend(friend);
                    middlePanel.refresh();
                } else {
                    JOptionPane.showMessageDialog(null, targetName + " rejected your request!");
                }
                socket.close();
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | ClassNotFoundException | BadPaddingException | SignatureException | IllegalBlockSizeException | UnknownUserException e) {
                e.printStackTrace();
            }

        }
    }
}
