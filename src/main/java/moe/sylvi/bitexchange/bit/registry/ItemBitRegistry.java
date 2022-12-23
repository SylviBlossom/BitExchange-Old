package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class ItemBitRegistry extends SimpleBitRegistry<Item, ItemBitInfo> implements ResearchableBitRegistry<Item, ItemBitInfo> {
    public ItemBitRegistry(Registry<Item> resourceRegistry) {
        super(resourceRegistry, BitInfo.ofItem(Items.AIR, 1, 1, true, false));
    }

    @Override
    public <V> BitKnowledge<Item> getKnowledge(V provider) {
        return BitComponents.ITEM_KNOWLEDGE.get(provider);
    }

    public boolean isAutomatable(Item item) {
        ItemBitInfo info = get(item);
        return info != null && info.isAutomatable();
    }

    @Override
    public void writeInfo(ItemBitInfo info, PacketByteBuf buf) {
        buf.writeIdentifier(Registry.ITEM.getId(info.getResource()));
        buf.writeDouble(info.getValue());
        buf.writeLong(info.getResearch());
        buf.writeBoolean(info.isResearchable());
        buf.writeBoolean(info.isAutomatable());

        buf.writeVarInt(info.getResearchRequirements().size());
        for (var requirement : info.getResearchRequirements()) {
            requirement.writeToPacket(buf);
        }
    }

    @Override
    public ItemBitInfo readInfo(PacketByteBuf buf) {
        Item item = Registry.ITEM.get(buf.readIdentifier());
        double value = buf.readDouble();
        long research = buf.readLong();
        boolean researchable = buf.readBoolean();
        boolean automatable = buf.readBoolean();

        int size = buf.readVarInt();
        var requirements = new ArrayList<ResearchRequirement>(size);
        for (int i = 0; i < size; i++) {
            requirements.add(ResearchRequirement.readFromPacket(buf));
        }

        return new ItemBitInfo(item, value, research, researchable, automatable, requirements);
    }
}
