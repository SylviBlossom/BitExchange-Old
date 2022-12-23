package moe.sylvi.bitexchange.data.api;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

public class BitProviderItemEntry extends BitProviderResearchableEntry<Item, ItemBitInfo> {
    private boolean automatable;

    public BitProviderItemEntry() {
        super(BitRegistries.ITEM);

        this.automatable = true;
    }

    public BitProviderItemEntry nonAutomatable() {
        return this.automatable(false);
    }
    public BitProviderItemEntry automatable(boolean automatable) {
        this.automatable = automatable;
        return this;
    }

    @Override
    public BitProviderItemEntry nonResearchable() {
        return (BitProviderItemEntry) super.nonResearchable();
    }
    @Override
    public BitProviderItemEntry researchable() {
        return (BitProviderItemEntry) super.researchable();
    }
    @Override
    public BitProviderItemEntry setResearchable(boolean researchable) {
        return (BitProviderItemEntry) super.setResearchable(researchable);
    }
    @Override
    public BitProviderItemEntry research(long research) {
        return (BitProviderItemEntry) super.research(research);
    }
    @Override
    public BitProviderItemEntry research(ResearchTier tier) {
        return (BitProviderItemEntry) super.research(tier);
    }
    @Override
    public BitProviderItemEntry noRequiredResearch() {
        return (BitProviderItemEntry) super.noRequiredResearch();
    }
    @Override
    public BitProviderItemEntry requireResearch(Item resource) {
        return (BitProviderItemEntry) super.requireResearch(resource);
    }
    @Override
    public <R2, I2 extends BitInfo<R2>> BitProviderItemEntry requireResearch(BitRegistry<R2, I2> registry, R2 resource) {
        return (BitProviderItemEntry) super.requireResearch(registry, resource);
    }
    @Override
    public BitProviderItemEntry resource(Item resource) {
        return (BitProviderItemEntry) super.resource(resource);
    }
    @Override
    public BitProviderItemEntry tag(TagKey<Item> tag) {
        return (BitProviderItemEntry) super.tag(tag);
    }
    @Override
    public BitProviderItemEntry resourceOrTag(ItemOrTag<Item> resource) {
        return (BitProviderItemEntry) super.resourceOrTag(resource);
    }
    @Override
    public BitProviderItemEntry value(double value) {
        return (BitProviderItemEntry) super.value(value);
    }
    @Override
    public BitProviderItemEntry noOverride() {
        return (BitProviderItemEntry) super.noOverride();
    }
    @Override
    public BitProviderItemEntry override() {
        return (BitProviderItemEntry) super.override();
    }
    @Override
    public BitProviderItemEntry setOverride(boolean override) {
        return (BitProviderItemEntry) super.setOverride(override);
    }
    @Override
    public BitProviderItemEntry valueRef(Item resource, double amount) {
        return (BitProviderItemEntry) super.valueRef(resource, amount);
    }
    @Override
    public <R2, I2 extends BitInfo<R2>> BitProviderItemEntry valueRef(BitRegistry<R2, I2> registry, R2 resource, double amount) {
        return (BitProviderItemEntry) super.valueRef(registry, resource, amount);
    }

    @Override
    public JsonObject build() {
        var json = super.build();

        if (isCopy()) {
            return json;
        }

        json.addProperty("automatable", automatable);

        return json;
    }
}
