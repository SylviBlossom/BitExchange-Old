package moe.sylvi.bitexchange.item;

import moe.sylvi.bitexchange.bit.storage.BitInventory;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;

public class BitArrayInventory implements BitInventory {
    private final ContainerItemContext context;
    private final double capacity;

    public BitArrayInventory(double capacity, ContainerItemContext context) {
        this.capacity = capacity;
        this.context = context;
    }

    @Override
    public double getBits() {
        ItemVariant variant = context.getItemVariant();
        NbtCompound nbt = variant.getNbt();
        if (nbt != null && nbt.contains("Bits")) {
            return nbt.getDouble("Bits");
        }
        return 0;
    }

    @Override
    public void setBits(double amount, TransactionContext transaction) {
        if (transaction == null) {
            return;
        }

        Item item = context.getItemVariant().getItem();
        NbtCompound nbt = context.getItemVariant().copyNbt();

        if (nbt == null) {
            nbt = new NbtCompound();
        }
        nbt.putDouble("Bits", amount);

        try (Transaction innerTransaction = transaction.openNested()) {
            context.exchange(ItemVariant.of(item, nbt), 1, innerTransaction);
            innerTransaction.commit();
        }
    }

    @Override
    public double getMaxBits() {
        return capacity;
    }
}
