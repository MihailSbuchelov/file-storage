package com.sbuchelov.model;

public class AuthMessage extends AbstractMessage {
    public AuthMessage() {
        setType(CommandType.AUTH_MESSAGE);
    }
}
