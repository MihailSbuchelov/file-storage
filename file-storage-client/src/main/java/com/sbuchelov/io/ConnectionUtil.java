package com.sbuchelov.io;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class ConnectionUtil {
    private Socket socket;
    private ObjectEncoderOutputStream dos;
    private ObjectDecoderInputStream dis;

    public ConnectionUtil() {
        try {
            socket = new Socket("localhost", 8189);
            dos = new ObjectEncoderOutputStream(socket.getOutputStream());
            dis = new ObjectDecoderInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectEncoderOutputStream getDos() {
        return dos;
    }

    public ObjectDecoderInputStream getDis() {
        return dis;
    }
}
