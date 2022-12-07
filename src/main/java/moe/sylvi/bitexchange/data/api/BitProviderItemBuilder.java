package moe.sylvi.bitexchange.data.api;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public class BitProviderItemBuilder extends BitProviderBuilder<Item, ItemBitInfo, BitProviderItemEntry> {
    private boolean override = true;

    public BitProviderItemBuilder(Identifier path) {
        super(path, BitRegistries.ITEM);
    }

    public BitProviderItemBuilder noOverride() {
        this.override = false;
        return this;
    }

    @Override
    public BitProviderItemEntry register(Item resource) {
        var entry = new BitProviderItemEntry().resource(resource).setOverride(override);
        entries.add(entry);
        return entry;
    }
    public BitProviderItemEntry register(Item resource, double value, long research) {
        var entry = new BitProviderItemEntry().resource(resource).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }

    @Override
    public BitProviderItemEntry register(TagKey<Item> tag) {
        var entry = new BitProviderItemEntry().tag(tag).setOverride(override);
        entries.add(entry);
        return entry;
    }
    public BitProviderItemEntry register(TagKey<Item> tag, double value, long research) {
        var entry = new BitProviderItemEntry().tag(tag).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }
}
