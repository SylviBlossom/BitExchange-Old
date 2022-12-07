package moe.sylvi.bitexchange.data.api;

import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;

public class BitProviderFluidEntry extends BitProviderResearchableEntry<Fluid, FluidBitInfo> {
    private long ratio;

    public BitProviderFluidEntry() {
        super(BitRegistries.FLUID);

        this.ratio = FluidConstants.BUCKET;
    }

    public BitProviderFluidEntry ratio(long ratio) {
        this.ratio = ratio;
        return this;
    }

    @Override
    public BitProviderFluidEntry nonResearchable() {
        return (BitProviderFluidEntry) super.nonResearchable();
    }
    @Override
    public BitProviderFluidEntry researchable() {
        return (BitProviderFluidEntry) super.researchable();
    }
    @Override
    public BitProviderFluidEntry setResearchable(boolean researchable) {
        return (BitProviderFluidEntry) super.setResearchable(researchable);
    }
    @Override
    public BitProviderFluidEntry research(long research) {
        return (BitProviderFluidEntry) super.research(research);
    }
    @Override
    public BitProviderFluidEntry noRequiredResearch() {
        return (BitProviderFluidEntry) super.noRequiredResearch();
    }
    @Override
    public BitProviderFluidEntry requireResearch(Fluid resource) {
        return (BitProviderFluidEntry) super.requireResearch(resource);
    }
    @Override
    public <R2, I2 extends BitInfo<R2>> BitProviderFluidEntry requireResearch(BitRegistry<R2, I2> registry, R2 resource) {
        return (BitProviderFluidEntry) super.requireResearch(registry, resource);
    }
    @Override
    public BitProviderFluidEntry resource(Fluid resource) {
        return (BitProviderFluidEntry) super.resource(resource);
    }
    @Override
    public BitProviderFluidEntry tag(TagKey<Fluid> tag) {
        return (BitProviderFluidEntry) super.tag(tag);
    }
    @Override
    public BitProviderFluidEntry resourceOrTag(ItemOrTag<Fluid> resource) {
        return (BitProviderFluidEntry) super.resourceOrTag(resource);
    }
    @Override
    public BitProviderFluidEntry value(double value) {
        return (BitProviderFluidEntry) super.value(value);
    }
    @Override
    public BitProviderFluidEntry noOverride() {
        return (BitProviderFluidEntry) super.noOverride();
    }
    @Override
    public BitProviderFluidEntry override() {
        return (BitProviderFluidEntry) super.override();
    }
    @Override
    public BitProviderFluidEntry setOverride(boolean override) {
        return (BitProviderFluidEntry) super.setOverride(override);
    }
    @Override
    public BitProviderFluidEntry valueRef(Fluid resource, double amount) {
        return (BitProviderFluidEntry) super.valueRef(resource, amount);
    }
    @Override
    public <R2, I2 extends BitInfo<R2>> BitProviderFluidEntry valueRef(BitRegistry<R2, I2> registry, R2 resource, double amount) {
        return (BitProviderFluidEntry) super.valueRef(registry, resource, amount);
    }

    @Override
    public JsonObject build() {
        var json = super.build();

        if (isCopy()) {
            return json;
        }

        json.addProperty("ratio", ratio);

        return json;
    }
}
