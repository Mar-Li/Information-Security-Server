package data;

import exception.UnknownUserException;
import util.KeyGenerator;

import java.io.IOException;
import java.net.InetAddress;
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

    public static void addUser(String username) {
        userData.put(username, new User(username));
    }

    public static User getUser(String username) {
        return userData.get(username);
    }

    public static void setPublicKey(String username, PublicKey publicKey) throws UnknownUserException {
        User user = userData.get(username);
        if (user == null) {
            throw new UnknownUserException(username);
        } else {
            user.setPublicKey(publicKey);
        }
    }

    public static void setIPAndPort(String username, InetAddress IP, Integer port) throws UnknownUserException {
        User user = userData.get(username);
        if (user == null) {
            throw new UnknownUserException(username);
        } else {
            user.setIP(IP);
            user.setPort(port);
        }
    }

    public static PublicKey getPublicKey(String username) {
        return userData.get(username) == null ? null : userData.get(username).getPublicKey();
    }

    public static InetAddress getIP(String username) {
        return userData.get(username) == null ? null : userData.get(username).getIP();
    }

    public static Integer getPort(String username) {
        return userData.get(username) == null ? null : userData.get(username).getPort();
    }

}
