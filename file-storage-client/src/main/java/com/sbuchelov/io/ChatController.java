package com.sbuchelov.io;

import com.sbuchelov.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static javafx.application.Platform.runLater;

@Slf4j
public class ChatController implements Initializable {
    public Pane authPane;
    public ListView<String> listViewClient;
    public ListView<String> listViewStorage;
    public Button sendButton;
    public Button loadButton;
    public TextField inputName;
    public Label label;
    private Path root;
    private byte[] buffer;
    private ConnectionUtil connection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[1024];
        root = Paths.get("root");
        checkRoot(root);
        connection = new ConnectionUtil("localhost", 8189);
        try {
            fillFilesInView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        choiceFileClient();
        choiceFileStorage();
        connectionThread();
    }


    private void checkRoot(Path root) {
        if (!Files.exists(root)) {
            try {
                Files.createDirectory(root);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void fillFilesInView() throws Exception {
        runLater(() -> {
            listViewClient.getItems().clear();
            List<String> list = null;
            try {
                list = Files.list(root)
                        .map(p -> p.getFileName().toString())
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            listViewClient.getItems().addAll(list);
        });

    }

    private void choiceFileClient() {
        listViewClient.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(true);
                sendButton.disableProperty().set(false);
                String fileName = listViewClient.getSelectionModel().getSelectedItem();
                label.setText("File " + fileName + " will send on server!");
            }
        });
    }

    private void choiceFileStorage() {
        listViewStorage.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(false);
                sendButton.disableProperty().set(true);
                String fileName = listViewStorage.getSelectionModel().getSelectedItem();
                label.setText("File " + fileName + " will load from server!");
            }
        });
    }

    private void connectionThread() {

        ObjectDecoderInputStream dis = connection.getDis();
        Thread readThread = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage abstractMessage = (AbstractMessage) dis.readObject();
                    processingMessage(abstractMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void processingMessage(AbstractMessage abstractMessage) throws Exception {
        switch (abstractMessage.getType()) {
            case LIST_MESSAGE:
                gettingTreeFileFromCloud(abstractMessage);
                break;
            case FILE_MESSAGE:
                gettingFileFromCloud(abstractMessage);
                break;
        }
    }

    private void gettingTreeFileFromCloud(AbstractMessage abstractMessage) {
        ListFilesMessage message = (ListFilesMessage) abstractMessage;
        runLater(() -> {
            listViewStorage.getItems().clear();
            listViewStorage.getItems().addAll(message.getFiles());
        });
    }

    private void gettingFileFromCloud(AbstractMessage abstractMessage) throws Exception {
        FileMessage msg = (FileMessage) abstractMessage;
        Path file = root.resolve(msg.getName());

        if (msg.isFirstBatch()) {
            Files.deleteIfExists(file);
        }

        try (FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(msg.getBytes(), 0, msg.getEndByteNum());
        }
        fillFilesInView();
    }

    public void sendFile(MouseEvent mouseEvent) throws IOException {
        boolean isFirstButch = true;
        String fileName = listViewClient.getSelectionModel().getSelectedItem();
        Path filePath = root.resolve(fileName);
        long size = Files.size(filePath);
        ObjectEncoderOutputStream dos = connection.getDos();
        try (FileInputStream is = new FileInputStream(filePath.toFile())) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(buffer)
                        .name(fileName)
                        .size(size)
                        .isFirstBatch(isFirstButch)
                        .isFinishBatch(is.available() <= 0)
                        .endByteNum(read)
                        .build();
                dos.writeObject(message);
                isFirstButch = false;
            }
        }
    }

    public void loadFile(MouseEvent mouseEvent) throws IOException {
        String fileName = listViewStorage.getSelectionModel().getSelectedItem();
        ObjectEncoderOutputStream dos = connection.getDos();
        dos.writeObject(new FileRequest(fileName));
    }

    public void authorisation(ActionEvent actionEvent) throws IOException {
        authPane.setStyle("-fx-background-color: DAE6F3");
        authPane.setVisible(true);
        AuthMessage authMessage = new AuthMessage();
        authMessage.setMessage(inputName.getText());
        connection.getDos().writeObject(authMessage);
        connection.getDos().flush();
    }

    public void closePaneAuth(ActionEvent actionEvent) {
        authPane.setVisible(false);
    }
}
