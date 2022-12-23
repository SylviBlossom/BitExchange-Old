package moe.sylvi.bitexchange.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import moe.sylvi.bitexchange.bit.registry.builder.AbstractDataRegistryBuilder;

import java.util.ArrayList;
import java.util.List;

public class BitRegistryItemEventJS extends EventJS {

    private final List<BitRegistryItemBuilderJS> builders = new ArrayList<>();
    private final KubeJSItemRegistryBuilder registryBuilder;

    public BitRegistryItemEventJS(KubeJSItemRegistryBuilder registryBuilder) {
        this.registryBuilder = registryBuilder;
    }

    public BitRegistryItemBuilderJS add(BitRegistryItemBuilderJS.Resource resource) {
        var builder = new BitRegistryItemBuilderJS(resource);
        builders.add(builder);
        return builder;
    }
    public BitRegistryItemBuilderJS add(BitRegistryItemBuilderJS.Resource resource, double value) {
        var builder = add(resource);
        builder.value(value);
        return builder;
    }
    public BitRegistryItemBuilderJS add(BitRegistryItemBuilderJS.Resource resource, double value, long research) {
        var builder = add(resource);
        builder.value(value);
        builder.research(research);
        return builder;
    }
    public BitRegistryItemBuilderJS modify(BitRegistryItemBuilderJS.Resource resource) {
        var builder = add(resource);
        builder.modify();
        return builder;
    }

    public void ignoreData(BitRegistryItemBuilderJS.Resource resource) {
        AbstractDataRegistryBuilder.TEMP_IGNORE.addAll(resource.items());
    }

    @Override
    protected void afterPosted(boolean isCanceled) {
        for (var builder : builders) {
            for (var item : builder.getResource().items()) {
                registryBuilder.builders.put(item, builder);
            }
        }
    }
}
