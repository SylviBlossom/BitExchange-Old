package moe.sylvi.bitexchange;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.steven.indrev.registry.PacketRegistry;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.bit.info.BitInfoResearchable;
import moe.sylvi.bitexchange.bit.registry.ResearchableBitRegistry;
import moe.sylvi.bitexchange.bit.registry.builder.*;
import moe.sylvi.bitexchange.bit.storage.IBitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import moe.sylvi.bitexchange.block.*;
import moe.sylvi.bitexchange.block.entity.*;
import moe.sylvi.bitexchange.compat.indrev.IRCompat;
import moe.sylvi.bitexchange.compat.kubejs.BitExchangeKubeJSCompat;
import moe.sylvi.bitexchange.compat.modern_industrialization.MICompat;
import moe.sylvi.bitexchange.inventory.IBitConsumerInventory;
import moe.sylvi.bitexchange.item.BitArrayInventory;
import moe.sylvi.bitexchange.item.BitDatabaseItem;
import moe.sylvi.bitexchange.item.BitInscriptionItem;
import moe.sylvi.bitexchange.networking.BitRegistryS2CPacket;
import moe.sylvi.bitexchange.screen.*;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.impl.networking.PacketCallbackListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

// literal("foo")
import static net.minecraft.server.command.CommandManager.literal;
// argument("bar", word())
import static net.minecraft.server.command.CommandManager.argument;

