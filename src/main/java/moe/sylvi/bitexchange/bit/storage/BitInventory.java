package moe.sylvi.bitexchange.bit.storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class BitInventory implements IBitInventory {
    private final double capacity;
    private double bits;

    public BitInventory(double capacity) {
        this.capacity = capacity;
        this.bits = 0;
    }

    @Override
    public double getBits() {
        return bits;
    }

    @Override
    public double getMaxBits() {
        return capacity;
    }

    @Override
    public void setBits(double amount, TransactionContext transaction) {
        bits = Math.max(0, Math.min(getMaxBits(), amount));
    }
}
