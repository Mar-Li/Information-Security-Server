package service;

import server.Server;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
 *
 * Body:
 *   User's public key, encrypted by server's public key
 *
 * ===== Response =====
 * Header includes fields:
 *   Service: register
 *   Status: 200 if success, 400 if fail.
 *
 * Body:
 *   User's registered KeyPair
 */
public class RegisterService implements Service {

    @Override
    public byte[] handle(MessageWrapper request) throws IOException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, ClassNotFoundException {
        String username = request.getHeader().get("Username");
        KeyPair keyPair = KeyGenerator.generateRSAKey("key/user/" + username + ".pri", "key/user/" + username + ".pub");
        String requestBody = EncryptionUtils.decryptWithRSA(request.getBody(), Server.SERVER_PRIVATE_KEY);
        PublicKey publicKey = (PublicKey)CommonUtils.byteArrayToObject(CommonUtils.stringToByteArray(requestBody));
        byte[] responseBody = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(keyPair), publicKey);
        MessageHeader header = new MessageHeader();
        header
                .add("Service", "register")
                .add("Status", "200");
        return (new MessageWrapper(header, responseBody, publicKey, Server.SERVER_PRIVATE_KEY)).getWrappedData();
    }

}
