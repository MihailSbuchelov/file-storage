package com.sbuchelov.model;

import lombok.Data;

@Data
public class FileFromClientMessage extends AbstractMessage {
    String dos;

    public FileFromClientMessage(String s) {
        setDos(s);
    }

}
