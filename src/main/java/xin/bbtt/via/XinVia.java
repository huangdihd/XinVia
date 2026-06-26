package xin.bbtt.via;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import xin.bbtt.mcbot.plugin.Plugin;

import java.io.File;

/**
 * XinVia — a xinbot library plugin that bootstraps ViaVersion / ViaBackwards and
 * exposes {@link XinViaProvider} so meta plugins can attach version-translating
 * codecs to their connection.
 */
public class XinVia implements Plugin {

    @Override
    public void onLoad() {
        XinViaPlatform platform = new XinViaPlatform();
        ViaManagerImpl manager = new ViaManagerImpl(platform, new XinViaInjector(), null, new XinViaPlatformLoader());
        Via.init(manager);
        platform.getConf().reload();
        manager.init();
        new XinViaBackwardsPlatform().init(new File("viabackwards"));
    }

    @Override
    public void onUnload() {
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }
}
