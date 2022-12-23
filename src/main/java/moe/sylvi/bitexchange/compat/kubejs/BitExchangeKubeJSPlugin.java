package moe.sylvi.bitexchange.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import org.apache.logging.log4j.Level;

public class BitExchangeKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        BitExchange.log(Level.INFO, "Hello KubeJS!");
    }

    @Override
    public void registerEvents() {
        BitRegistryEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("FluidConstants", FluidConstants.class);
    }

    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(BitRegistryItemBuilderJS.Resource.class, BitRegistryItemBuilderJS.Resource::of);
        typeWrappers.registerSimple(BitRegistryFluidBuilderJS.Resource.class, BitRegistryFluidBuilderJS.Resource::of);
    }
}
