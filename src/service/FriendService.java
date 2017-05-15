package service;

import data.User;
import data.UserData;
import exception.UnknownUserException;
import server.Server;
import util.CommonUtils;
import util.EncryptionUtils;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

/**
 * Created by lifengshuang on 14/05/2017.
 *
 * User register service
 *
 * ===== Request =====
 * Header includes fields:
 *   Service: addFriend
 *   Username: <username>
 *   Friend: <friend username>
 *
 * Body:
 *   Friend's information. See User.java
 *
 * ===== Response (success) =====
 * Header includes fields:
 *   Service: addFriend
 *   Response: accepted/rejected
 *
 *
 * ===========================================
 *
 *
 * Forward Request
 * ===== Request =====
 * Header includes fields:
 *   Service: friendRequest
 *   Username: <username> // the request is from from <username>
 *
 * Body:
 *   Request sender's information.
 *
 * ===== Response (success) =====
 * Header includes fields:
 *   Response: accepted/rejected
 *
 * Body:
 *   empty
 */
public class FriendService implements Service {
    @Override
    public byte[] handle(MessageWrapper request) throws Exception {
        String username = request.getHeader().get("Username");
        String friend = request.getHeader().get("Friend");
        if (UserData.getUser(username) == null) {
            throw new UnknownUserException(username);
        }
        if (UserData.getUser(friend) == null) {
            throw new UnknownUserException(friend);
        }
        PublicKey publicKey = UserData.getPublicKey(username);

//        User user = UserData.getUser(username);
//        user.addFriend(friend, false);

        String result = forwardRequest(username, friend);

        MessageHeader header = new MessageHeader();
        header
                .add("Service", "addFriend")
                .add("Response", result);
        byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(UserData.getUser(friend)), publicKey);
        MessageWrapper response = new MessageWrapper(header, body, publicKey, Server.SERVER_PRIVATE_KEY);
        return response.getWrappedData();
    }

    private String forwardRequest(String fromUser, String toUser) throws Exception {
        User user = UserData.getUser(fromUser);
        User friend = UserData.getUser(toUser);
        Socket socket = new Socket(friend.getIP(), friend.getPort());
        MessageHeader header = new MessageHeader();
        header
                .add("Service", "friendRequest")
                .add("Username", fromUser);
        byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(user), friend.getPublicKey());
        MessageWrapper request = new MessageWrapper(header, body, friend.getPublicKey(), Server.SERVER_PRIVATE_KEY);
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.writeObject(request.getWrappedData());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        byte[] data = (byte[])inputStream.readObject();
        MessageWrapper response = new MessageWrapper(data, friend.getPublicKey(), Server.SERVER_PRIVATE_KEY);
        return response.getHeader().get("Response");
    }
}
