package com.sbuchelov.model;

import lombok.Getter;

@Getter
public class RenameMessage extends AbstractMessage {
    private final String oldName;
    private final String newName;

    public RenameMessage(CommandType type, String oldName, String newName) {
        setType(CommandType.FILE_RENAME);
        this.oldName = oldName;
        this.newName = newName;
    }
}
