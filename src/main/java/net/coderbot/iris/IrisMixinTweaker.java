package net.coderbot.iris;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("Oculus")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class IrisMixinTweaker implements IFMLLoadingPlugin, IEarlyMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return ImmutableList.of(
                "mixins.oculus.bettermipmaps.json",
                "mixins.oculus.compat.sodium.json",
                "mixins.oculus.fantastic.json",
                "mixins.oculus.fixes.maxfpscrash.json",
                "mixins.oculus.json",
                "mixins.oculus.optimized-stitching.json",
                "mixins.oculus.vertexformat.json",
                "oculus-batched-entity-rendering.mixins.json"
        );
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
