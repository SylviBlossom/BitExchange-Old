package moe.sylvi.bitexchange.bit;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.bit.storage.IBitStorage;
import moe.sylvi.bitexchange.bit.storage.BitStorages;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class BitHelper {
    public static String DEBUG_ITEM = null;

    public static String format(double bits) {
        return new DecimalFormat("#,##0.##").format(bits);
    }

    public static double convertToBits(double maxAmount, ContainerItemContext context, Transaction transaction) {
        Item item = context.getItemVariant().getItem();

        IBitStorage storage = context.find(BitStorages.ITEM);
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

    public static BitResource parseResourceId(String id) throws Throwable {
        return parseResourceId(id, BitRegistries.ITEM);
    }

    public static BitResource parseResourceId(String id, @Nullable BitRegistry defaultRegistry) throws Throwable {
        BitRegistry registryRef = defaultRegistry;

        int registryIndex = id.indexOf("$");
        if (registryIndex >= 0) {
            registryRef = BitRegistries.REGISTRY.get(new Identifier(id.substring(0, registryIndex)));
            if (registryRef == null) {
                throw new Exception("Bit registry not found: " + id.substring(0, registryIndex));
            }
            id = id.substring(registryIndex + 1);
        }

        double count = registryRef.getEmpty().getRatio();

        int multIndex = id.indexOf("*");
        if (multIndex >= 0) {
            count *= Double.parseDouble(id.substring(multIndex + 1));
            id = id.substring(0, multIndex);
        }

        int divIndex = id.lastIndexOf("/");
        if (divIndex >= 0) {
            try {
                count /= Double.parseDouble(id.substring(divIndex + 1));
                id = id.substring(0, divIndex);
            } catch (Exception ignored) { }
        }

        String finalId = id;
        Object resourceRef = registryRef.getResourceRegistry().getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

        return BitResource.of(registryRef, resourceRef, count);
    }

    public static List<BitResource> parseMultiResourceId(String id) throws Throwable {
        return parseMultiResourceId(id, BitRegistries.ITEM);
    }

    public static List<BitResource> parseMultiResourceId(String id, @Nullable BitRegistry defaultRegistry) throws Throwable {
        List<BitResource> list = Lists.newArrayList();

        for (String subId : id.split("\\|")) {
            BitRegistry registryRef = defaultRegistry;

            int registryIndex = subId.indexOf("$");
            if (registryIndex >= 0) {
                registryRef = BitRegistries.REGISTRY.get(new Identifier(subId.substring(0, registryIndex)));
                if (registryRef == null) {
                    throw new Exception("Bit registry not found: " + subId.substring(0, registryIndex));
                }
                subId = subId.substring(registryIndex + 1);
            }

            double count = registryRef.getEmpty().getRatio();

            int multIndex = subId.indexOf("*");
            if (multIndex >= 0) {
                count *= Double.parseDouble(subId.substring(multIndex + 1));
                subId = subId.substring(0, multIndex);
            }

            int divIndex = subId.lastIndexOf("/");
            if (divIndex >= 0) {
                try {
                    count /= Double.parseDouble(subId.substring(divIndex + 1));
                    subId = subId.substring(0, divIndex);
                } catch (Exception ignored) { }
            }

            if (subId.startsWith("#")) {

                var tag = TagKey.of(registryRef.getResourceRegistry().getKey(), new Identifier(subId));

                String finalSubId = subId;
                var tagValues = (RegistryEntryList.Named)(registryRef.getResourceRegistry().getEntryList(tag).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported tag '" + finalSubId + "'")));

                for (Object entry : tagValues) {
                    BitResource resource = BitResource.of(registryRef, ((RegistryEntry)entry).value(), count);
                    if (!list.contains(resource)) {
                        list.add(resource);
                    }
                }
            } else {
                String finalId = subId;
                Object resourceRef = registryRef.getResourceRegistry().getOrEmpty(new Identifier(subId)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));

                if (!list.contains(resourceRef)) {
                    list.add(BitResource.of(registryRef, resourceRef, count));
                }
            }
        }

        return list;
    }

    public static <R, I extends BitInfo<R>> boolean isDebugging(R resource, BitRegistry<R, I> registry) {
        if (DEBUG_ITEM == null) {
            return false;
        }
        var id = registry.getResourceRegistry().getId(resource);
        if (id == null) {
            return false;
        }
        return id.toString().equals(DEBUG_ITEM);
    }

    public static <R, I extends BitInfo<R>> String getItemId(R resource, BitRegistry<R, I> registry) {
        return registry.getResourceRegistry().getId(resource).toString();
    }
}
