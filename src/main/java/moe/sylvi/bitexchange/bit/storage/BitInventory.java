package moe.sylvi.bitexchange.bit.storage;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public interface BitInventory {
    static BitInventory of(double capacity) {
        return new BitInventoryImpl(capacity);
    }

    double getBits();

    double getMaxBits();

    default void setBits(double amount) {
        setBits(amount, null);
    }
    void setBits(double amount, TransactionContext transaction);

    default boolean isEmpty() {
        return getBits() == 0;
    }

    default void markDirty() {
    }

    default void increment(double amount) {
        increment(amount, null);
    }
    default void increment(double amount, TransactionContext transaction) {
        setBits(Math.min(getMaxBits(), getBits() + amount), transaction);
    }

    default void decrement(double amount) {
        decrement(amount, null);
    }
    default void decrement(double amount, TransactionContext transaction) {
        setBits(Math.max(0, getBits() - amount), transaction);
    }
}
