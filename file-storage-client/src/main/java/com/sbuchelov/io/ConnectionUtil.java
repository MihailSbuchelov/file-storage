package com.sbuchelov.io;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.Getter;
import java.io.IOException;
import java.net.Socket;

@Getter
public class ConnectionUtil {
    private Socket socket;
    private ObjectEncoderOutputStream dos;
    private ObjectDecoderInputStream dis;

    public ConnectionUtil(String host, int port) {
        try {
            socket = new Socket(host, port);
            dos = new ObjectEncoderOutputStream(socket.getOutputStream());
            dis = new ObjectDecoderInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
