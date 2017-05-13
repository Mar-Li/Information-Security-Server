package service;

import data.User;
import data.UserData;
import server.Server;
import util.CommonUtils;
import util.EncryptionUtils;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.security.PublicKey;

/**
 * Created by lifengshuang on 13/05/2017.
 *
 * User service
 *
 * ===== Request =====
 * Header includes fields:
 *   Service: getAllUsers
 *   Username: <username>
 *
 * Body:
 *   new byte[0]
 *
 * ===== Response =====
 * Header includes fields:
 *   Service: getAllUsers
 *
 * Body:
 *   Array of User instances
 */
public class UserService implements Service {
    @Override
    public byte[] handle(MessageWrapper request) throws Exception {
        MessageHeader header = new MessageHeader();
        header.add("Service", "getAllUsers");
        PublicKey publicKey = UserData.getPublicKey(request.getHeader().get("Username"));
        User[] users = UserData.getAllUsers();
        byte[] body = EncryptionUtils.encryptWithRSA(CommonUtils.objectToString(users), publicKey);
        return new MessageWrapper(header, body, publicKey, Server.SERVER_PRIVATE_KEY).getWrappedData();
    }
}
