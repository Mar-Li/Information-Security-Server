package GUI;

import client.Client;
import client.Friend;
import exception.UnknownUserException;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
import java.security.*;
import java.security.cert.CertificateException;
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
        data = client.getFriendList();
        tableModel = new DefaultTableModel(data, title);
        table.setModel(tableModel);
        this.repaint();
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
                int index = table.rowAtPoint(e.getPoint());
                frame.endPanel.setFriend(client.getFriend(index));
                frame.endPanel.connect();
                frame.cardLayout.next(frame.mainPanel);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //input targer name
        String targetName = JOptionPane.showInputDialog("Friend's name:");
        if (targetName == null) {
            return;
        }
        try {
            //wrap message
            PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
            MessageHeader messageHeader = new MessageHeader();
            messageHeader
                    .add("Service", "register")
                    .add("Username", client.username);
            byte[] body = EncryptionUtils.encryptWithRSA(targetName, serverPublicKey);
            MessageWrapper request = new MessageWrapper(messageHeader, body, serverPublicKey, client.getPrivateKey());
            System.out.println(request);

            //connect to server
            Socket socket = new Socket("127.0.0.1", 2333);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(request.getWrappedData());
            System.out.println("sending request to server");

            //get response
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Object object = in.readObject();
            byte[] receivedBytes = (byte[]) object;
            System.out.println("Get response from server");
            MessageWrapper response = new MessageWrapper(receivedBytes, serverPublicKey, client.getPrivateKey());
            //parse response
            String status = response.getHeader().get("Status");
            if (status.equals("200")) {//TODO
                System.out.println("Befriend success.");
                int port = Integer.parseInt(response.getHeader().get("Port"));
                String ip = response.getHeader().get("Ip");
                byte[] encryptedBody = response.getBody();
                String decrypedBody = EncryptionUtils.decryptWithRSA(encryptedBody, client.getPrivateKey());
                PublicKey publicKey = (PublicKey) CommonUtils.stringToObject(decrypedBody);
                Friend friend = new Friend(targetName, port, ip, publicKey);
                client.addFriend(friend);
                refresh();
            }
            socket.close();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | SignatureException | UnknownUserException | ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
