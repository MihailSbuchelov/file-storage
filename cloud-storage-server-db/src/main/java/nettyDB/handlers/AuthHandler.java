package nettyDB.handlers;

import com.sbuchelov.database.service.Authentication;
import com.sbuchelov.model.AuthMessage;
import com.sbuchelov.model.ListFilesMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.file.Paths;


public class AuthHandler extends SimpleChannelInboundHandler<AuthMessage> {
    Authentication authentication = new Authentication();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, AuthMessage authMessage) throws Exception {
        if (authentication.findNicknameByLogin(authMessage.getUserName()) != null) {
            MessageHandler.serverClientDir = Paths.get("server_root", authMessage.getUserName());
            MessageHandler.userName = authMessage.getUserName();
            channelHandlerContext.writeAndFlush(
                    new ListFilesMessage(Paths.get("server_root", authMessage.getUserName())));
        } else return;
    }
}
