package com.sbuchelov.model;

import lombok.Getter;

@Getter
public class DelFileMessage extends AbstractMessage {
    private final String name;

    public DelFileMessage(CommandType type, String name) {
        this.name = name;
        setType(CommandType.FILE_DEL);
    }
}
