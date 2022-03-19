package moe.sylvi.bitexchange.bit.info;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitExchangeClient;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.research.*;
import moe.sylvi.bitexchange.mixin.SpriteMixin;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mutable;

import java.util.List;

public class FluidBitInfo implements BitInfoResearchable<Fluid> {
    protected final Fluid fluid;
    protected final double value;
    protected final long research;
    protected final boolean researchable;
    protected final List<ResearchRequirement> researchRequirements;

    public FluidBitInfo(Fluid fluid, double value, long research, boolean researchable, List<ResearchRequirement> researchRequirements) {
        this.fluid = fluid;
        this.value = value;
        this.research = research;
        this.researchable = researchable;
        this.researchRequirements = researchRequirements;
    }

    @Override
    public Fluid getResource() {
        return fluid;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public long getResearch() {
        return research;
    }

    @Override
    public boolean isResearchable() {
        return researchable;
    }

    @Override
    public ResearchRequirement createResearchRequirement() {
        return new BitResearchRequirement(fluid, BitRegistries.FLUID);
    }

    @Override
    public List<ResearchRequirement> getResearchRequirements() {
        return researchRequirements;
    }

    @Override
    public <V> BitKnowledge<Fluid> getKnowledge(V provider) {
        return BitComponents.FLUID_KNOWLEDGE.get(provider);
    }

    @Override
    public Text getDisplayName() {
        return fluid.getDefaultState().getBlockState().getBlock().getName();
    }

    @Override
    public void showResearchMessage(PlayerEntity player) {
        var tooltipText = Texts.join(FluidVariantRendering.getTooltip(FluidVariant.of(fluid)), new LiteralText("\n"));
        var hoverText = Texts.bracketed(getDisplayName()).formatted(Formatting.WHITE).styled((style) ->
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltipText)));
        player.sendMessage(new LiteralText("Researched fluid: ").formatted(Formatting.LIGHT_PURPLE).append(hoverText), false);
    }

    @Override
    public FluidBitInfo withResource(Fluid resource) {
        return new FluidBitInfo(resource, value, research, researchable, researchRequirements);
    }
}
