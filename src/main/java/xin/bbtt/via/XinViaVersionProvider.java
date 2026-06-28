package xin.bbtt.via;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import io.netty.util.AttributeKey;

/**
 * Tells ViaVersion which protocol version each connection's target server speaks, so the
 * base protocol can build the right translation pipeline for <b>any</b> server version —
 * not just the platform default.
 *
 * <p>ViaVersion's {@code InitialBaseProtocol}, when it processes the client handshake, calls
 * {@link #getClosestServerProtocol} to pick the server version, computes the protocol path
 * to it, adds the translation protocols, and rewrites the handshake. The default provider
 * returns the platform's fixed server version ({@code XinViaApi} → 1.21), which is why a
 * plain {@code setup} can only translate to that one version. Here the target is read from a
 * per-connection channel attribute set by {@link XinViaProvider#setupClient}, enabling
 * arbitrary client→server version translation. Connections without the attribute (e.g. the
 * legacy {@link XinViaProvider#setup} path) fall back to the platform default.
 */
public class XinViaVersionProvider implements VersionProvider {

    /** Per-connection target server protocol version, set before the handshake. */
    public static final AttributeKey<ProtocolVersion> TARGET_SERVER_VERSION =
            AttributeKey.valueOf("xinvia.target-server-version");

    @Override
    public ProtocolVersion getClosestServerProtocol(UserConnection connection) {
        if (connection.getChannel() != null) {
            ProtocolVersion target = connection.getChannel().attr(TARGET_SERVER_VERSION).get();
            if (target != null) {
                return target;
            }
        }
        // Fallback: platform default (keeps the legacy setup() path working).
        return Via.getAPI().getServerVersion().lowestSupportedProtocolVersion();
    }
}
