package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.storage.BitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;

import java.text.DecimalFormat;

public class BitHelper {
    public static String format(double bits) {
        return new DecimalFormat("#,##0.##").format(bits);
    }

    public static double convertToBits(double maxAmount, ContainerItemContext context, Transaction transaction) {
        Item item = context.getItemVariant().getItem();

        BitStorage storage = context.find(BitStorages.ITEM);
        if (storage != null) {
            try (Transaction storageTransaction = (transaction == null ? Transaction.openOuter() : transaction.openNested())) {
                double extracted = storage.extract(maxAmount, storageTransaction);
                storageTransaction.commit();
                return extracted;
            }
        } else {
            double value = BitRegistries.ITEM.getValue(item);
            if (value > 0) {
                maxAmount = Math.min(context.getAmount() * value, maxAmount);
                long toExtract = (long)Math.floor(maxAmount / value);
                try (Transaction storageTransaction = (transaction == null ? Transaction.openOuter() : transaction.openNested())) {
                    long extracted = context.extract(context.getItemVariant(), toExtract, storageTransaction);
                    storageTransaction.commit();
                    return extracted * value;
                }
            }
        }
        return 0.0;
    }

    public static double fixBitRounding(double bits, double compareTo) {
        if (bits > 0 && bits < compareTo && Math.round(bits * 100) == Math.round(compareTo * 100)) {
            return compareTo;
        }
        return bits;
    }

    public static boolean compareBits(double a, double b) {
        return fixBitRounding(a, b) >= fixBitRounding(b, a);
    }
}
