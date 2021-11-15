package com.sbuchelov.netty.handlers;

import com.sbuchelov.model.DelFileMessage;
import com.sbuchelov.model.ListFilesMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DelHandler extends SimpleChannelInboundHandler<DelFileMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DelFileMessage delFileMessage) throws Exception {
        Files.delete(Paths.get("server_root", MessageHandler.userName, delFileMessage.getName()));
        channelHandlerContext.writeAndFlush(new ListFilesMessage(Paths.get("server_root", MessageHandler.userName)));
    }
}
