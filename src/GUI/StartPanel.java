package GUI;

import client.Client;
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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
public class StartPanel extends JPanel implements ActionListener{
    protected JTextField usernameComp = new JTextField();
    protected JLabel label1 = new JLabel("Username:");
    protected JLabel label2 = new JLabel("Password:");
    protected JPasswordField passwordField = new JPasswordField();
    protected JButton registerBtn = new JButton("REGISTER");
    private MyFrame frame;

    public StartPanel(MyFrame frame) {
        this.frame = frame;
        setLayout(new GridLayout(5, 1));
        add(label1);
        add(usernameComp);
        add(label2);
        add(passwordField);
        add(registerBtn);
        registerBtn.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //get input
        String username = usernameComp.getText();
        char[] password = passwordField.getPassword();
        if (e.getSource() == registerBtn) {
            try {
                //wrap message
                KeyPair initKeyPair = KeyGenerator.generateRSAKey();
                int clientPort = Client.portCount;
                PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
                MessageHeader messageHeader = new MessageHeader();
                messageHeader
                        .add("Service", "register")
                        .add("Username", username)
                        .add("Port", String.valueOf(clientPort));
                byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(initKeyPair.getPublic()), serverPublicKey);
                MessageWrapper request = new MessageWrapper(messageHeader, body, serverPublicKey, initKeyPair.getPrivate());
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
                MessageWrapper response = new MessageWrapper(receivedBytes, serverPublicKey, initKeyPair.getPrivate());
                //parse response
                String status = response.getHeader().get("Status");
                if (status.equals("200")) {
                    System.out.println("register success, get correct response from server");
                    byte[] encryptedBody = response.getBody();
                    String decrypedBody = EncryptionUtils.decryptWithRSA(encryptedBody, initKeyPair.getPrivate());
                    //get true RSA keypair
                    KeyPair myKeyPair = (KeyPair) CommonUtils.byteArrayToObject(CommonUtils.stringToByteArray(decrypedBody));
                    Client client = new Client(username, password, myKeyPair, clientPort, frame);
                    frame.setClient(client);
                    System.out.println("register done, create client " + client.username);
                    //send listening port to server
                    MessageHeader header = new MessageHeader();
                    header
                            .add("Service", "port")
                            .add("Username", client.username);
                    byte[] body2 = EncryptionUtils.encryptWithRSA(String.valueOf(client.getPort()), serverPublicKey);
                    MessageWrapper mw = new MessageWrapper(header, body2, serverPublicKey, client.getPrivateKey());
                    out.writeObject(mw.getWrappedData());
                    System.out.println("send listening port" + client.getPort() + " to server");
                    socket.close();
                    frame.cardLayout.next(frame.mainPanel);
                } else if (status.equals("Error")) {
                    JOptionPane.showMessageDialog(null, response.getHeader().get("ErrorType"));
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | CertificateException | SignatureException | UnknownUserException | KeyStoreException | ClassNotFoundException e1) {
                e1.printStackTrace();
            }
        }
    }
}