package moe.sylvi.bitexchange;

import com.google.common.collect.Lists;
import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import moe.sylvi.bitexchange.bit.storage.BitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.transfer.SimpleItemContext;
import moe.sylvi.bitexchange.client.gui.BitConverterScreen;
import moe.sylvi.bitexchange.client.gui.BitFactoryScreen;
import moe.sylvi.bitexchange.client.gui.BitMinerScreen;
import moe.sylvi.bitexchange.client.gui.BitResearcherScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class BitExchangeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BitExchange.BIT_CONVERTER_SCREEN_HANDLER, BitConverterScreen::new);
        ScreenRegistry.register(BitExchange.BIT_RESEARCHER_SCREEN_HANDLER, BitResearcherScreen::new);
        ScreenRegistry.register(BitExchange.BIT_FACTORY_SCREEN_HANDLER, BitFactoryScreen::new);
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
            if (BitRegistries.ITEM.get(item) != null) {
                Screen screen = MinecraftClient.getInstance().currentScreen;
                boolean bitScreen = (screen instanceof BitConverterScreen) ||
                                    (screen instanceof BitResearcherScreen) ||
                                    (screen instanceof BitFactoryScreen);
                BitConfig config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();
                PlayerEntity player = MinecraftClient.getInstance().player;
                long research = BitComponents.ITEM_KNOWLEDGE.get(player).getKnowledge(item);
                long maxResearch = BitRegistries.ITEM.getResearch(item);
                if ((research >= maxResearch || config.showUnlearnedValues) && (storage == null || Screen.hasShiftDown())) {
                    MutableText text = new LiteralText("Bits: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(BitHelper.format(BitRegistries.ITEM.getValue(item))).formatted(Formatting.YELLOW));
                    if (config.showUnlearnedValues) {
                        text.append(new LiteralText(" [" + research + "/" + maxResearch + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                    }
                    lines.add(text);
                    if (Screen.hasShiftDown() || bitScreen) {
                        if (stack.getCount() > 1) {
                            lines.add(new LiteralText("- Stack: ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText(BitHelper.format(BitRegistries.ITEM.getValue(item) * stack.getCount())).formatted(Formatting.YELLOW)));
                        }
                        if (BitRegistries.ITEM.isAutomatable(item)) {
                            lines.add(new LiteralText("- ").formatted(Formatting.LIGHT_PURPLE).append(new LiteralText("Automatable").formatted(Formatting.DARK_PURPLE)));
                        }
                        if (config.showUnlearnedValues) {
                            addResearchRequirementLines(true, item, player, lines);
                        }
                    }
                } else if (research < maxResearch) {
                    MutableText text = new LiteralText("Unlearned").formatted(Formatting.DARK_PURPLE);
                    if (Screen.hasShiftDown() || bitScreen) {
                        text.append(new LiteralText(" [" + research + "/" + maxResearch + "]").formatted(Formatting.DARK_GRAY));
                    }
                    lines.add(text);
                    if (Screen.hasShiftDown() || bitScreen) {
                        addResearchRequirementLines(false, item, player, lines);
                    }
                }
            }
        });
    }

    private void addResearchRequirementLines(boolean header, Item item, PlayerEntity player, List<Text> lines) {
        List<ResearchRequirement> requirements = BitRegistries.ITEM.getResearchRequirements(item);

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
}
