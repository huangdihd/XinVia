package xin.bbtt.via;

import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class XinViaEncoder extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection user;

    public XinViaEncoder(UserConnection user) {
        this.user = user;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) {
        if (!bytebuf.isReadable()) {
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            user.transformServerbound(transformedBuf, EncoderException::new);
            out.add(transformedBuf.retain());
        } catch (Exception e) {
            if (e instanceof com.viaversion.viaversion.exception.CancelCodecException) {
                // Cancelled
                return;
            }
            out.add(bytebuf.retain());
        } finally {
            transformedBuf.release();
        }
    }
}
