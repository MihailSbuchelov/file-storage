package com.sbuchelov.netty.handlers;

import com.sbuchelov.model.ListFilesMessage;
import com.sbuchelov.model.RenameMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Paths;

public class RenameHandler extends SimpleChannelInboundHandler<RenameMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RenameMessage renameMessage) throws Exception {
        Files.copy(Paths.get(MessageHandler.serverClientDir.toString(), renameMessage.getOldName()),
                Paths.get(MessageHandler.serverClientDir.toString(), renameMessage.getNewName()));
        Files.delete(Paths.get(MessageHandler.serverClientDir.toString(), renameMessage.getOldName()));
        channelHandlerContext.writeAndFlush(new ListFilesMessage(Paths.get("server_root", MessageHandler.userName)));
    }
}
