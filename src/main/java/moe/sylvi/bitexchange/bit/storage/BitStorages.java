package moe.sylvi.bitexchange.bit.storage;

import moe.sylvi.bitexchange.BitExchange;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class BitStorages {
    public static final BlockApiLookup<IBitStorage, Direction> SIDED =
            BlockApiLookup.get(new Identifier(BitExchange.MOD_ID, "sided_bit_storage"), IBitStorage.class, Direction.class);

    public static final ItemApiLookup<IBitStorage, ContainerItemContext> ITEM =
            ItemApiLookup.get(new Identifier(BitExchange.MOD_ID, "bit_storage"), IBitStorage.class, ContainerItemContext.class);
}
