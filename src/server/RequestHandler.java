package server;

import data.User;
import data.UserData;
import exception.ServiceNotFoundException;
import exception.UnknownUserException;
import service.RegisterService;
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

    @Override
    public void run() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            byte[] data = (byte[]) inputStream.readObject();

            MessageWrapper request = new MessageWrapper(data, null, Server.SERVER_PRIVATE_KEY);

            System.out.println("===== New Request =====");
            String username = request.getHeader().get("Username");
            if (UserData.getUser(username) == null) {
                UserData.addUser(username);
            }
            this.user = UserData.getUser(username);
            System.out.println("From user: " + username);
            System.out.println("Address: " + socket.getInetAddress() + " " + socket.getPort());
            UserData.setIPAndPort(username, socket.getInetAddress(), socket.getPort());
            String service = request.getHeader().get("Service");
            if (service == null) {
                throw new ServiceNotFoundException();
            }
            System.out.println("Service: " + service);

            byte[] result = null;
            switch (service) {
                case "register":
                    result = new RegisterService().handle(request);
                    break;
                default:
                    break;
            }
            if (result != null) {
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(result);
                socket.close();
            } else {
                throw new ServiceNotFoundException();
            }
        } catch (UnknownUserException e) {
            returnErrorMessage("User " + e.getUsername() + " is not found and the request is not a register request", e);
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