public class BitExchange implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();

    public static boolean KUBEJS_LOADED = false;

    public static final String MOD_ID = "bitexchange";
    public static final String MOD_NAME = "Bit Exchange";

    public static final Item BIT_ARRAY_ITEM;
    public static final Item BIT_INSCRIPTION_ITEM;
    public static final Item BIT_DATABASE_ITEM;
    public static final Item BIT_ITEM;
    public static final Item BYTE_ITEM;
    public static final Item KILOBIT_ITEM;
    public static final Item MEGABIT_ITEM;
    public static final Item GIGABIT_ITEM;
    public static final Item TERABIT_ITEM;
    public static final Item PETABIT_ITEM;
    public static final Item EXABIT_ITEM;
    public static final Item ITTY_BIT_ITEM;
    public static final Block BIT_CONVERTER_BLOCK;
    public static final Block BIT_RESEARCHER_BLOCK;
    public static final Block BIT_FACTORY_BLOCK;
    public static final Block BIT_LIQUEFIER_BLOCK;
    public static final Block BIT_MINER_BLOCK;
    public static final Block BYTE_MINER_BLOCK;
    public static final Block KILOBIT_MINER_BLOCK;
    public static final Block MEGABIT_MINER_BLOCK;
    public static final Block GIGABIT_MINER_BLOCK;
    public static final Block TERABIT_MINER_BLOCK;
    public static final Block PETABIT_MINER_BLOCK;
    public static final Block EXABIT_MINER_BLOCK;
    public static final Block ITTY_BIT_MINER_BLOCK;
    public static final BlockItem BIT_CONVERTER_BLOCK_ITEM;
    public static final BlockItem BIT_RESEARCHER_BLOCK_ITEM;
    public static final BlockItem BIT_FACTORY_BLOCK_ITEM;
    public static final BlockItem BIT_LIQUEFIER_BLOCK_ITEM;
    public static final BlockItem BIT_MINER_BLOCK_ITEM;
    public static final BlockItem BYTE_MINER_BLOCK_ITEM;
    public static final BlockItem KILOBIT_MINER_BLOCK_ITEM;
    public static final BlockItem MEGABIT_MINER_BLOCK_ITEM;
    public static final BlockItem GIGABIT_MINER_BLOCK_ITEM;
    public static final BlockItem TERABIT_MINER_BLOCK_ITEM;
    public static final BlockItem PETABIT_MINER_BLOCK_ITEM;
    public static final BlockItem EXABIT_MINER_BLOCK_ITEM;
    public static final BlockItem ITTY_BIT_MINER_BLOCK_ITEM;
    public static final BlockEntityType<BitConverterBlockEntity> BIT_CONVERTER_BLOCK_ENTITY;
    public static final BlockEntityType<BitResearcherBlockEntity> BIT_RESEARCHER_BLOCK_ENTITY;
    public static final BlockEntityType<BitFactoryBlockEntity> BIT_FACTORY_BLOCK_ENTITY;
    public static final BlockEntityType<BitLiquefierBlockEntity> BIT_LIQUEFIER_BLOCK_ENTITY;
    public static final BlockEntityType<BitMinerBlockEntity> BIT_MINER_BLOCK_ENTITY;
    public static final ScreenHandlerType<BitConverterScreenHandler> BIT_CONVERTER_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitResearcherScreenHandler> BIT_RESEARCHER_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitFactoryScreenHandler> BIT_FACTORY_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitLiquefierScreenHandler> BIT_LIQUEFIER_SCREEN_HANDLER;
    public static final ScreenHandlerType<BitMinerScreenHandler> BIT_MINER_SCREEN_HANDLER;

    static {
        BIT_ARRAY_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_array"), new Item(new FabricItemSettings().group(ItemGroup.MISC).maxCount(1)));
        BIT_INSCRIPTION_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_inscription"), new BitInscriptionItem());
        BIT_DATABASE_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_database"), new BitDatabaseItem());

        BIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        BYTE_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "byte"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        KILOBIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "kilobit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        MEGABIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "megabit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        GIGABIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gigabit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        TERABIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "terabit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        PETABIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "petabit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        EXABIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "exabit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));
        ITTY_BIT_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "itty_bit"), new Item(new FabricItemSettings().group(ItemGroup.MISC)));

        BIT_CONVERTER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_converter"), new BitConverterBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_RESEARCHER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_researcher"), new BitResearcherBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_FACTORY_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_factory"), new BitFactoryBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BIT_LIQUEFIER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_liquefier"), new BitLiquefierBlock(FabricBlockSettings.of(Material.WOOL).strength(1.0f).luminance(BitLiquefierBlock::getLuminance).nonOpaque()));
        BIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "bit_miner"), new BitMinerBlock(BIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        BYTE_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "byte_miner"), new BitMinerBlock(BYTE_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        KILOBIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "kilobit_miner"), new BitMinerBlock(KILOBIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        MEGABIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "megabit_miner"), new BitMinerBlock(MEGABIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        GIGABIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "gigabit_miner"), new BitMinerBlock(GIGABIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        TERABIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "terabit_miner"), new BitMinerBlock(TERABIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        PETABIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "petabit_miner"), new BitMinerBlock(PETABIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        EXABIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "exabit_miner"), new BitMinerBlock(EXABIT_ITEM, 40, FabricBlockSettings.of(Material.WOOL).strength(1.0f)));
        ITTY_BIT_MINER_BLOCK = Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "itty_bit_miner"), new IttyBitMinerBlock(ITTY_BIT_ITEM, 1, FabricBlockSettings.of(Material.WOOL).strength(0.75f)));

        BIT_CONVERTER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_converter"), new BlockItem(BIT_CONVERTER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_RESEARCHER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_researcher"), new BlockItem(BIT_RESEARCHER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_FACTORY_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_factory"), new BlockItem(BIT_FACTORY_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_LIQUEFIER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_liquefier"), new BlockItem(BIT_LIQUEFIER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "bit_miner"), new BlockItem(BIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        BYTE_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "byte_miner"), new BlockItem(BYTE_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        KILOBIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "kilobit_miner"), new BlockItem(KILOBIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        MEGABIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "megabit_miner"), new BlockItem(MEGABIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        GIGABIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "gigabit_miner"), new BlockItem(GIGABIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        TERABIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "terabit_miner"), new BlockItem(TERABIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        PETABIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "petabit_miner"), new BlockItem(PETABIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        EXABIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "exabit_miner"), new BlockItem(EXABIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));
        ITTY_BIT_MINER_BLOCK_ITEM = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "itty_bit_miner"), new BlockItem(ITTY_BIT_MINER_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

        BIT_CONVERTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_converter"), FabricBlockEntityTypeBuilder.create(BitConverterBlockEntity::new, BIT_CONVERTER_BLOCK).build(null));
        BIT_RESEARCHER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_researcher"), FabricBlockEntityTypeBuilder.create(BitResearcherBlockEntity::new, BIT_RESEARCHER_BLOCK).build(null));
        BIT_FACTORY_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_factory"), FabricBlockEntityTypeBuilder.create(BitFactoryBlockEntity::new, BIT_FACTORY_BLOCK).build(null));
        BIT_LIQUEFIER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_liquefier"), FabricBlockEntityTypeBuilder.create(BitLiquefierBlockEntity::new, BIT_LIQUEFIER_BLOCK).build(null));
        BIT_MINER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "bit_miner"), FabricBlockEntityTypeBuilder.create(BitMinerBlockEntity::new,
                BIT_MINER_BLOCK, BYTE_MINER_BLOCK, KILOBIT_MINER_BLOCK, MEGABIT_MINER_BLOCK, GIGABIT_MINER_BLOCK, TERABIT_MINER_BLOCK, PETABIT_MINER_BLOCK, EXABIT_MINER_BLOCK, ITTY_BIT_MINER_BLOCK).build(null));

        BIT_CONVERTER_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "bit_converter"), new ScreenHandlerType<>(BitConverterScreenHandler::new));
        BIT_RESEARCHER_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "bit_researcher"), new ScreenHandlerType<>(BitResearcherScreenHandler::new));
        BIT_FACTORY_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "bit_factory"), new ScreenHandlerType<>(BitFactoryScreenHandler::new));
        BIT_LIQUEFIER_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "bit_liquefier"), new ExtendedScreenHandlerType<>(BitLiquefierScreenHandler::new));
        BIT_MINER_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(MOD_ID, "bit_miner"), new ExtendedScreenHandlerType<>(BitMinerScreenHandler::new));

        BitStorages.ITEM.registerForItems((stack, context) -> IBitStorage.of(new BitArrayInventory(Double.MAX_VALUE, context)), BIT_ARRAY_ITEM);

        BitRegistries.ITEM.registerBuilder(new ItemDataRegistryBuilder(BitRegistries.ITEM));
        BitRegistries.ITEM.registerBuilder(new ItemRecipeRegistryBuilder(BitRegistries.ITEM));
        BitRegistries.ITEM.registerBuilder(new FluidContainerItemRegistryBuilder(BitRegistries.ITEM));

        BitRegistries.FLUID.registerBuilder(new FluidDataRegistryBuilder(BitRegistries.FLUID));
        BitRegistries.FLUID.registerBuilder(new FluidRecipeRegistryBuilder(BitRegistries.FLUID));

        FluidStorage.SIDED.registerForBlockEntity((be, side) -> be.combinedStorage, BIT_LIQUEFIER_BLOCK_ENTITY);
        FluidStorage.SIDED.registerFallback((world, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IBitConsumerInventory inventory) {
                return inventory.getInputFluid();
            }
            return null;
        });
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");

        if (FabricLoader.getInstance().isModLoaded("modern_industrialization")) {
            MICompat.load();
        }
        if (FabricLoader.getInstance().isModLoaded("indrev")) {
            IRCompat.load();
        }
        if (FabricLoader.getInstance().isModLoaded("kubejs")) {
            KUBEJS_LOADED = true;
            BitExchangeKubeJSCompat.load();
        }

        AutoConfig.register(BitConfig.class, GsonConfigSerializer::new);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MOD_ID, "bit_registry");
            }

            @Override
            public void reload(ResourceManager manager) {
                AbstractDataRegistryBuilder.loadResources(manager);
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (var registry : BitRegistries.REGISTRY) {
                new BitRegistryS2CPacket(registry).send(handler.player);
            }
        });

        BitExchangeNetworking.registerServerGlobalReceivers();

        BitExchangeCommands.register();
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable e) {
        LOGGER.error(message + "\n" + e.getMessage());
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Throwable e) {
        LOGGER.warn(message + "\n" + e.getMessage());
    }
}