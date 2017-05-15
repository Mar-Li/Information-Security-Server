package client;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * Created by mayezhou on 2017/5/11.
 */
public class Client {
    //store session key in memory
    private KeyStore keyStore;
    private char[] pwdToKey;
    private KeyStore.ProtectionParameter protectionParameter;
    public String username;
    private List<Client> friends;
    private final KeyPair keyPair;

    public Client(String username, char[] password, KeyPair keyPair) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.username = username;
        this.pwdToKey = password;
        keyStore = KeyStore.getInstance("JKS");//default most proper in environment
        // initialize keystore
        keyStore.load(null, pwdToKey);
        protectionParameter = new KeyStore.PasswordProtection(pwdToKey);
        this.keyPair = keyPair;
    }

    public Client(KeyStore keyStore, char[] pwdToKey, KeyStore.ProtectionParameter protectionParameter, String username, List<Client> friends, KeyPair keyPair) {
        this.keyStore = keyStore;
        this.pwdToKey = pwdToKey;
        this.protectionParameter = protectionParameter;
        this.username = username;
        this.friends = friends;
        this.keyPair = keyPair;
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
            result[i][0] = friends.get(i).username;
        }
        return result;
    }

    public Client getFriend(int i) {
        return friends.get(i);
    }
}
