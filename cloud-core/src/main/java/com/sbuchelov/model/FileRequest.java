package com.sbuchelov.model;

import lombok.Getter;

@Getter
public class FileRequest extends AbstractMessage {
    private final String name;

    public FileRequest(String name) {
        this.name = name;
        setType(CommandType.FILE_REQUEST);
    }
}