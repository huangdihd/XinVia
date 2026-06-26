package xin.bbtt.via;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Public entry point of the XinVia library.
 * <p>
 * Meta plugins call {@link #setup} to register ViaVersion's translating codecs on
 * a Netty channel and {@link #teardown} to remove them again. The protocol versions
 * are parameters, so each consumer decides which client/server versions to bridge.
 */
public final class XinViaProvider {

    private static final Logger log = LoggerFactory.getLogger("XinVia");

    /** The Netty pipeline handler name used for the clientbound (decoding) codec. */
    public static final String DECODER_NAME = "via-decoder";
    /** The Netty pipeline handler name used for the serverbound (encoding) codec. */
    public static final String ENCODER_NAME = "via-encoder";

    private XinViaProvider() {
    }

    /**
     * Builds a {@link UserConnection} for the given channel and installs the ViaVersion
     * decoder/encoder before the {@code codec} handler in the pipeline.
     *
     * @param channel         the connection's Netty channel
     * @param clientVersion   the protocol version the client (this bot) speaks
     * @param serverVersion   the protocol version the remote server speaks
     * @param uuid            the player UUID of the connection
     * @return the created {@link UserConnection}, or {@code null} if ViaVersion is not ready
     */
    public static UserConnection setup(Channel channel,
                                       ProtocolVersion clientVersion,
                                       ProtocolVersion serverVersion,
                                       UUID uuid) {
        if (Via.getManager() == null || Via.getManager().getProtocolManager() == null) {
            log.warn("ViaVersion not ready yet");
            return null;
        }

        UserConnection userConnection = new UserConnectionImpl(channel, true);

        ProtocolInfo protocolInfo = userConnection.getProtocolInfo();
        protocolInfo.setProtocolVersion(clientVersion);
        protocolInfo.setServerProtocolVersion(serverVersion);
        protocolInfo.setState(State.HANDSHAKE);
        protocolInfo.setUuid(uuid);

        new ProtocolPipelineImpl(userConnection);

        Via.getManager().getConnectionManager().onLoginSuccess(userConnection);

        try {
            channel.pipeline().addBefore("codec", DECODER_NAME, new XinViaDecoder(userConnection));
            channel.pipeline().addBefore("codec", ENCODER_NAME, new XinViaEncoder(userConnection));
        } catch (Exception e) {
            log.error("Failed to add handlers", e);
        }

        return userConnection;
    }

    /**
     * Removes the ViaVersion codecs from the channel pipeline (on its event loop) and
     * disconnects the given {@link UserConnection}. Safe to call with {@code null} arguments.
     *
     * @param channel        the connection's Netty channel, may be {@code null}
     * @param userConnection the connection returned by {@link #setup}, may be {@code null}
     */
    public static void teardown(Channel channel, UserConnection userConnection) {
        if (channel != null) {
            channel.eventLoop().execute(() -> {
                if (channel.pipeline().get(DECODER_NAME) != null) {
                    channel.pipeline().remove(DECODER_NAME);
                }
                if (channel.pipeline().get(ENCODER_NAME) != null) {
                    channel.pipeline().remove(ENCODER_NAME);
                }
            });
        }

        if (userConnection != null) {
            try {
                userConnection.disconnect("Plugin disabled");
            } catch (Exception ignored) {
            }
        }
    }
}
