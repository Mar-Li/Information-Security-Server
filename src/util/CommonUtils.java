package util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class CommonUtils {

    public static byte[] objectToByteArray(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(object);
        byte[] result = bos.toByteArray();
        out.close();
        bos.close();
        return result;
    }

    public static String objectToString(Object object) throws IOException {
        return byteArrayToString(objectToByteArray(object));
    }

    public static Object byteArrayToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);
        return in.readObject();
    }

    public static Object stringToObject(String string) throws IOException, ClassNotFoundException {
        return byteArrayToObject(stringToByteArray(string));
    }

    public static String byteArrayToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public static byte[] stringToByteArray(String string) {
        return string.getBytes(StandardCharsets.ISO_8859_1);
    }

}
