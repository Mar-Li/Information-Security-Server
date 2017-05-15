package data;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class User implements Serializable {
    private String username;
    private PublicKey publicKey;
    private InetAddress IP;
    private Integer port;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public InetAddress getIP() {
        return IP;
    }

    public int getPort() {
        return port;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setIP(InetAddress IP) {
        this.IP = IP;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Username: " + username + ", IP:" + IP.getHostAddress() + ", port: " + port;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(username);
        out.writeObject(publicKey);
        out.writeObject(IP);
        out.writeObject(port);
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        this.username = (String) in.readObject();
        this.publicKey = (PublicKey) in.readObject();
        this.IP = (InetAddress) in.readObject();
        this.port = (Integer) in.readObject();
    }

    private void readObjectNoData()
            throws ObjectStreamException {

    }
}
