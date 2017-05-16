package client;

import java.security.PublicKey;

/**
 * Created by mayezhou on 2017/5/15.
 */
public class Friend {
    public String name;
    public int port;
    public String ip;
    public PublicKey publicKey;

    public Friend(String name, int port, String ip, PublicKey publicKey) {
        this.name = name;
        this.port = port;
        this.ip = ip;
        this.publicKey = publicKey;
    }
}
