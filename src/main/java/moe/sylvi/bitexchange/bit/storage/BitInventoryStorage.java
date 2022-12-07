package moe.sylvi.bitexchange.bit.storage;

import moe.sylvi.bitexchange.bit.BitHelper;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class BitInventoryStorage extends SnapshotParticipant<Double> implements IBitStorage {
    private final IBitInventory inventory;

    public BitInventoryStorage(IBitInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public double getBits() {
        return inventory.getBits();
    }

    @Override
    public double getMaxBits() {
        return inventory.getMaxBits();
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public double insert(double maxAmount, TransactionContext transaction) {
        double inserted = Math.max(0, Math.min(getMaxBits() - getBits(), maxAmount));
        updateSnapshots(transaction);
        inventory.increment(inserted, transaction);
        return inserted;
    }

    @Override
    public double extract(double maxAmount, TransactionContext transaction) {
        double extracted = Math.max(0, Math.min(getBits(), maxAmount));
        updateSnapshots(transaction);
        inventory.decrement(extracted, transaction);
        return BitHelper.fixBitRounding(extracted, maxAmount);
    }

    @Override
    protected Double createSnapshot() {
        return inventory.getBits();
    }

    @Override
    protected void readSnapshot(Double snapshot) {
        inventory.setBits(snapshot);
    }

    @Override
    protected void onFinalCommit() {
        inventory.markDirty();
    }
}
