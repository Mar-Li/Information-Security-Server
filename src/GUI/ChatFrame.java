package GUI;

import client.Client;
import client.Friend;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by mayezhou on 2017/5/17.
 */
public class ChatFrame extends JFrame {
    private EndPanel endPanel;

    public ChatFrame(Client client, Friend friend, Socket socket, SecretKey sessionKey, ObjectOutputStream out, ObjectInputStream in) throws HeadlessException {
        endPanel = new EndPanel(client, friend, socket, sessionKey, out, in);
        Container container = this.getContentPane();
        container.add(endPanel);
        this.setTitle("Chatting with " + friend.name);
        this.setSize(325, 550);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                endPanel.close();
            }
        });
    }
}
