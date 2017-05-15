package GUI;

import client.Client;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class MiddlePanel extends JPanel{
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
        if (client != null && client.getFriendList() != null) {
            createTable();
        }
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private void refresh() {
        //TODO: after add new friend
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
                frame.endPanel.setTarget(client.getFriend(index));
                frame.cardLayout.next(frame.mainPanel);
            }
        });
    }
}
