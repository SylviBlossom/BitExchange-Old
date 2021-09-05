package moe.sylvi.bitexchange.bit.storage;

import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class BitStorages {
    public static final BlockApiLookup<BitStorage, Direction> SIDED =
            BlockApiLookup.get(new Identifier(BitExchange.MOD_ID, "sided_bit_storage"), BitStorage.class, Direction.class);

    public static final ItemApiLookup<BitStorage, ContainerItemContext> ITEM =
            ItemApiLookup.get(new Identifier(BitExchange.MOD_ID, "bit_storage"), BitStorage.class, ContainerItemContext.class);
}
