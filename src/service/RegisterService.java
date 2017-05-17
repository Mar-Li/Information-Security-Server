package service;

import data.UserData;
import exception.UnknownUserException;
import server.Server;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * Created by lifengshuang on 11/05/2017.
 *
 * User register service
 *
 * ===== Request =====
 * Header includes fields:
 *   Service: register
 *   Username: <username>
 *   Port: <port>
 *
 * Body:
 *   User's public key, encrypted by server's public key
 *
 * ===== Response =====
 * Header includes fields:
 *   Service: register
 *
 * Body:
 *   User's registered KeyPair
 */
public class RegisterService implements Service {

    @Override
    public byte[] handle(MessageWrapper request) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, ClassNotFoundException, UnknownUserException {

        String username = request.getHeader().get("Username");
        String port = request.getHeader().get("Port");
        KeyPair keyPair = KeyGenerator.generateRSAKey("key/user/" + username + ".pri", "key/user/" + username + ".pub");
        String requestBody = EncryptionUtils.decryptWithRSA(request.getBody(), Server.SERVER_PRIVATE_KEY);
        PublicKey publicKey = (PublicKey)CommonUtils.byteArrayToObject(CommonUtils.stringToByteArray(requestBody));
        try {
            UserData.addUser(username);
            UserData.setPublicKey(username, keyPair.getPublic());
            UserData.setPort(username, Integer.parseInt(port));
            byte[] responseBody = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(keyPair), publicKey);
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "register")
                    .add("Status", "200");
            return (new MessageWrapper(header, responseBody, publicKey, Server.SERVER_PRIVATE_KEY)).getWrappedData();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(null, "Username is already used!");
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "register")
                    .add("Status", "Error")
                    .add("ErrorType", "Repetitive username");
            byte[] responseBody = EncryptionUtils.encryptWithRSA("", publicKey);
            return (new MessageWrapper(header, responseBody, publicKey, Server.SERVER_PRIVATE_KEY)).getWrappedData();
        }

    }

}
