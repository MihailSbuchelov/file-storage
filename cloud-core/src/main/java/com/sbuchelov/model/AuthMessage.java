package com.sbuchelov.model;

import lombok.Getter;

@Getter
public class AuthMessage extends AbstractMessage {
    private final String userName;

    public AuthMessage(String userName) {
        this.userName = userName;
        setType(CommandType.AUTH_MESSAGE);
    }
}
