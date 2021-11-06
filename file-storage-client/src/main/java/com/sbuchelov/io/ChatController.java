package com.sbuchelov.io;

import com.sbuchelov.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
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
    public Pane renamePaneClient;
    public Pane renamePaneCloud;

    public ListView<String> listViewClient;
    public ListView<String> listViewStorage;

    public Button sendButton;
    public Button loadButton;
    public Button delButtonCloud;
    public Button delButtonClient;
    public Button renameButtonCl;
    public Button renameButtonCld;
    public Button chRootDirButton;
    public Button authButton;

    public Label authLabel;

    public TextField inputAuthName;
    public TextField inputNameFile;
    public TextField inputNameFileCloud;

    private Path root;
    private byte[] buffer;
    private ConnectionUtil connection;
    private ObjectDecoderInputStream dis;

    @SneakyThrows
    @FXML

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[1024];
        root = Paths.get("root");
        connection = new ConnectionUtil("localhost", 8189);
        dis = connection.getDis();
        try {
            fillFilesInView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        connectionThread();

        choiceFileClient();
        checkAuth();
        choiceFileStorage();
    }

    private void fillFilesInView() {
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

    private void connectionThread() {
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

    private void checkAuth() {
        new Thread(() -> {
            try {
                if (authButton.isPressed()) {
                    while (inputAuthName.getText().isEmpty() || inputAuthName.getText().equals("")) {
                        listViewClient.setDisable(true);
                        sendButton.setDisable(true);
                    }
                    AuthMessage authMessage = new AuthMessage(inputAuthName.getText());
                    connection.getDos().writeObject(authMessage);
                    runLater(() -> {
                        authPane.setVisible(false);
                        listViewClient.setDisable(false);
                        listViewStorage.setDisable(false);
                        sendButton.setDisable(false);
                    });
                } else {
                    listViewStorage.setDisable(true);
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void choiceFileClient() {
        listViewClient.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(true);
                sendButton.disableProperty().set(false);
                chRootDirButton.disableProperty().set(false);
                delButtonCloud.disableProperty().set(true);
                delButtonClient.disableProperty().set(false);
                renameButtonCld.disableProperty().set(true);
                renameButtonCl.disableProperty().set(false);
            }
        });
    }

    private void choiceFileStorage() {
        listViewStorage.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(false);
                sendButton.disableProperty().set(true);
                chRootDirButton.disableProperty().set(true);
                delButtonClient.disableProperty().set(true);
                delButtonCloud.disableProperty().set(false);
                renameButtonCld.disableProperty().set(false);
                renameButtonCl.disableProperty().set(true);
            }
        });
    }

    private void processingMessage(AbstractMessage abstractMessage) throws Exception {
        switch (abstractMessage.getType()) {
            case LIST_MESSAGE:
                gettingTreeFilesFromCloud(abstractMessage);
                break;
            case FILE_MESSAGE:
                gettingFileFromCloud(abstractMessage);
                break;
        }
    }

    private void gettingTreeFilesFromCloud(AbstractMessage abstractMessage) {
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

    public void sendFileFromClient(MouseEvent mouseEvent) throws IOException {
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

    public void loadFileFromCloud(MouseEvent mouseEvent) throws IOException {
        String fileName = listViewStorage.getSelectionModel().getSelectedItem();
        ObjectEncoderOutputStream dos = connection.getDos();
        dos.writeObject(new FileRequest(fileName));
    }

    public void authPane(ActionEvent actionEvent) {
        authPane.setStyle("-fx-background-color: DAE6F3");
        authPane.setVisible(true);
    }

    public void closeAuthPane(ActionEvent actionEvent) throws IOException {
        AuthMessage authMessage = new AuthMessage(inputAuthName.getText());
        connection.getDos().writeObject(authMessage);
        runLater(() -> {
            authPane.setVisible(false);
            if (inputAuthName.getText().isEmpty() || inputAuthName.getText().equals("")) {
                listViewClient.setDisable(true);
                listViewStorage.setDisable(true);
                sendButton.setDisable(true);
                loadButton.setDisable(true);
                chRootDirButton.setDisable(true);
                delButtonCloud.setDisable(true);
                delButtonClient.setDisable(true);
                renameButtonCl.setDisable(true);
                renameButtonCld.setDisable(true);
                return;
            }
            listViewClient.setDisable(false);
            listViewStorage.setDisable(false);
        });
    }

    public void renameFileClient(MouseEvent mouseEvent) {
        renamePaneClient.setStyle("-fx-background-color: DAE6F3");
        renamePaneClient.setVisible(true);
    }

    public void renameFileCloud(MouseEvent mouseEvent) {
        renamePaneCloud.setStyle("-fx-background-color: DAE6F3");
        renamePaneCloud.setVisible(true);
    }

    public void delButtonCloud(MouseEvent mouseEvent) throws Exception {
        if (listViewStorage.getFocusModel().getFocusedIndex() != -1) {
            AbstractMessage delMessage = new DellFileMessage(CommandType.FILE_DEL, listViewStorage.getSelectionModel().getSelectedItem());
            connection.getDos().writeObject(delMessage);
        }
    }

    public void delButtonClient(MouseEvent mouseEvent) throws Exception {
        if (listViewClient.getFocusModel().getFocusedIndex() != -1) {
            Files.delete(Paths.get(root.toString(), listViewClient.getSelectionModel().getSelectedItem()));
            fillFilesInView();
        }
    }

    public void saveNewNameFileClient(ActionEvent actionEvent) throws Exception {
        if (listViewClient.getSelectionModel().getSelectedItem().isEmpty() || inputNameFile.getText().isEmpty()
                || (listViewClient.getFocusModel().getFocusedIndex() == -1)
                || (listViewClient.getSelectionModel().getSelectedItem().equals(inputNameFile.getText()))) {
            renamePaneClient.setVisible(false);
            return;
        }
        Files.copy(Paths.get(root.toString(), listViewClient.getSelectionModel().getSelectedItem()),
                Paths.get(root.toString(), inputNameFile.getText()));
        Files.delete(Paths.get(root.toString(), listViewClient.getSelectionModel().getSelectedItem()));
        renamePaneClient.setVisible(false);
        fillFilesInView();
    }

    public void saveNewNameFileCloud(ActionEvent actionEvent) throws IOException {
        if (listViewStorage.getSelectionModel().getSelectedItem().isEmpty() || inputNameFileCloud.getText().isEmpty()
                || (listViewStorage.getFocusModel().getFocusedIndex() == -1)
                || (listViewStorage.getSelectionModel().getSelectedItem().equals(inputNameFileCloud.getText()))) {
            renamePaneCloud.setVisible(false);
            return;
        }
        connection.getDos().writeObject(new RenameMessage(CommandType.FILE_RENAME, listViewStorage.getSelectionModel().getSelectedItem(),
                inputNameFileCloud.getText()));
        renamePaneCloud.setVisible(false);
    }

    public void changeDirClient(MouseEvent mouseEvent) throws Exception {
        JFileChooser jfc = new JFileChooser();
        // показывать только директории
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "";
            }
        });
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int a = jfc.showDialog(null, "Open directory");
        if (a == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            root = file.toPath().toAbsolutePath();
            fillFilesInView();
        }
    }
}
