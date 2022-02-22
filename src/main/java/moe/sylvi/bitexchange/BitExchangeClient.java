package moe.sylvi.bitexchange;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.storage.BitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.client.gui.*;
import moe.sylvi.bitexchange.mixin.SpriteMixin;
import moe.sylvi.bitexchange.render.BitLiquefierRenderer;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelAppender;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.text.Format;
import java.util.List;

public class BitExchangeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(BitExchange.BIT_LIQUEFIER_BLOCK_ENTITY, ctx -> new BitLiquefierRenderer());

        BlockRenderLayerMap.INSTANCE.putBlock(BitExchange.BIT_LIQUEFIER_BLOCK, RenderLayer.getCutout());

        ScreenRegistry.register(BitExchange.BIT_CONVERTER_SCREEN_HANDLER, BitConverterScreen::new);
        ScreenRegistry.register(BitExchange.BIT_RESEARCHER_SCREEN_HANDLER, BitResearcherScreen::new);
        ScreenRegistry.register(BitExchange.BIT_FACTORY_SCREEN_HANDLER, BitFactoryScreen::new);
        ScreenRegistry.register(BitExchange.BIT_LIQUEFIER_SCREEN_HANDLER, BitLiquefierScreen::new);
        ScreenRegistry.register(BitExchange.BIT_MINER_SCREEN_HANDLER, BitMinerScreen::new);

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (MinecraftClient.getInstance() == null) {
                return;
            }
            Item item = stack.getItem();
            SimpleItemContext storageContext = new SimpleItemContext(stack);
            BitStorage storage = storageContext.find(BitStorages.ITEM);
            if (storage != null) {
                String countText = BitHelper.format(storage.getBits());
                if (storage.getMaxBits() != Double.MAX_VALUE) {
                    countText += " / " + BitHelper.format(storage.getMaxBits());
                }
                lines.add(new LiteralText("Stored: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(countText).formatted(Formatting.WHITE)));
            }
            Screen screen = MinecraftClient.getInstance().currentScreen;
            boolean bitScreen = (screen instanceof BitConverterScreen) ||
                    (screen instanceof BitResearcherScreen) ||
                    (screen instanceof BitFactoryScreen);
            BitConfig config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();
            PlayerEntity player = MinecraftClient.getInstance().player;
            var itemInfo = BitRegistries.ITEM.get(item);
            if (itemInfo != null) {
                long research = BitComponents.ITEM_KNOWLEDGE.get(player).getKnowledge(item);
                long maxResearch = itemInfo.getResearch();
                if ((research >= maxResearch || config.showUnlearnedValues) && (storage == null || Screen.hasShiftDown())) {
                    MutableText text = new LiteralText("Bits: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(BitHelper.format(itemInfo.getValue())).formatted(Formatting.YELLOW));
                    if (config.showUnlearnedValues) {
                        if (!itemInfo.isResearchable()) {
                            text.append(new LiteralText(" [" + (research < maxResearch ? "???" : "⭐") + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                        } else {
                            text.append(new LiteralText(" [" + research + "/" + maxResearch + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                        }
                    }
                    lines.add(text);
                    if (Screen.hasShiftDown() || bitScreen) {
                        if (stack.getCount() > 1) {
                            lines.add(new LiteralText("- Stack: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(BitHelper.format(itemInfo.getValue() * stack.getCount())).formatted(Formatting.YELLOW)));
                        }
                        if (itemInfo.isAutomatable()) {
                            lines.add(new LiteralText("- ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("Automatable").formatted(Formatting.DARK_PURPLE)));
                        }
                        if (config.showUnlearnedValues && itemInfo.isResearchable()) {
                            addResearchRequirementLines(true, itemInfo, player, lines);
                        }
                    }
                } else if (research < maxResearch) {
                    MutableText text = new LiteralText("Unlearned").formatted(Formatting.DARK_PURPLE);
                    if (!itemInfo.isResearchable()) {
                        text.append(new LiteralText(" ⭐").formatted(Formatting.DARK_PURPLE));
                    } else if (Screen.hasShiftDown() || bitScreen) {
                        text.append(new LiteralText(" [" + research + "/" + maxResearch + "]").formatted(Formatting.DARK_GRAY));
                    }
                    lines.add(text);
                    if (itemInfo.isResearchable() && (Screen.hasShiftDown() || bitScreen)) {
                        addResearchRequirementLines(false, itemInfo, player, lines);
                    }
                }
            }
            var fluidStorage = storageContext.find(FluidStorage.ITEM);
            if (fluidStorage != null) {
                var resourceAmount = getStoredResourceAmount(fluidStorage, null);
                if (resourceAmount != null && !resourceAmount.resource().isBlank()) {
                    var resource = resourceAmount.resource();
                    var amount = resourceAmount.amount();

                    var fluid = resource.getFluid();
                    var fluidInfo = BitRegistries.FLUID.get(fluid);
                    if (fluidInfo != null) {
                        var research = BitComponents.FLUID_KNOWLEDGE.get(player).getKnowledge(fluid);
                        var maxResearch = fluidInfo.getResearch();

                        var displayAmount = (double)amount / FluidConstants.BUCKET;
                        var displayResearch = BitHelper.format((double)research / FluidConstants.BUCKET) + "B";
                        var displayMax = BitHelper.format((double)maxResearch / FluidConstants.BUCKET) + "B";

                        var name = fluidInfo.getDisplayName().shallowCopy();
                        var fluidSpriteColor = ColorHelper.swapRedBlueIfNeeded(((SpriteMixin)FluidVariantRendering.getSprite(resource)).bitexchange_getImages()[0].getColor(0, 0));
                        var fluidColor = ColorHelper.multiplyColor(fluidSpriteColor, FluidVariantRendering.getColor(resource));
                        var fluidText = name.formatted(getClosestFormatting(fluidColor));
                        if (research >= maxResearch || config.showUnlearnedValues) {
                            var text = new LiteralText("Bits | ").formatted(Formatting.LIGHT_PURPLE).append(fluidText.shallowCopy())
                                    .append(new LiteralText(": ").formatted(Formatting.LIGHT_PURPLE))
                                    .append(new LiteralText(BitHelper.format(fluidInfo.getValue() * displayAmount)).formatted(Formatting.YELLOW))
                                    .append(new LiteralText(" (").formatted(Formatting.WHITE))
                                    .append(new LiteralText(BitHelper.format(fluidInfo.getValue()) + "/B").formatted(Formatting.YELLOW))
                                    .append(new LiteralText(")").formatted(Formatting.WHITE));
                            if (config.showUnlearnedValues) {
                                if (!fluidInfo.isResearchable()) {
                                    text.append(new LiteralText(" [" + (research < maxResearch ? "???" : "⭐") + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                                } else {
                                    text.append(new LiteralText(" [" + displayResearch + "/" + displayMax + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                                }
                            }
                            lines.add(text);
                            if ((screen.hasShiftDown() || bitScreen) && config.showUnlearnedValues && fluidInfo.isResearchable()) {
                                addResearchRequirementLines(true, fluidInfo, player, lines);
                            }
                        } else if (research < maxResearch) {
                            var text = new LiteralText("Unlearned | ").formatted(Formatting.DARK_PURPLE).append(fluidText.shallowCopy());
                            if (!fluidInfo.isResearchable()) {
                                text.append(new LiteralText(" ⭐").formatted(Formatting.DARK_PURPLE));
                            } else if (Screen.hasShiftDown() || bitScreen) {
                                text.append(new LiteralText(" [" + displayResearch + "/" + displayMax + "]").formatted(Formatting.DARK_GRAY));
                            }
                            lines.add(text);
                            if (fluidInfo.isResearchable() && (Screen.hasShiftDown() || bitScreen)) {
                                addResearchRequirementLines(true, fluidInfo, player, lines);
                            }
                        }
                    }
                }
            }
        });
    }

    public static void addResearchRequirementLines(boolean header, BitInfoResearchable info, PlayerEntity player, List<Text> lines) {
        List<ResearchRequirement> requirements = info.getResearchRequirements();

        boolean anyFailed = false;

        List<Text> newLines = Lists.newArrayList();
        for (ResearchRequirement requirement : requirements) {
            requirement.createTooltip(player, newLines, Formatting.DARK_GRAY, Formatting.GREEN);
            if (!requirement.isMet(player)) {
                anyFailed = true;
            }
        }
        if (!newLines.isEmpty()) {
            if (anyFailed) {
                lines.add(new LiteralText("Requirements:").formatted(Formatting.LIGHT_PURPLE));
                lines.addAll(newLines);
            } else {
                lines.add(new LiteralText("Requirements: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("✔").formatted(Formatting.GREEN)));
            }
        }
    }

    private <T> ResourceAmount<T> getStoredResourceAmount(Storage<T> storage, @Nullable TransactionContext transaction) {
        try (Transaction innerTransaction = Transaction.openNested(transaction)) {
            for (var view : storage.iterable(innerTransaction)) {
                if (!view.isResourceBlank()) {
                    return new ResourceAmount<>(view.getResource(), view.getAmount());
                }
            }
        }
        return null;
    }

    public static Formatting getClosestFormatting(Integer color) {
        var closest = Formatting.WHITE;
        var closestDist = -1D;
        for (var formatting : Formatting.values()) {
            if (!formatting.isColor()) {
                continue;
            }
            var dist = colorDistance(color, formatting.getColorValue());
            if (closestDist < 0 || dist < closestDist) {
                closest = formatting;
                closestDist = dist;
            }
        }
        return closest;
    }

    // https://www.compuphase.com/cmetric.htm
    private static double colorDistance(Integer c1, Integer c2)
    {
        long r1 = ((c1 & 0xFF0000) >> 16);
        long r2 = ((c2 & 0xFF0000) >> 16);
        long rmean = (r1 + r2) / 2;
        long r = r1 - r2;
        long g = ((c1 & 0x00FF00) >> 8) - ((c2 & 0x00FF00) >> 8);
        long b = (c1 & 0x0000FF) - (c2 & 0x0000FF);
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }
}
