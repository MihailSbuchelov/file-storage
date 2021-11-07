package com.sbuchelov.netty.handlers;

import com.sbuchelov.model.AbstractMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    public static Path serverClientDir;
    public static byte[] buffer;
    public static String userName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        buffer = new byte[8192];
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractMessage abstractMessage) throws Exception {
        switch (abstractMessage.getType()) {
            case AUTH_MESSAGE:
            case FILE_DEL:
            case FILE_RENAME:
            case FILE_MESSAGE:
            case FILE_REQUEST:
                channelHandlerContext.fireChannelRead(abstractMessage);
                break;
        }
    }
}