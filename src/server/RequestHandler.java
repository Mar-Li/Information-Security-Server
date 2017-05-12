package server;

import service.RegisterService;
import service.Service;
import util.CommonUtils;
import util.message.MessageWrapper;

import java.io.*;
import java.net.Socket;

/**
 * Created by lifengshuang on 13/05/2017.
 */
public class RequestHandler implements Runnable {

    private Socket socket;

    public RequestHandler(Socket socket) {
        this.socket = socket;
//        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            byte[] data = CommonUtils.readAllBytesFromInputStream(socket.getInputStream());
            MessageWrapper request = new MessageWrapper(data, null, Server.SERVER_PRIVATE_KEY);
            byte[] result = null;
            Service service;
            switch (request.getHeader().get("Service")) {
                case "register":
                    service = new RegisterService();
                    result = service.handle(request);
                    break;
                default:
                    break;
            }
            if (result != null) {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(result);
                socket.close();
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
