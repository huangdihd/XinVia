package xin.bbtt.via;

import com.viaversion.viaversion.api.connection.UserConnection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class XinViaDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final boolean DEBUG = Boolean.getBoolean("xinvia.debug");
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("XinVia");
    private final UserConnection user;

    public XinViaDecoder(UserConnection user) {
        this.user = user;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) {
        if (!bytebuf.isReadable()) {
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            user.transformClientbound(transformedBuf, DecoderException::new);
            out.add(transformedBuf.retain());
        } catch (Exception e) {
            if (e instanceof com.viaversion.viaversion.exception.CancelCodecException) {
                return;
            }
            if (DEBUG) {
                LOG.warn("[XinVia] clientbound translate failed ({} readable), passing through", bytebuf.readableBytes(), e);
            }
            out.add(bytebuf.retain());
        } finally {
            transformedBuf.release();
        }
    }
}
