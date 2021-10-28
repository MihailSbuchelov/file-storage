package com.sbuchelov.model;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ListFilesMessage extends AbstractMessage {
    private final List<String> files;

    public ListFilesMessage(Path dir) throws Exception {
        setType(CommandType.LIST_MESSAGE);
        files = Files.list(dir).map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }
}
