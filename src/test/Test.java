package test;

import data.UserData;
import exception.UnknownUserException;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.ISO_8859_1;


/**
 * Created by lifengshuang on 10/05/2017.
 */

public class Test {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, SignatureException, UnknownUserException {
        testMessageWrapper();
//        System.out.println(testMD5().length);
//        testByteStringConversion();
//        testUserData();
    }


    private static byte[] testMD5() throws NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, IOException, SignatureException, UnknownUserException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        return digest.digest(testMessageWrapper());
    }

    private static byte[] testMessageWrapper() throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException, InvalidKeySpecException, SignatureException, UnknownUserException {

        // RSA Key generation
        KeyGenerator.generateRSAKey("key/test/test.pri", "key/test/test.pub");

        // load keys
        PrivateKey privateKey = KeyGenerator.loadPrivateKey("key/test/test.pri");
        PublicKey publicKey = KeyGenerator.loadPublicKey("key/test/test.pub");

        // Test data
        String testText = CommonUtils.objectToString(publicKey);
        byte[] messageBody = EncryptionUtils.encryptWithRSA(testText, privateKey);
//        byte[] messageBody = new byte[0];

        // Message Header.
        // Header with a very long message is also tested.
        MessageHeader header = new MessageHeader();
        header
                .add("Service", "register")
                .add("Body Encryption", "Private Key")
                .add("Name", "Alice");

        // logs before message wrapping
        System.out.println("==== Before Message wrapping ====");
        System.out.println(header);
        System.out.println("Plaintext: " + testText);
        System.out.println("Encrypted text (body):" + Arrays.toString(messageBody));

        // Wrap header and body to bytes.
        MessageWrapper wrapper1 = new MessageWrapper(header, messageBody, publicKey, privateKey);

        // Use Socket to send this message. It's fully encrypted.
        // Suppose client sends this message to server.
        byte[] wrappedMessage = wrapper1.getWrappedData();

        // Suppose server has received the wrapped message.
        // Decode the message to header and body.
        MessageWrapper wrapper2 = new MessageWrapper(wrappedMessage, publicKey, privateKey);

        System.out.println("\n\n==== After Message wrapping ====");
        System.out.println(wrapper2);

        String decryptedText = EncryptionUtils.decryptWithRSA(wrapper2.getBody(), publicKey);
        System.out.println("Decrypted Text: " + decryptedText);

        return wrappedMessage;
    }

    private static void testByteStringConversion() {
        byte[] bytes = new byte[]{123, 21, 32, 41, 54, 7, 86, -10, -11};
        System.out.println(Arrays.toString(bytes));
        String s = new String(bytes, ISO_8859_1);
        System.out.println(s);
        byte[] b = s.getBytes(StandardCharsets.ISO_8859_1);
        System.out.println(Arrays.toString(b));
    }
}
