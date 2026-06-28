# XinVia

> A [xinbot](https://github.com/huangdihd/xinbot) **library plugin** that bootstraps
> [ViaVersion](https://github.com/ViaVersion/ViaVersion) / [ViaBackwards](https://github.com/ViaVersion/ViaBackwards)
> and exposes a small API so **meta plugins can attach version-translating codecs to their connection**.

English / [简体中文](README_zh.md)

---

## What it does

XinVia is `type: PLUGIN` (not a meta plugin). It does two things:

1. On load, it initializes the ViaVersion/ViaBackwards platform for xinbot.
2. It exposes `xin.bbtt.via.XinViaProvider` for other plugins to install/remove the
   translating Netty codecs on a connection's channel.

This lets any meta plugin gain cross-version protocol support by simply depending on
XinVia, instead of bundling ViaVersion itself.

## Usage

### 1. plugin.yml

Declare XinVia as a dependency so xinbot loads it first and wires up the classloader chain:

```yaml
name: YourMetaPlugin
main: com.example.YourMetaPlugin
type: META_PLUGIN
depend:
  - XinVia
```

### 2. pom.xml

Add the dependency (provided — it is supplied at runtime by the XinVia plugin jar) via JitPack:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.huangdihd</groupId>
    <artifactId>XinVia</artifactId>
    <version>1.1.0-RELEASE</version>
    <scope>provided</scope>
</dependency>
```

### 3. Code

There are two entry points. Pick based on how far apart the versions are.

**`setupClient` — full client pipeline, any version → any version (recommended).**
Install it on the first outgoing packet (the handshake). ViaVersion's base protocol then
translates the *entire* connection — handshake, login, configuration and play — so the bot
can join a server several versions older or newer than the bot itself. Internally a custom
version provider feeds ViaVersion the per-connection target version, so arbitrary gaps work
(e.g. a 1.21.11 bot joining a 1.20.1 / 1.16.5 / 1.12.2 server).

```java
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;

private volatile boolean done = false;

// in onEnable(): install Via on the first packet so the handshake is translated too
Bot.INSTANCE.addPacketListener(new SessionAdapter() {
    @Override public void packetSending(org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent e) {
        if (done) return;
        done = true;
        // Only the target server version is needed — the bot's own version and UUID
        // are detected automatically.
        XinViaProvider.setupClient(e.getSession().getChannel(), ProtocolVersion.v1_20);
    }
}, this);
```

**`setup` — play-phase only (legacy; for tiny gaps where login is already compatible).**

```java
UserConnection connection = XinViaProvider.setup(
        channel, ProtocolVersion.v1_21_11, ProtocolVersion.v1_21, playerUuid);
// On disable / disconnect:
XinViaProvider.teardown(channel, connection);
```

To join **modded** old servers (Forge/NeoForge), combine `setupClient` with
[XinModBridge](https://github.com/huangdihd/XinModBridge) (`depend: [XinVia, XinModBridge]`)
for the FML handshake.

## Building

```bash
mvn clean package
```

The shaded jar in `target/` is a self-contained xinbot plugin: drop it into the `plugin`
directory next to the meta plugins that depend on it. Java 17 or newer is required.

## License

GPL-3.0-or-later, see [LICENSE](LICENSE) for the full text.
