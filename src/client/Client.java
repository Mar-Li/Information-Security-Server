package client;

import exception.UnknownUserException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by mayezhou on 2017/5/11.
 */
public class Client {
    public static int portCount = 2000;
    //store session key in memory
    private KeyStore keyStore;
    private char[] pwdToKey;
    private KeyStore.ProtectionParameter protectionParameter;
    public String username;
    private List<Friend> friends;
    private final KeyPair keyPair;
    private ServerSocket listenSocket;
    private int port;

    public Client(String username, char[] password, KeyPair keyPair, int port) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.username = username;
        this.pwdToKey = password;
        keyStore = KeyStore.getInstance("JKS");//default most proper in environment
        // initialize keystore
        keyStore.load(null, pwdToKey);
        protectionParameter = new KeyStore.PasswordProtection(pwdToKey);
        this.keyPair = keyPair;
        this.port = port;
        listenSocket = new ServerSocket(port);
        System.out.println("Start Client socket " + username + " in port " + port);
        new Thread(new ClientListenRunnable(listenSocket, this)).start();
        portCount++;
    }

    public int getPort() {
        return port;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public Key getSecretKey(String keyAlias) throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        KeyStore.SecretKeyEntry keyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyAlias, protectionParameter);
        return keyEntry.getSecretKey();
    }

    public void saveSessionKey(String keyAlias, SecretKey key) throws KeyStoreException {
        KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(key);
        keyStore.setEntry(keyAlias, keyEntry, protectionParameter);
    }

    public String[][] getFriendList() {
        if (friends.size() == 0) {
            return null;
        }
        String[][] result = new String[friends.size()][1];
        for (int i = 0; i < friends.size(); i++) {
            result[i][0] = friends.get(i).name;
        }
        return result;
    }

    public Friend getFriend(int i) {
        return friends.get(i);
    }

    public void addFriend(Friend friend) {
        friends.add(friend);
    }

    public PublicKey getFriendPublicKey(String name) throws UnknownUserException {
        for (Friend friend :
                friends) {
            if (friend.name.equals(name)) {
                return friend.publicKey;
            }
        }
        throw new UnknownUserException(name);
    }
}
