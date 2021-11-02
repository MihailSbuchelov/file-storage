package com.sbuchelov.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileMessage extends AbstractMessage {
    private static final int BUTCH_SIZE = 8192;

    private final String name;
    private final long size;
    private final byte[] bytes;
    private final boolean isFirstBatch;
    private final int endByteNum;
    private final boolean isFinishBatch;

    public FileMessage(String name,
                       long size,
                       byte[] bytes,
                       boolean isFirstBatch,
                       int endByteNum,
                       boolean isFinishBatch) {
        this.name = name;
        this.size = size;
        this.bytes = bytes;
        this.isFirstBatch = isFirstBatch;
        this.endByteNum = endByteNum;
        this.isFinishBatch = isFinishBatch;
        setType(CommandType.FILE_MESSAGE);
    }
}