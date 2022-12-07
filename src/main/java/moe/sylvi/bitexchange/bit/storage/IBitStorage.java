package moe.sylvi.bitexchange.bit.storage;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface IBitStorage extends SingleSlotStorage<Object> {
    static IBitStorage of(IBitInventory inventory) {
        return new BitInventoryStorage(inventory);
    }

    double getBits();
    double getMaxBits();
    boolean isEmpty();

    double insert(double maxAmount, TransactionContext transaction);
    double extract(double maxAmount, TransactionContext transaction);

    @Override
    @Deprecated
    default Object getResource() {
        return null;
    }

    @Override
    @Deprecated
    default long getAmount() {
        return Double.doubleToLongBits(getBits());
    }

    @Override
    @Deprecated
    default long getCapacity() {
        return Double.doubleToLongBits(getMaxBits());
    }

    @Override
    @Deprecated
    default boolean isResourceBlank() {
        return isEmpty();
    }

    @Override
    @Deprecated
    default long insert(Object resource, long maxAmount, TransactionContext transaction) {
        double result = insert(Double.longBitsToDouble(maxAmount), transaction);
        return Double.doubleToLongBits(result);
    }

    @Override
    @Deprecated
    default long extract(Object resource, long maxAmount, TransactionContext transaction) {
        double result = extract(Double.longBitsToDouble(maxAmount), transaction);
        return Double.doubleToLongBits(result);
    }

    @Override
    @Nullable
    default StorageView<Object> exactView(Object resource) {
        return this;
    }
}
