package nettyDB.handlers;

import com.sbuchelov.model.FileMessage;
import com.sbuchelov.model.ListFilesMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessageHandler extends SimpleChannelInboundHandler<FileMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileMessage fileMessage) throws Exception {
        processFile(fileMessage, channelHandlerContext);
    }

    private void processFile(FileMessage msg, ChannelHandlerContext ctx) throws Exception {
        Path file = MessageHandler.serverClientDir.resolve(msg.getName());
        if (msg.isFirstBatch()) {
            Files.deleteIfExists(file);
        }

        try (FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(msg.getBytes(), 0, msg.getEndByteNum());
        }

        if (msg.isFinishBatch()) {
            ctx.writeAndFlush(new ListFilesMessage(MessageHandler.serverClientDir));
        }
    }

}
