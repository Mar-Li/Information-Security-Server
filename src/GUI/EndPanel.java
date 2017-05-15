package GUI;

import client.Client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class EndPanel extends JPanel{
    private Client client;
    private MyFrame frame;
    private Client target;
    private JButton button;
    private JTextField textField;
    private JTextArea textArea;

    public EndPanel(MyFrame frame) {
        this.frame = frame;
        button = new JButton("SEND");
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
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setTarget(Client target) {
        this.target = target;
    }
}
