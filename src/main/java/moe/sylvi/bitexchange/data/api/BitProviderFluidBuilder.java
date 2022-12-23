package moe.sylvi.bitexchange.data.api;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchTier;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;

public class BitProviderFluidBuilder extends BitProviderBuilder<Fluid, FluidBitInfo, BitProviderFluidEntry> {
    private boolean override = true;

    public BitProviderFluidBuilder(Identifier path) {
        super(path, BitRegistries.FLUID);
    }

    public BitProviderFluidBuilder noOverride() {
        this.override = false;
        return this;
    }

    @Override
    public BitProviderFluidEntry register(Fluid resource) {
        var entry = new BitProviderFluidEntry().resource(resource).setOverride(override);
        entries.add(entry);
        return entry;
    }
    public BitProviderFluidEntry register(Fluid resource, double value, long research) {
        var entry = new BitProviderFluidEntry().resource(resource).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }
    public BitProviderFluidEntry register(Fluid resource, double value, ResearchTier research) {
        var entry = new BitProviderFluidEntry().resource(resource).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }

    @Override
    public BitProviderFluidEntry register(TagKey<Fluid> tag) {
        var entry = new BitProviderFluidEntry().tag(tag).setOverride(override);
        entries.add(entry);
        return entry;
    }
    public BitProviderFluidEntry register(TagKey<Fluid> tag, double value, long research) {
        var entry = new BitProviderFluidEntry().tag(tag).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }
    public BitProviderFluidEntry register(TagKey<Fluid> tag, double value, ResearchTier research) {
        var entry = new BitProviderFluidEntry().tag(tag).setOverride(override).value(value).research(research);
        entries.add(entry);
        return entry;
    }
}
