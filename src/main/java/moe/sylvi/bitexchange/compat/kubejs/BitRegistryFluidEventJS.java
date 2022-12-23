package moe.sylvi.bitexchange.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import moe.sylvi.bitexchange.bit.registry.builder.AbstractDataRegistryBuilder;

import java.util.ArrayList;
import java.util.List;

public class BitRegistryFluidEventJS extends EventJS {

    private final List<BitRegistryFluidBuilderJS> builders = new ArrayList<>();
    private final KubeJSFluidRegistryBuilder registryBuilder;

    public BitRegistryFluidEventJS(KubeJSFluidRegistryBuilder registryBuilder) {
        this.registryBuilder = registryBuilder;
    }

    public BitRegistryFluidBuilderJS add(BitRegistryFluidBuilderJS.Resource resource) {
        var builder = new BitRegistryFluidBuilderJS(resource);
        builders.add(builder);
        return builder;
    }
    public BitRegistryFluidBuilderJS add(BitRegistryFluidBuilderJS.Resource resource, double value) {
        var builder = add(resource);
        builder.value(value);
        return builder;
    }
    public BitRegistryFluidBuilderJS add(BitRegistryFluidBuilderJS.Resource resource, double value, long research) {
        var builder = add(resource);
        builder.value(value);
        builder.research(research);
        return builder;
    }
    public BitRegistryFluidBuilderJS modify(BitRegistryFluidBuilderJS.Resource resource) {
        var builder = add(resource);
        builder.modify();
        return builder;
    }

    public void ignoreData(BitRegistryFluidBuilderJS.Resource resource) {
        AbstractDataRegistryBuilder.TEMP_IGNORE.addAll(resource.fluids());
    }

    @Override
    protected void afterPosted(boolean isCanceled) {
        for (var builder : builders) {
            for (var fluid : builder.getResource().fluids()) {
                registryBuilder.builders.put(fluid, builder);
            }
        }
    }
}
