package moe.sylvi.bitexchange.compat.kubejs;

import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.registry.builder.BitRegistryBuilder;
import moe.sylvi.bitexchange.bit.registry.builder.ItemPriorities;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class KubeJSItemRegistryBuilder implements BitRegistryBuilder<Item, ItemBitInfo> {
    protected final BitRegistry<Item, ItemBitInfo> registry;

    public Map<Item, BitRegistryItemBuilderJS> builders = new HashMap<>();

    public KubeJSItemRegistryBuilder(BitRegistry<Item, ItemBitInfo> registry) {
        this.registry = registry;
    }

    @Override
    public int getPriority() {
        return ItemPriorities.ABOVE_DATA;
    }

    @Override
    public void prepare(MinecraftServer server) {
        BitRegistryEvents.ITEMS.post(new BitRegistryItemEventJS(this));

        for (var item : builders.keySet()) {
            registry.prepareResource(item, this);
        }
    }

    @Override
    public ItemBitInfo process(Item resource) {
        return builders.get(resource).build(resource);
    }

    @Override
    public void postProcess() {
        builders.clear();
    }
}
