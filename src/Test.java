import util.EncryptionUtils;
import util.KeyGenerator;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;


/**
 * Created by lifengshuang on 10/05/2017.
 */
public class Test {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException {

        // RSA Key generation
        KeyGenerator.generateRSAKey("test.pri", "test.pub");

        // load keys
        PrivateKey privateKey = KeyGenerator.loadPrivateKey("test.pri");
        PublicKey publicKey = KeyGenerator.loadPublicKey("test.pub");

        // Test data
        String testText = "abcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefghabcdefgh";
        byte[] messageBody = EncryptionUtils.encryptWithRSA(testText, privateKey);

        // Message Header.
        // Header with a very long message is also tested.
        MessageHeader header = new MessageHeader();
        header
                .add("Request", "test")
                .add("Body Encryption", "Private Key")
                .add("Name", "Alice");

        // logs before message wrapping
        System.out.println("==== Before Message wrapping ====");
        System.out.println(header);
        System.out.println("Plaintext: " + testText);
        System.out.println("Encrypted text (body):" + Arrays.toString(messageBody));

        // Wrap header and body to bytes.
        MessageWrapper wrapper1 = new MessageWrapper(header, messageBody, publicKey);

        // Use Socket to send this message. It's fully encrypted.
        // Suppose client sends this message to server.
        byte[] wrappedMessage = wrapper1.getWrappedData();

        // Suppose server has received the wrapped message.
        // Decode the message to header and body.
        MessageWrapper wrapper2 = new MessageWrapper(wrappedMessage, privateKey);

        System.out.println("\n\n==== After Message wrapping ====");
        System.out.println(wrapper2);

        String decryptedText = EncryptionUtils.decryptWithRSA(wrapper2.getBody(), publicKey);
        System.out.println("Decrypted Text: " + decryptedText);
    }
}
