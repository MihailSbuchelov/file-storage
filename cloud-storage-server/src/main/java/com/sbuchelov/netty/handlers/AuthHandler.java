package com.sbuchelov.netty.handlers;

import com.sbuchelov.model.AuthMessage;
import com.sbuchelov.model.ListFilesMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Paths;

import static com.sbuchelov.netty.handlers.MessageHandler.serverClientDir;
import static com.sbuchelov.netty.handlers.MessageHandler.userName;

public class AuthHandler extends SimpleChannelInboundHandler<AuthMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AuthMessage authMessage) throws Exception {
        if (authMessage.getUserName().isEmpty()) {
            return;
        } else {
            serverClientDir = Paths.get("server_root", authMessage.getUserName());
            userName = authMessage.getUserName();
            channelHandlerContext.writeAndFlush(
                    new ListFilesMessage(Paths.get("server_root", authMessage.getUserName())));
        }
    }
}
