package com.sbuchelov.netty;

import com.sbuchelov.model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AbstractMessage abstractMessage) throws Exception {

        if (abstractMessage instanceof HiMessage) {
            TreeMessage treeMessage = new TreeMessage(Paths.get("C:\\Users\\Miha_admin\\Desktop\\file-storage\\server_root"
                    + "\\" + abstractMessage.getMessage()));
            channelHandlerContext.writeAndFlush(treeMessage);
        }
        if (abstractMessage instanceof FileFromCloudeMessage) {

        }
        if (abstractMessage instanceof FileFromClientMessage) {
            System.out.println(((FileFromClientMessage) abstractMessage).getDos());
        }

    }
}
