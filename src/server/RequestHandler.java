package server;

import data.User;
import data.UserData;
import exception.ServiceNotFoundException;
import exception.UnknownUserException;
import service.FriendService;
import service.RegisterService;
import service.UserService;
import util.message.MessageHeader;
import util.message.MessageWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.SignatureException;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class RequestHandler implements Runnable {

    private Socket socket;
    private User user;

    public RequestHandler(Socket socket) {
        this.socket = socket;
    }

    private byte[] handleRequest(MessageWrapper request, String service) throws Exception {
        byte[] response = null;
        switch (service) {
            // Register a new user
            case "register":
                response = new RegisterService().handle(request);
                break;
            // Get all users' information
            // There's only a few(two) users in this project, so server don't support querying one user.
            case "getAllUsers":
                response = new UserService().handle(request);
                break;
            case "addFriend":
                response = new FriendService().handle(request);
                break;
            default:
                break;
        }
        return response;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] data = (byte[]) inputStream.readObject();

            MessageWrapper request = new MessageWrapper(data, null, Server.SERVER_PRIVATE_KEY);

            System.out.println("===== New Request =====");
            String username = request.getHeader().get("Username");
            System.out.println("From user: " + username);
            System.out.println("Address: " + socket.getInetAddress() + " " + socket.getPort());

            String service = request.getHeader().get("Service");

            if (service == null) {
                throw new ServiceNotFoundException();
            }
            if (!service.equals("register")) {
                if (UserData.getUser(username) == null) {
                    throw new UnknownUserException(username);
                } else {
                    this.user = UserData.getUser(username);
                    if (this.user == null) {
                        throw new UnknownUserException(username);
                    }
                }
            }
            String friend = request.getHeader().get("Friend");
            if (friend != null && UserData.getUser(friend) == null) {
                throw new UnknownUserException(friend);
            }

            System.out.println("Service: " + service);

            byte[] response = handleRequest(request, service);

            if (response != null) {
                UserData.setIP(username, socket.getInetAddress());
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(response);
                System.out.println("Respond to client" + username);
                socket.close();
            } else {
                throw new ServiceNotFoundException();
            }
        } catch (UnknownUserException e) {
            returnErrorMessage("User " + e.getUsername() + " is not found.", e);
        } catch (SignatureException e) {
            returnErrorMessage("The signature validation failed.", e);
        } catch (ServiceNotFoundException e) {
            returnErrorMessage("Your requested service is not found.", e);
        } catch (Exception e) {
            returnErrorMessage("Error!", e);
        }
    }

    private void returnErrorMessage(String error, Exception e) {
        e.printStackTrace();
        try {
            if (user != null && user.getPublicKey() != null) {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                MessageHeader header = new MessageHeader();
                header
                        .add("Error", error)
                        .add("Status", "Error")
                        .add("ErrorType", e.getClass().getName());
                MessageWrapper response = new MessageWrapper(header, new byte[0], user.getPublicKey(), Server.SERVER_PRIVATE_KEY);
                outputStream.writeObject(response.getWrappedData());
                socket.close();
            } else {
                socket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }
}
