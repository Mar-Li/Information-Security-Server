package data;

import util.KeyGenerator;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lifengshuang on 11/05/2017.
 */
public class UserData {

    // just store in memory.
    private static Map<String, User> userData = new HashMap<>();

    public static KeyPair registerUser(String username) throws IOException, NoSuchAlgorithmException {
        if (userData.get(username) != null) {
            return null;
        }
        KeyPair keyPair = KeyGenerator.generateRSAKey("key/user" + username + ".pri", "key/user" + username + ".pub");
        userData.put(username, new User(keyPair.getPublic()));
        return keyPair;
    }

    public static PublicKey getPublicKey(String username) {
        return userData.get(username) == null ? null : userData.get(username).publicKey;
    }

    public static String getIP(String username) {
        return userData.get(username) == null ? null : userData.get(username).IP;
    }

    public static String getPort(String username) {
        return userData.get(username) == null ? null : userData.get(username).port;
    }

    private static class User {
        PublicKey publicKey;
        String IP;
        String port;

        public User(PublicKey publicKey) {
            this.publicKey = publicKey;
        }
    }
}
