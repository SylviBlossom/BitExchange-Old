package moe.sylvi.bitexchange.data.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract class BitProviderBuilder<R, I extends BitInfo<R>, E extends BitProviderEntry> {
    protected final Identifier path;
    protected final BitRegistry<R, I> registry;
    protected final List<E> entries;

    public BitProviderBuilder(Identifier path, BitRegistry<R, I> registry) {
        this.path = path;
        this.registry = registry;
        this.entries = new ArrayList<>();
    }

    public abstract E register(R resource);
    public abstract E register(TagKey<R> tag);

    public Identifier getPath() {
        return path;
    }

    public JsonObject build() {
        var json = new JsonObject();

        json.addProperty("type", BitRegistries.REGISTRY.getId(registry).toString());

        var array = new JsonArray();
        for (E entry : entries) {
            array.add(entry.build());
        }
        json.add("values", array);

        return json;
    }
}
