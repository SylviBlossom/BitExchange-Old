package moe.sylvi.bitexchange.data.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.bit.BitResource;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import net.minecraft.tag.TagKey;

import java.util.ArrayList;
import java.util.List;

public class BitProviderResearchableEntry<R, I extends BitInfo<R>> extends BitProviderSimpleEntry<R, I> {
    private boolean researchable;
    private long research;
    private ResearchTier researchTier;
    private List<BitResource> researchRequirements;
    private boolean noRequirements;

    public BitProviderResearchableEntry(BitRegistry<R, I> registry) {
        super(registry);

        this.researchable = true;
        this.research = registry.getEmpty().getRatio();
        this.researchRequirements = new ArrayList<>();
    }


    public BitProviderResearchableEntry<R, I> nonResearchable() {
        return this.setResearchable(false);
    }
    public BitProviderResearchableEntry<R, I> researchable() {
        return this.setResearchable(true);
    }
    public BitProviderResearchableEntry<R, I> setResearchable(boolean researchable) {
        this.researchable = researchable;
        return this;
    }

    public BitProviderResearchableEntry<R, I> research(long research) {
        this.research = research;
        this.researchTier = null;
        return this;
    }
    public BitProviderResearchableEntry<R, I> research(ResearchTier tier) {
        this.research = tier.getDefaultResearch();
        this.researchTier = tier;
        return this;
    }

    public BitProviderResearchableEntry<R, I> noRequiredResearch() {
        this.noRequirements = true;
        return this;
    }

    public BitProviderResearchableEntry<R, I> requireResearch(R resource) {
        return this.requireResearch(getDefaultRegistry(), resource);
    }
    public <R2, I2 extends BitInfo<R2>> BitProviderResearchableEntry<R, I> requireResearch(BitRegistry<R2, I2> registry, R2 resource) {
        this.researchRequirements.add(BitResource.of(registry, resource, 1));
        return this;
    }

    @Override
    public BitProviderResearchableEntry<R, I> resource(R resource) {
        return (BitProviderResearchableEntry<R, I>) super.resource(resource);
    }
    @Override
    public BitProviderResearchableEntry<R, I> tag(TagKey<R> tag) {
        return (BitProviderResearchableEntry<R, I>) super.tag(tag);
    }
    @Override
    public BitProviderResearchableEntry<R, I> resourceOrTag(ItemOrTag<R> resource) {
        return (BitProviderResearchableEntry<R, I>) super.resourceOrTag(resource);
    }
    @Override
    public BitProviderResearchableEntry<R, I> value(double value) {
        return (BitProviderResearchableEntry<R, I>) super.value(value);
    }
    @Override
    public BitProviderResearchableEntry<R, I> noOverride() {
        return (BitProviderResearchableEntry<R, I>) super.noOverride();
    }
    @Override
    public BitProviderResearchableEntry<R, I> override() {
        return (BitProviderResearchableEntry<R, I>) super.override();
    }
    @Override
    public BitProviderResearchableEntry<R, I> setOverride(boolean override) {
        return (BitProviderResearchableEntry<R, I>) super.setOverride(override);
    }
    @Override
    public BitProviderResearchableEntry<R, I> valueRef(R resource, double amount) {
        return (BitProviderResearchableEntry<R, I>) super.valueRef(resource, amount);
    }
    @Override
    public <R2, I2 extends BitInfo<R2>> BitProviderResearchableEntry<R, I> valueRef(BitRegistry<R2, I2> registry, R2 resource, double amount) {
        return (BitProviderResearchableEntry<R, I>) super.valueRef(registry, resource, amount);
    }

    @Override
    public JsonObject build() {
        var json = super.build();

        if (isCopy()) {
            return json;
        }

        json.addProperty("researchable", researchable);
        if (researchTier != null) {
            json.addProperty("research", researchTier.getName());
        } else {
            json.addProperty("research", research);
        }
        if (noRequirements) {
            json.addProperty("required_research", "");
        } else if(!researchRequirements.isEmpty()) {
            json.addProperty("required_research", serializeResourceList(researchRequirements, false));
        }

        return json;
    }
}
