package moe.sylvi.bitexchange;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.ResearchableBitRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BitExchangeCommands {
    private static HashSet<Item> tested = new HashSet<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("bit")
                .then(literal("knowledge")
                    .then(literal("add")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                            .executes((ctx) -> {
                                ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                BitComponents.ITEM_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).addKnowledge(itemArg.getItem(), Integer.MAX_VALUE);
                                return 1;
                            })
                        ).then(argument("fluid", StringArgumentType.string())
                            .executes((ctx) -> {
                                var fluidStr = StringArgumentType.getString(ctx, "fluid");
                                var fluid = Registry.FLUID.getOrEmpty(new Identifier(fluidStr));
                                if (!fluid.isEmpty()) {
                                    BitComponents.FLUID_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).addKnowledge(fluid.get(), Long.MAX_VALUE);
                                }
                                return 1;
                            })
                        )
                    ).then(literal("set")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                            .then(argument("count", IntegerArgumentType.integer(0))
                                .executes((ctx) -> {
                                    ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                    var knowledge = BitComponents.ITEM_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow());
                                    var knowledgeMap = knowledge.getKnowledgeMap();
                                    knowledgeMap.put(itemArg.getItem(), Math.min(LongArgumentType.getLong(ctx, "count"), BitRegistries.ITEM.getResearch(itemArg.getItem())));
                                    knowledge.setKnowledgeMap(knowledgeMap);
                                    return 1;
                                })
                            )
                        ).then(argument("fluid", StringArgumentType.string())
                            .then(argument("count", LongArgumentType.longArg(0))
                                .executes((ctx) -> {
                                    var fluidStr = StringArgumentType.getString(ctx, "fluid");
                                    var fluid = Registry.FLUID.getOrEmpty(new Identifier(fluidStr));
                                    if (!fluid.isEmpty()) {
                                        var knowledge = BitComponents.FLUID_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow());
                                        var knowledgeMap = knowledge.getKnowledgeMap();
                                        knowledgeMap.put(fluid.get(), Math.min(LongArgumentType.getLong(ctx, "count"), BitRegistries.FLUID.getResearch(fluid.get())));
                                        knowledge.setKnowledgeMap(knowledgeMap);
                                    }
                                    return 1;
                                })
                            )
                        )
                    ).then(literal("remove")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                            .executes((ctx) -> {
                                ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                BitComponents.ITEM_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).removeKnowledge(itemArg.getItem());
                                return 1;
                            })
                        ).then(argument("fluid", StringArgumentType.string())
                            .executes((ctx) -> {
                                var fluidStr = StringArgumentType.getString(ctx, "fluid");
                                var fluid = Registry.FLUID.getOrEmpty(new Identifier(fluidStr));
                                if (!fluid.isEmpty()) {
                                    BitComponents.FLUID_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).removeKnowledge(fluid.get());
                                }
                                return 1;
                            })
                        )
                    ).then(literal("complete")
                        .executes((ctx) -> {
                            completeKnowledge(BitRegistries.ITEM, ctx.getSource().getPlayerOrThrow());
                            completeKnowledge(BitRegistries.FLUID, ctx.getSource().getPlayerOrThrow());
                            return 1;
                        })
                    ).then(literal("clear")
                        .executes((ctx) -> {
                            BitComponents.ITEM_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).setKnowledgeMap(new HashMap<>());
                            BitComponents.FLUID_KNOWLEDGE.get(ctx.getSource().getPlayerOrThrow()).setKnowledgeMap(new HashMap<>());
                            return 1;
                        })
                    )
                ).then(literal("test")
                    .then(literal("clear")
                        .executes((ctx) -> {
                            tested.clear();
                            return 1;
                        })
                    )
                    .executes((ctx) -> {
                        ctx.getSource().getPlayerOrThrow().sendMessage(Text.literal("Unregistered:").formatted(Formatting.DARK_PURPLE), false);
                        int count = 0;
                        for (Item item : Registry.ITEM) {
                            if (BitRegistries.ITEM.get(item) == null && !tested.contains(item) && !IGNORE_TESTING_SET.contains(Registry.ITEM.getId(item).toString())) {
                                ctx.getSource().getPlayerOrThrow().sendMessage(item.getDefaultStack().toHoverableText(), false);
                                tested.add(item);
                                count++;
                            }
                            if (count == 10) {
                                break;
                            }
                        }
                        return 1;
                    })
                ).then(literal("debug")
                    .then(literal("clear")
                        .executes(ctx -> {
                            BitHelper.DEBUG_ITEM = null;
                            return 1;
                        })
                    ).then(literal("set")
                        .then(argument("item", ItemStackArgumentType.itemStack(registryAccess))
                            .executes(ctx -> {
                                var itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                BitHelper.DEBUG_ITEM = Registry.ITEM.getId(itemArg.getItem()).toString();
                                return 1;
                            })
                        )
                    )
                )
            );
        });
    }

    private static <R,I extends BitInfoResearchable<R>> void completeKnowledge(ResearchableBitRegistry<R, I> registry, PlayerEntity player) {
        Map<R, Long> knowledge = registry.getKnowledge(player).getKnowledgeMap();
        for (BitInfoResearchable<R> info : registry) {
            knowledge.put(info.getResource(), info.getResearch());
        }
        registry.getKnowledge(player).setKnowledgeMap(knowledge);
    }

    private static String[] IGNORE_TESTING = new String[] {
            "minecraft:air",
            "minecraft:bedrock",
            "minecraft:coal_ore",
            "minecraft:deepslate_coal_ore",
            "minecraft:iron_ore",
            "minecraft:deepslate_iron_ore",
            "minecraft:copper_ore",
            "minecraft:deepslate_copper_ore",
            "minecraft:gold_ore",
            "minecraft:deepslate_gold_ore",
            "minecraft:redstone_ore",
            "minecraft:deepslate_redstone_ore",
            "minecraft:emerald_ore",
            "minecraft:deepslate_emerald_ore",
            "minecraft:lapis_ore",
            "minecraft:deepslate_lapis_ore",
            "minecraft:diamond_ore",
            "minecraft:deepslate_diamond_ore",
            "minecraft:nether_gold_ore",
            "minecraft:nether_quartz_ore",
            "minecraft:ancient_debris",
            "minecraft:budding_amethyst",
            "minecraft:petrified_oak_slab",
            "minecraft:spawner",
            "minecraft:farmland",
            "minecraft:infested_stone",
            "minecraft:infested_cobblestone",
            "minecraft:infested_stone_bricks",
            "minecraft:infested_mossy_stone_bricks",
            "minecraft:infested_cracked_stone_bricks",
            "minecraft:infested_chiseled_stone_bricks",
            "minecraft:infested_deepslate",
            "minecraft:end_portal_frame",
            "minecraft:command_block",
            "minecraft:chipped_anvil",
            "minecraft:damaged_anvil",
            "minecraft:barrier",
            "minecraft:light",
            "minecraft:repeating_command_block",
            "minecraft:chain_command_block",
            "minecraft:structure_void",
            "minecraft:structure_block",
            "minecraft:jigsaw",
            "minecraft:chainmail_helmet",
            "minecraft:chainmail_chestplate",
            "minecraft:chainmail_leggings",
            "minecraft:chainmail_boots",
            "minecraft:filled_map",
            "minecraft:written_book",
            "minecraft:wither_skeleton_skull", //potential
            "minecraft:player_head",
            "minecraft:dragon_head", //potential
            "minecraft:firework_star",
            "minecraft:enchanted_book",
            "minecraft:command_block_minecart",
            "minecraft:splash_potion",
            "minecraft:tipped_arrow",
            "minecraft:lingering_potion",
            "minecraft:knowledge_book",
            "minecraft:debug_stick",
            "minecraft:suspicious_stew",
            "minecraft:skull_banner_pattern",
            "minecraft:globe_banner_pattern",
            "minecraft:piglin_banner_pattern",
            "minecraft:bee_nest",
            "minecraft:small_amethyst_bud",
            "minecraft:medium_amethyst_bud",
            "minecraft:large_amethyst_bud",
            "minecraft:amethyst_cluster",
            "minecraft:reinforced_deepslate",
            "minecraft:frogspawn",
            "minecraft:chorus_plant"
    };
    private static HashSet<String> IGNORE_TESTING_SET = new HashSet<>(Arrays.asList(IGNORE_TESTING));
}
