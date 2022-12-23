package moe.sylvi.bitexchange.data.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class BitProviderSimpleEntry<R, I extends BitInfo<R>> implements BitProviderEntry {
    private final BitRegistry<R, I> registry;

    private ItemOrTag<R> resource;
    private double value;
    private List<BitResource> valueRefs;
    private boolean override;
    private BitResource copy;

    public BitProviderSimpleEntry(BitRegistry<R, I> registry) {
        this.registry = registry;

        this.resource = null;
        this.value = 0;
        this.valueRefs = new ArrayList<>();
        this.override = true;
        this.copy = null;
    }

    public BitProviderSimpleEntry<R, I> resource(R resource) {
        this.resource = ItemOrTag.of(resource);
        return this;
    }

    public BitProviderSimpleEntry<R, I> tag(TagKey<R> tag) {
        this.resource = ItemOrTag.of(tag);
        return this;
    }

    public BitProviderSimpleEntry<R, I> resourceOrTag(ItemOrTag<R> resource) {
        this.resource = resource;
        return this;
    }

    public BitProviderSimpleEntry<R, I> value(double value) {
        this.value = value;
        return this;
    }

    public BitProviderSimpleEntry<R, I> noOverride() {
        return this.setOverride(false);
    }
    public BitProviderSimpleEntry<R, I> override() {
        return this.setOverride(true);
    }
    public BitProviderSimpleEntry<R, I> setOverride(boolean override) {
        this.override = override;
        return this;
    }

    public BitProviderSimpleEntry<R, I> valueRef(R resource, double amount) {
        return this.valueRef(getDefaultRegistry(), resource, amount);
    }
    public <R2, I2 extends BitInfo<R2>> BitProviderSimpleEntry<R, I> valueRef(BitRegistry<R2, I2> registry, R2 resource, double amount) {
        this.valueRefs.add(BitResource.of(registry, resource, amount));
        return this;
    }

    public BitProviderSimpleEntry<R, I> copy(R resource) {
        return this.copy(getDefaultRegistry(), resource);
    }
    public <R2, I2 extends BitInfo<R2>> BitProviderSimpleEntry<R, I> copy(BitRegistry<R2, I2> registry, R2 resource) {
        this.copy = BitResource.of(registry, resource, 1);
        return this;
    }

    @Deprecated
    public BitRegistry<R, I> getDefaultRegistry() {
        return registry;
    }

    @Deprecated
    public Registry<R> getResourceRegistry() {
        return getDefaultRegistry().getResourceRegistry();
    }

    @Deprecated
    public boolean isCopy() {
        return copy != null;
    }

    protected String serializeResource(BitResource resource, boolean includeQuantity) {
        var builder = new StringBuilder();

        if(resource.getRegistry() != getDefaultRegistry()) {
            builder.append(BitRegistries.REGISTRY.getId(resource.getRegistry()).toString());
            builder.append("$");
        }
        builder.append(resource.getRegistry().getResourceRegistry().getId(resource.getResource()).toString());
        if (includeQuantity) {
            if (resource.getAmount() % 1 == 0) {
                builder.append("*");
                builder.append((long) resource.getAmount());
            } else if (resource.getAmount() < 1 && resource.getAmount() > 0 && (1 / resource.getAmount()) % 1 == 0) {
                builder.append("/");
                builder.append((long) (1 / resource.getAmount()));
            } else {
                builder.append("*");
                builder.append(resource.getAmount());
            }
        }

        return builder.toString();
    }

    protected String serializeResourceList(List<BitResource> resources, boolean includeQuantity) {
        return String.join(",", resources.stream().map(r -> serializeResource(r, includeQuantity)).toArray(String[]::new));
    }

    @Override
    public JsonObject build() {
        var json = new JsonObject();

        json.addProperty("override", override);
        if (resource != null) {
            resource.consumeItem(item ->
                    json.addProperty("id", getResourceRegistry().getId(item).toString())
            ).consumeTag(tag ->
                    json.addProperty("id", "#" + tag.id().toString())
            );
        }

        if (isCopy()) {
            json.addProperty("copy", serializeResource(copy, false));
            return json;
        }

        if (!valueRefs.isEmpty()) {
            json.addProperty("value_ref", serializeResourceList(valueRefs, true));
        }
        json.addProperty("value", value);

        return json;
    }
}
