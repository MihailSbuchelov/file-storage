package com.sbuchelov.model;

import lombok.Getter;

@Getter
public class DellFileMessage extends AbstractMessage {
    private final String name;

    public DellFileMessage(CommandType type, String name) {
        this.name = name;
        setType(CommandType.FILE_DEL);
    }
}
