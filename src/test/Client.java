package test;

import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class Client {
    public static void main(String[] args) {
        testRegister();
    }

    public static void testRegister() {
        try {
            Socket socket = new Socket("localhost", 2333);
            KeyPair keyPair = KeyGenerator.generateRSAKey("key/temp.pri", "key/temp.pub");
            PublicKey serverPublicKey = KeyGenerator.loadPublicKey("key/server.pub");
            MessageHeader header = new MessageHeader();
            header
                    .add("Service", "register")
                    .add("Username", "lfs");
            byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(keyPair.getPublic()), serverPublicKey);
            System.out.println(Arrays.toString(body));
            MessageWrapper request = new MessageWrapper(header, body, serverPublicKey, keyPair.getPrivate());
            System.out.println(request);
            socket.getOutputStream().write(request.getWrappedData());
            byte[] responseBytes = CommonUtils.readAllBytesFromInputStream(socket.getInputStream());
            MessageWrapper response = new MessageWrapper(responseBytes, serverPublicKey, keyPair.getPrivate());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
