package com.sbuchelov.io;

import com.sbuchelov.model.AbstractMessage;
import com.sbuchelov.model.FileFromClientMessage;
import com.sbuchelov.model.HiMessage;
import com.sbuchelov.model.TreeMessage;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChatController implements Initializable {
    public Pane authPane;
    public ListView<String> listView;
    public ListView<String> listView2;
    public Button sendButton;
    public Button loadButton;
    public TextField inputName;
    public Label label;
    private Path root;
    private byte[] buffer;
    private ConnectionUtil currConnection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[1024];
        root = Paths.get("root");
        checkRoot(root);
        currConnection = new ConnectionUtil();
        try {
            fillFilesInView();
        } catch (Exception e) {
            e.printStackTrace();
        }
        choiceFile();
        choiceFile2();
        specificThread();
    }

    private void choiceFile() {
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(true);
                sendButton.disableProperty().set(false);
                String fileName = listView.getSelectionModel().getSelectedItem();
                label.setText("File " + fileName + " will send on server!");
            }
        });
    }

    private void choiceFile2() {
        listView2.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                loadButton.disableProperty().set(false);
                sendButton.disableProperty().set(true);
                String fileName = listView.getSelectionModel().getSelectedItem();
                label.setText("File " + fileName + " will load from server!");
            }
        });
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

    private void specificThread() {
        Thread readThread = new Thread(() -> {
            try {
                HiMessage hiMessage = new HiMessage();
                hiMessage.setMessage(inputName.getText());
                currConnection.getDos().writeObject(hiMessage);
                currConnection.getDos().flush();
                while (true) {
                    AbstractMessage abstractMessage = (AbstractMessage) currConnection.getDis().readObject();
                    if (abstractMessage instanceof TreeMessage) {
                        TreeMessage message = (TreeMessage) abstractMessage;
                        Platform.runLater(() -> listView2.setItems(new ObservableListWrapper<>(message.getOutMessage())));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    private void fillFilesInView() throws Exception {
        listView.getItems().clear();
        List<String> list = Files.list(root)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        listView.getItems().addAll(list);
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException {
//        String fileName = input.getText();
//        input.clear();
//        currConnection.getDos().writeObject(new AbstractMessage(fileName));
//        currConnection.getDos().writeObject(new AbstractMessage("User_1"));

    }

    public void sendFile(MouseEvent mouseEvent) throws IOException {
        Path filePath = root.resolve(listView.getSelectionModel().getSelectedItem());
        if (Files.exists(filePath)) {
            AbstractMessage fileFromClient = new FileFromClientMessage("Test message from client");
            currConnection.getDos().writeObject(fileFromClient);
//            currConnection.getDos().writeLong(Files.size(filePath));
//            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
//                int read;
//                while ((read = fis.read(buffer)) != -1) {
//                    currConnection.getDos().write(buffer, 0, read);
//                }
//            }
            currConnection.getDos().flush();
        }
    }

    public void loadFile(MouseEvent mouseEvent) {
    }

    public void authorisation(ActionEvent actionEvent) {
        authPane.setStyle("-fx-background-color: DAE6F3");
        authPane.setVisible(true);
    }

    public void closePaneAuth(ActionEvent actionEvent) {
        authPane.setVisible(false);
    }
}
