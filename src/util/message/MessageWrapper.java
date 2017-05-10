package util.message;

import util.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.util.Arrays;

/**
 * The public key is the receiver's public key.
 * Header and body are both encrypted message.
 */
public class MessageWrapper {

    private MessageHeader header;
    private byte[] body;
    private byte[] wrappedData;

    public MessageWrapper(MessageHeader header, byte[] body, PublicKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.header = header;
        this.body = body;
        byte[] encryptedHeader = EncryptionUtils.encryptWithRSA(header.toString(), key);
        byte[] lengthBlock = EncryptionUtils.encryptWithRSA(String.valueOf(encryptedHeader.length), key);
        wrappedData = new byte[lengthBlock.length + encryptedHeader.length + body.length];
        System.arraycopy(lengthBlock, 0, wrappedData, 0, lengthBlock.length);
        System.arraycopy(encryptedHeader, 0, wrappedData, lengthBlock.length, encryptedHeader.length);
        System.arraycopy(body, 0, wrappedData, lengthBlock.length + encryptedHeader.length, body.length);
    }

    public MessageWrapper(byte[] wrappedData, PrivateKey key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.wrappedData = wrappedData;
        byte[] lengthBlock = Arrays.copyOfRange(wrappedData, 0, EncryptionUtils.BYTE_BLOCK_SIZE);
        int headerLength = Integer.parseInt(EncryptionUtils.decryptWithRSA(lengthBlock, key));
        byte[] encryptedHeader = Arrays.copyOfRange(wrappedData, EncryptionUtils.BYTE_BLOCK_SIZE, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength);
        this.header = MessageHeader.parse(EncryptionUtils.decryptWithRSA(encryptedHeader, key));
        this.body = Arrays.copyOfRange(wrappedData, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength, wrappedData.length);
    }

    public MessageHeader getHeader() {
        return header;
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getWrappedData() {
        return wrappedData;
    }

    @Override
    public String toString() {
        return "The header is " + this.header + "\nThe body is " + Arrays.toString(this.body);
    }
}
