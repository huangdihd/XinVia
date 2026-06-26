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
    <version>1.0.0-RELEASE</version>
    <scope>provided</scope>
</dependency>
```

### 3. Code

```java
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import xin.bbtt.via.XinViaProvider;

// When your channel is ready, install the codecs.
// Versions are parameters, so you decide which client/server versions to bridge.
UserConnection connection = XinViaProvider.setup(
        channel,
        ProtocolVersion.v1_21_11, // version this bot speaks
        ProtocolVersion.v1_21,    // version the remote server speaks
        playerUuid);

// On disable / disconnect:
XinViaProvider.teardown(channel, connection);
```

## Building

```bash
mvn clean package
```

The shaded jar in `target/` is a self-contained xinbot plugin: drop it into the `plugin`
directory next to the meta plugins that depend on it. Java 17 or newer is required.

## License

GPL-3.0-or-later, see [LICENSE](LICENSE) for the full text.
