package nettyDB.handlers;

import com.sbuchelov.model.FileMessage;
import com.sbuchelov.model.FileRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileRequestHandler extends SimpleChannelInboundHandler<FileRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FileRequest fileRequest) throws Exception {
        sendFile(fileRequest, channelHandlerContext);
    }

    private void sendFile(FileRequest msg, ChannelHandlerContext ctx) throws IOException {
        boolean isFirstButch = true;
        Path filePath = MessageHandler.serverClientDir.resolve(msg.getName());
        long size = Files.size(filePath);
        try (FileInputStream is = new FileInputStream(MessageHandler.serverClientDir.resolve(msg.getName()).toFile())) {
            int read;
            while ((read = is.read(MessageHandler.buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(MessageHandler.buffer)
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
}
