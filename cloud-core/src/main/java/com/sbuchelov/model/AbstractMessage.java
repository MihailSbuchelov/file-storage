package com.sbuchelov.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class AbstractMessage implements Serializable {
    private CommandType type;

    public AbstractMessage() {
    }
}
