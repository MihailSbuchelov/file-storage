package com.sbuchelov.netty;

import com.sbuchelov.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private Path serverClientDir;
    private byte[] buffer;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        serverClientDir = Paths.get("server_root");
        ctx.writeAndFlush(new ListFilesMessage(serverClientDir));
        buffer = new byte[8192];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractMessage abstractMessage) throws Exception {
        switch (abstractMessage.getType()) {
            case AUTH_MESSAGE:
                new ListFilesMessage(Paths.get("server_root", abstractMessage.getMessage()));
                break;
            case FILE_DEL:
                delFile((DellFileMessage) abstractMessage, channelHandlerContext);
                break;
            case FILE_RENAME:
                renameFile((RenameMessage) abstractMessage, channelHandlerContext);
                break;
            case FILE_MESSAGE:
                processFile((FileMessage) abstractMessage, channelHandlerContext);
                break;
            case FILE_REQUEST:
                sendFile((FileRequest) abstractMessage, channelHandlerContext);
                break;

        }
    }

    private void renameFile(RenameMessage abstractMessage, ChannelHandlerContext channelHandlerContext) {
    }

    private void delFile(DellFileMessage abstractMessage, ChannelHandlerContext channelHandlerContext) {
    }

    private void sendFile(FileRequest msg, ChannelHandlerContext ctx) throws IOException {
        boolean isFirstButch = true;
        Path filePath = serverClientDir.resolve(msg.getName());
        long size = Files.size(filePath);
        try (FileInputStream is = new FileInputStream(serverClientDir.resolve(msg.getName()).toFile())) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(buffer)
                        .name(filePath.getFileName().toString())
                        .size(size)
                        .isFirstBatch(isFirstButch)
                        .isFinishBatch(is.available() <= 0)
                        .endByteNum(read)
                        .build();
                ctx.writeAndFlush(message);
                isFirstButch = false;
            }
        } catch (Exception e) {
            log.error("e:", e);
        }
    }

    private void processFile(FileMessage msg, ChannelHandlerContext ctx) throws Exception {
        Path file = serverClientDir.resolve(msg.getName());
        if (msg.isFirstBatch()) {
            Files.deleteIfExists(file);
        }

        try (FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(msg.getBytes(), 0, msg.getEndByteNum());
        }

        if (msg.isFinishBatch()) {
            ctx.writeAndFlush(new ListFilesMessage(serverClientDir));
        }
    }
}