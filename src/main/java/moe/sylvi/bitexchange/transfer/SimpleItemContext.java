package moe.sylvi.bitexchange.transfer;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class SimpleItemContext implements ContainerItemContext {
    private SimpleStackStorage slot;

    public SimpleItemContext(ItemStack stack) {
        this.slot = new SimpleStackStorage(stack);
    }

    @Override
    public <A> A find(ItemApiLookup<A, ContainerItemContext> lookup) {
        return slot.isResourceBlank() ? null : lookup.find(slot.getStack(), this);
    }

    public ItemStack getStack() {
        return slot.getStack();
    }

    public void setStack(ItemStack stack) {
        slot.setStack(stack);
    }

    @Override
    public SingleSlotStorage<ItemVariant> getMainSlot() {
        return slot;
    }

    @Override
    public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
        return Lists.newArrayList();
    }

    @Override
    public World getWorld() {
        return null;
    }
}
