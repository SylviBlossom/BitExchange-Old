package moe.sylvi.bitexchange;

import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.client.gui.*;
import moe.sylvi.bitexchange.mixin.SpriteMixin;
import moe.sylvi.bitexchange.render.BitLiquefierRenderer;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class BitExchangeClient implements ClientModInitializer {
    private static final Style LIGHT_STYLE = Style.EMPTY.withColor(0xBE87FF);
    private static final Style DARK_STYLE = Style.EMPTY.withColor(0x6244AD);
    private static final Style WHITE_STYLE = Style.EMPTY.withColor(0xFFFFFF);
    //private static final Style WHITE_STYLE = Style.EMPTY.withColor(0xEFEAD7);
    private static final Style GRAY_STYLE = Style.EMPTY.withColor(0x514668);
    private static final Style GOLD_STYLE = Style.EMPTY.withColor(0xFFF587);

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(BitExchange.BIT_LIQUEFIER_BLOCK_ENTITY, ctx -> new BitLiquefierRenderer());

        BlockRenderLayerMap.INSTANCE.putBlock(BitExchange.BIT_LIQUEFIER_BLOCK, RenderLayer.getCutout());

        HandledScreens.register(BitExchange.BIT_CONVERTER_SCREEN_HANDLER, BitConverterScreen::new);
        HandledScreens.register(BitExchange.BIT_RESEARCHER_SCREEN_HANDLER, BitResearcherScreen::new);
        HandledScreens.register(BitExchange.BIT_FACTORY_SCREEN_HANDLER, BitFactoryScreen::new);
        HandledScreens.register(BitExchange.BIT_LIQUEFIER_SCREEN_HANDLER, BitLiquefierScreen::new);
        HandledScreens.register(BitExchange.BIT_MINER_SCREEN_HANDLER, BitMinerScreen::new);

        BitExchangeNetworking.registerClientGlobalReceivers();

        ItemTooltipCallback.EVENT.register(this::buildTooltip);
    }

    public static void addResearchRequirementLines(boolean header, BitInfoResearchable info, PlayerEntity player, List<Text> lines) {
        List<ResearchRequirement> requirements = info.getResearchRequirements();

        boolean anyFailed = false;

        List<Text> newLines = new ArrayList<>();
        for (ResearchRequirement requirement : requirements) {
            requirement.createTooltip(player, newLines, GRAY_STYLE, GOLD_STYLE);
            if (!requirement.isMet(player)) {
                anyFailed = true;
            }
        }
        if (!newLines.isEmpty()) {
            if (anyFailed) {
                lines.add(Text.literal("Requirements:").setStyle(LIGHT_STYLE));
                lines.addAll(newLines);
            } else {
                lines.add(Text.literal("Requirements: ").setStyle(LIGHT_STYLE).append(Text.literal("✔").setStyle(GOLD_STYLE)));
            }
        }
    }

    private <T> ResourceAmount<T> getStoredResourceAmount(Storage<T> storage) {
        for (var view : storage) {
            if (!view.isResourceBlank()) {
                return new ResourceAmount<>(view.getResource(), view.getAmount());
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

    private void buildTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        if (MinecraftClient.getInstance() == null) {
            return;
        }

        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        var player = MinecraftClient.getInstance().player;

        var storageContext = new SimpleItemContext(stack);

        // If the item is a bit storage (e.g. a bit array), display the amount of bits stored
        var bitStorage = storageContext.find(BitStorages.ITEM);
        if (bitStorage != null) {
            var countText = BitHelper.format(bitStorage.getBits());
            if (bitStorage.getMaxBits() != Double.MAX_VALUE) {
                countText += " / " + BitHelper.format(bitStorage.getMaxBits());
            }
            lines.add(Text.literal("Stored: ").setStyle(LIGHT_STYLE).append(Text.literal(countText).setStyle(WHITE_STYLE)));
        }

        var itemInfo = BitRegistries.ITEM.get(stack.getItem());
        if (itemInfo != null) {
            buildItemTooltip(stack, lines, itemInfo, bitStorage != null);
        }

        var fluidStorage = storageContext.find(FluidStorage.ITEM);
        if (fluidStorage != null) {
            buildFluidTooltip(lines, config, fluidStorage);
        }
    }

    private void buildItemTooltip(ItemStack stack, List<Text> lines, ItemBitInfo itemInfo, boolean isBitStorage) {
        var config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();

        var player = MinecraftClient.getInstance().player;
        var screen = MinecraftClient.getInstance().currentScreen;

        var isBitConsumer = (screen instanceof BitConverterScreen) || (screen instanceof BitFactoryScreen);
        var isBitScreen   = isBitConsumer || (screen instanceof BitResearcherScreen);

        if (!itemInfo.isAutomatable() && !config.shouldSupportCraftables()) {
            return;
        }

        var research = BitComponents.ITEM_KNOWLEDGE.get(player).getKnowledge(stack.getItem());
        var maxResearch = itemInfo.getResearch();
        if ((research >= maxResearch || config.showUnlearnedValues) && (!isBitStorage || Screen.hasShiftDown())) {
            var buyValue = BitHelper.getItemValue(stack, BitHelper.PurchaseMode.BUY);
            var sellValue = BitHelper.getItemValue(stack, BitHelper.PurchaseMode.SELL);

            var testStyle = Style.EMPTY.withColor(0xBBBBBB);

            var text = Text.literal((buyValue == sellValue) ? "Bits: " : "Bit value: ").setStyle(LIGHT_STYLE).append(Text.literal(BitHelper.format(sellValue)).setStyle(GOLD_STYLE));
            if (config.showUnlearnedValues && !isBitConsumer) {
                if (!itemInfo.isResearchable()) {
                    text.append(Text.literal(" [" + (research < maxResearch ? "???" : "⭐") + "]").setStyle((research < maxResearch) ? GRAY_STYLE : DARK_STYLE));
                } else {
                    text.append(Text.literal(" [" + research + "/" + maxResearch + "]").setStyle((research < maxResearch) ? GRAY_STYLE : DARK_STYLE));
                }
            }
            lines.add(text);

            if (buyValue != sellValue) {
                lines.add(Text.literal("Bit cost: ").setStyle(LIGHT_STYLE).append(Text.literal(BitHelper.format(buyValue)).setStyle(GOLD_STYLE)));
            }

            if (Screen.hasShiftDown() || isBitScreen) {
                if (stack.getCount() > 1) {
                    lines.add(Text.literal((buyValue == sellValue) ? "- Stack: " : "- Stack value: ").setStyle(LIGHT_STYLE).append(Text.literal(BitHelper.format(sellValue * stack.getCount())).setStyle(GOLD_STYLE)));
                }
                if (itemInfo.isAutomatable()) {
                    lines.add(Text.literal("- ").setStyle(LIGHT_STYLE).append(Text.literal("Automatable").setStyle(LIGHT_STYLE)));
                }
                if (config.showUnlearnedValues && itemInfo.isResearchable()) {
                    addResearchRequirementLines(true, itemInfo, player, lines);
                }
            }
        } else if (research < maxResearch) {
            var text = Text.literal("Unlearned").setStyle(DARK_STYLE);
            if (!itemInfo.isResearchable()) {
                text.append(Text.literal(" ⭐").setStyle(DARK_STYLE));
            } else if (Screen.hasShiftDown() || isBitScreen) {
                text.append(Text.literal(" [" + research + "/" + maxResearch + "]").setStyle(GRAY_STYLE));
            }
            lines.add(text);
            if (itemInfo.isResearchable() && (Screen.hasShiftDown() || isBitScreen)) {
                addResearchRequirementLines(false, itemInfo, player, lines);
            }
        }
    }

    private void buildFluidTooltip(List<Text> lines, BitConfig config, Storage<FluidVariant> fluidStorage) {
        var resourceAmount = getStoredResourceAmount(fluidStorage);

        if (resourceAmount == null || resourceAmount.resource().isBlank()) {
            return;
        }

        var resource = resourceAmount.resource();
        var amount = resourceAmount.amount();

        var fluid = resource.getFluid();
        var fluidInfo = BitRegistries.FLUID.get(fluid);

        if (fluidInfo == null) {
            return;
        }

        var player = MinecraftClient.getInstance().player;

        var screen = MinecraftClient.getInstance().currentScreen;
        var isBitScreen = (screen instanceof BitConverterScreen) ||
                (screen instanceof BitResearcherScreen) ||
                (screen instanceof BitFactoryScreen);

        var research = BitComponents.FLUID_KNOWLEDGE.get(player).getKnowledge(fluid);
        var maxResearch = fluidInfo.getResearch();

        var displayAmount = (double) amount / FluidConstants.BUCKET;
        var displayResearch = BitHelper.format((double) research / FluidConstants.BUCKET) + "B";
        var displayMax = BitHelper.format((double) maxResearch / FluidConstants.BUCKET) + "B";

        var name = fluidInfo.getDisplayName().copy();
        var fluidSpriteColor = ColorHelper.swapRedBlueIfNeeded(((SpriteMixin) FluidVariantRendering.getSprite(resource)).bitexchange_getImages()[0].getColor(0, 0));
        var fluidColor = ColorHelper.multiplyColor(fluidSpriteColor, FluidVariantRendering.getColor(resource));
        var fluidText = name.styled(style -> style.withColor(fluidColor));

        if (research >= maxResearch || config.showUnlearnedValues) {
            var text = Text.literal("Bits").setStyle(LIGHT_STYLE).append(Text.literal(" | ").setStyle(GRAY_STYLE)).append(fluidText.copy())
                    .append(Text.literal(": ").styled(style -> style.withColor(fluidColor)))
                    .append(Text.literal(BitHelper.format(fluidInfo.getValue() * displayAmount)).setStyle(GOLD_STYLE))
                    .append(Text.literal(" (").setStyle(GRAY_STYLE))
                    .append(Text.literal(BitHelper.format(fluidInfo.getValue()) + "/B").setStyle(WHITE_STYLE))
                    .append(Text.literal(")").setStyle(GRAY_STYLE));
            if (config.showUnlearnedValues) {
                if (!fluidInfo.isResearchable()) {
                    text.append(Text.literal(" [" + (research < maxResearch ? "???" : "⭐") + "]").setStyle((research < maxResearch) ? GRAY_STYLE : DARK_STYLE));
                } else {
                    text.append(Text.literal(" [" + displayResearch + "/" + displayMax + "]").setStyle((research < maxResearch) ? GRAY_STYLE : DARK_STYLE));
                }
            }
            lines.add(text);
            if ((Screen.hasShiftDown() || isBitScreen) && config.showUnlearnedValues && fluidInfo.isResearchable()) {
                addResearchRequirementLines(true, fluidInfo, player, lines);
            }
        } else {
            var text = Text.literal("Unlearned").setStyle(DARK_STYLE).append(Text.literal(" | ").setStyle(GRAY_STYLE)).append(fluidText.copy());
            if (!fluidInfo.isResearchable()) {
                text.append(Text.literal(" ⭐").setStyle(DARK_STYLE));
            } else if (Screen.hasShiftDown() || isBitScreen) {
                text.append(Text.literal(" [" + displayResearch + "/" + displayMax + "]").setStyle(GRAY_STYLE));
            }
            lines.add(text);
            if (fluidInfo.isResearchable() && (Screen.hasShiftDown() || isBitScreen)) {
                addResearchRequirementLines(true, fluidInfo, player, lines);
            }
        }
    }
}
