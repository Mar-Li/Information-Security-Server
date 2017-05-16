package GUI;

import client.Client;
import server.Server;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by mayezhou on 16/6/1.
 */
public class MyFrame extends JFrame{
    public CardLayout cardLayout;
    public JPanel mainPanel;
    public StartPanel startPanel;
    public MiddlePanel middlePanel;
    public EndPanel endPanel;

    public MyFrame() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel();
        startPanel = new StartPanel(this);
        middlePanel = new MiddlePanel(this);
        endPanel = new EndPanel(this);
        Container container = this.getContentPane();
        mainPanel.setLayout(cardLayout);
        mainPanel.add(startPanel, "first");
        mainPanel.add(middlePanel, "second");
        mainPanel.add(endPanel, "third");
        container.add(mainPanel);
        this.setTitle("IM");
        this.setSize(325, 550);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(true);
    }

    public static void main(String[] args) {
        new MyFrame();
    }

    public void setClient(Client client) {
        this.middlePanel.setClient(client);
        this.endPanel.setClient(client);
    }
}
