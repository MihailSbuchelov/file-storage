package com.sbuchelov.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class NioServer {
    private Path root;
    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer buffer;


    public NioServer() throws Exception {
        root = Path.of("C:\\Users\\Miha_admin\\Desktop\\file-storage\\root");
        buffer = ByteBuffer.allocate(256);
        server = ServerSocketChannel.open(); // accept -> SocketChannel
        server.bind(new InetSocketAddress(8189));
        selector = Selector.open();
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Сервер запущен...");
        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey key) throws Exception {

        SocketChannel channel = (SocketChannel) key.channel();

        StringBuilder sb = new StringBuilder();

        while (true) {
            int read = channel.read(buffer);
            if (read == -1) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }

        String commandFromClient = sb.toString().trim();
        String stringResult = "";
        Path path;
        if (commandFromClient.equals("ls")) {
            stringResult = fileOrDirectoryTree(root);
        }
        if (commandFromClient.startsWith("ls ")) {
            stringResult = fileOrDirectoryTree(Path.of(commandFromClient.substring(3)));
        }
        if (commandFromClient.startsWith("cd ")) {
            path = Path.of(commandFromClient.substring(3))
                    .resolve(commandFromClient.substring(3));
            stringResult = path.toAbsolutePath().toString();
        }
        if (commandFromClient.startsWith("cat ")) {
            path = Path.of(commandFromClient.substring(4));
            stringResult = Files.readAllLines(path, StandardCharsets.UTF_8) + "\n";
        }
        if (commandFromClient.startsWith("touch ")) {
            String newFileName = commandFromClient.substring(6);
            Path nFiles = Files.createFile(Path.of(newFileName));
            stringResult = fileOrDirectoryTree(nFiles.getParent());
        }
        channel.write(ByteBuffer.wrap(stringResult.getBytes(StandardCharsets.UTF_8)));
    }

    private String fileOrDirectoryTree(Path path) throws IOException {
        return Files.list(path)
                .map(this::mapper)
                .collect(Collectors.joining(""));
    }

    private String mapper(Path path) {
        if (Files.isDirectory(path)) {
            return path.getFileName().toString() + " [DIR]" + "\n";
        } else {
            try {
                long size = Files.size(path);
                return path.getFileName().toString() + " [FILE] " + size + "bytes" + "\n";
            } catch (Exception e) {
                throw new RuntimeException("неизвестная команда");
            }
        }
    }

    private void handleAccept(SelectionKey key) throws Exception {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, "Добро пожаловать в терминал!\n");
    }


    public static void main(String[] args) throws Exception {
        new NioServer();
    }
}
