package com.sbuchelov.model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TreeMessage extends AbstractMessage {
    private List<String> outMessage;

    public TreeMessage(Path path) {
        try {
            setOutMessage(fileOrDirectoryTree(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> fileOrDirectoryTree(Path path) throws IOException {
        return Files.list(path)
                .map(this::mapper)
                .filter(c->!c.equals("[DIR]"))
                .collect(Collectors.toList());
    }

    private String mapper(Path path) {
        if (!Files.isDirectory(path)) {
            long size = 0;
            try {
                size = Files.size(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return path.getFileName().toString() + ", file size: " + size + " Bt";
        } else {
            return "[DIR]";
        }
    }

}
