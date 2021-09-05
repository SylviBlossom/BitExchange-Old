package moe.sylvi.bitexchange.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;

public class SimpleStackStorage extends SingleStackStorage {
    private ItemStack stack;

    public SimpleStackStorage(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    protected ItemStack getStack() {
        return stack;
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
