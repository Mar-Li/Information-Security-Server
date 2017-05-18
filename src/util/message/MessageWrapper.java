package util.message;

import client.Client;
import data.UserData;
import exception.NotFriendException;
import exception.ServiceNotFoundException;
import exception.UnknownUserException;
import util.CommonUtils;
import util.EncryptionUtils;
import util.KeyGenerator;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * The public key is the receiver's public key.
 * Header and body are both encrypted message.
 */
public class MessageWrapper {
    private MessageHeader header;
    private byte[] body;
    private byte[] wrappedData;

    public MessageWrapper(MessageHeader header, byte[] body, PublicKey publicKey, PrivateKey privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        this.header = header;
        this.body = body;
        byte[] encryptedHeader = EncryptionUtils.encryptWithRSA(header.toString(), publicKey);
        byte[] lengthBlock = EncryptionUtils.encryptWithRSA(String.valueOf(encryptedHeader.length), publicKey);
        byte[] dataWithoutSignature = new byte[lengthBlock.length + encryptedHeader.length + body.length];
        System.arraycopy(lengthBlock, 0, dataWithoutSignature, 0, lengthBlock.length);
        System.arraycopy(encryptedHeader, 0, dataWithoutSignature, lengthBlock.length, encryptedHeader.length);
        System.arraycopy(body, 0, dataWithoutSignature, lengthBlock.length + encryptedHeader.length, body.length);
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(dataWithoutSignature);
        byte[] signature = EncryptionUtils.encryptWithRSA(CommonUtils.byteArrayToString(hash), privateKey);
        wrappedData = new byte[dataWithoutSignature.length + EncryptionUtils.BYTE_BLOCK_SIZE];
        System.arraycopy(dataWithoutSignature, 0, wrappedData, 0, dataWithoutSignature.length);
        System.arraycopy(signature, 0, wrappedData, dataWithoutSignature.length, EncryptionUtils.BYTE_BLOCK_SIZE);
    }

    //For Client listening socket
    public MessageWrapper(byte[] wrappedData, PrivateKey privateKey, Client client) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, UnknownUserException, ServiceNotFoundException, SignatureException, NotFriendException, IOException, InvalidKeySpecException {
        this.wrappedData = wrappedData;
        byte[] dataWithoutSignature = Arrays.copyOfRange(wrappedData, 0, wrappedData.length - EncryptionUtils.BYTE_BLOCK_SIZE);
        byte[] lengthBlock = Arrays.copyOfRange(dataWithoutSignature, 0, EncryptionUtils.BYTE_BLOCK_SIZE);
        int headerLength = Integer.parseInt(EncryptionUtils.decryptWithRSA(lengthBlock, privateKey));
        byte[] encryptedHeader = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength);
        this.header = MessageHeader.parse(EncryptionUtils.decryptWithRSA(encryptedHeader, privateKey));
        String username = this.header.get("Username");
        if (username == null) {
            throw new UnknownUserException(null);
        }
        String service = this.header.get("Service");
        PublicKey key;
        switch (service) {
            case "friendRequest":
                key = KeyGenerator.loadPublicKey("key/server.pub");
                break;
            case "InitChat":
                key = client.getFriendPublicKey(username);
                break;
            default:
                throw new ServiceNotFoundException();
        }
        if (key != null) {
            byte[] signatureBlock = Arrays.copyOfRange(wrappedData, dataWithoutSignature.length, wrappedData.length);
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(dataWithoutSignature);
            byte[] hashFromSignature = CommonUtils.stringToByteArray(EncryptionUtils.decryptWithRSA(signatureBlock, key));
            if (!Arrays.equals(hash, hashFromSignature)) {
                throw new SignatureException();
            }
        } else {
            throw new NotFriendException(username + " is not " + client.username + "'s friend!");
        }
        this.body = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength, dataWithoutSignature.length);
    }

    //For Client listening socket
    public MessageWrapper(byte[] wrappedData, PublicKey publicKey, SecretKey sessionKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException {
        this.wrappedData = wrappedData;
        byte[] dataWithoutSignature = Arrays.copyOfRange(wrappedData, 0, wrappedData.length - EncryptionUtils.BYTE_BLOCK_SIZE);
        byte[] lengthBlock = Arrays.copyOfRange(dataWithoutSignature, 0, EncryptionUtils.BYTE_BLOCK_SIZE);
        int headerLength = Integer.parseInt(EncryptionUtils.symmetricDecrypt(lengthBlock, sessionKey));
        byte[] encryptedHeader = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength);
        this.header = MessageHeader.parse(EncryptionUtils.symmetricDecrypt(encryptedHeader, sessionKey));
        this.body = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength, dataWithoutSignature.length);
        byte[] signatureBlock = Arrays.copyOfRange(wrappedData, dataWithoutSignature.length, wrappedData.length);
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(dataWithoutSignature);
        byte[] hashFromSignature = CommonUtils.stringToByteArray(EncryptionUtils.decryptWithRSA(signatureBlock, publicKey));
        if (!Arrays.equals(hash, hashFromSignature)) {
            throw new SignatureException();
        }
    }

    //For Server & Client
    public MessageWrapper(byte[] wrappedData, PublicKey publicKey, PrivateKey privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, SignatureException, UnknownUserException {
        this.wrappedData = wrappedData;
        byte[] dataWithoutSignature = Arrays.copyOfRange(wrappedData, 0, wrappedData.length - EncryptionUtils.BYTE_BLOCK_SIZE);
        // For client
        if (publicKey != null) {
            byte[] signatureBlock = Arrays.copyOfRange(wrappedData, dataWithoutSignature.length, wrappedData.length);
            byte[] hash = MessageDigest.getInstance("SHA-1").digest(dataWithoutSignature);
            byte[] hashFromSignature = CommonUtils.stringToByteArray(EncryptionUtils.decryptWithRSA(signatureBlock, publicKey));
            if (!Arrays.equals(hash, hashFromSignature)) {
                throw new SignatureException();
            }
        }
        byte[] lengthBlock = Arrays.copyOfRange(dataWithoutSignature, 0, EncryptionUtils.BYTE_BLOCK_SIZE);
        int headerLength = Integer.parseInt(EncryptionUtils.decryptWithRSA(lengthBlock, privateKey));
        byte[] encryptedHeader = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength);
        this.header = MessageHeader.parse(EncryptionUtils.decryptWithRSA(encryptedHeader, privateKey));
        // For server
        if (publicKey == null) {
            String username = this.header.get("Username");
            if (username == null) {
                throw new UnknownUserException(null);
            } else if(!header.get("Service").equals("register")) {
                PublicKey key = UserData.getPublicKey(username);
                if (key != null) {
                    byte[] signatureBlock = Arrays.copyOfRange(wrappedData, dataWithoutSignature.length, wrappedData.length);
                    byte[] hash = MessageDigest.getInstance("SHA-1").digest(dataWithoutSignature);
                    byte[] hashFromSignature = CommonUtils.stringToByteArray(EncryptionUtils.decryptWithRSA(signatureBlock, key));
                    if (!Arrays.equals(hash, hashFromSignature)) {
                        throw new SignatureException();
                    }
                }
            }
        }
        this.body = Arrays.copyOfRange(dataWithoutSignature, EncryptionUtils.BYTE_BLOCK_SIZE + headerLength, dataWithoutSignature.length);
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
        return "=== Header ===\n" + this.header + "=== Body ===\nLength: " + this.body.length + "\nData: " + Arrays.toString(this.body) + "\n";
    }
}
