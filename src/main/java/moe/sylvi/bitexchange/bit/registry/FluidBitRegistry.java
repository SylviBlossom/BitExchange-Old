package moe.sylvi.bitexchange.bit.registry;

import moe.sylvi.bitexchange.BitComponents;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.info.FluidBitInfo;
import moe.sylvi.bitexchange.bit.info.ItemBitInfo;
import moe.sylvi.bitexchange.bit.research.BitKnowledge;
import moe.sylvi.bitexchange.bit.research.ResearchRequirement;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class FluidBitRegistry extends SimpleBitRegistry<Fluid, FluidBitInfo> implements ResearchableBitRegistry<Fluid, FluidBitInfo> {
    public FluidBitRegistry(Registry<Fluid> resourceRegistry) {
        super(resourceRegistry, BitInfo.ofFluid(Fluids.EMPTY, 1, 1, FluidConstants.BUCKET, true));
    }

    @Override
    public <V> BitKnowledge<Fluid> getKnowledge(V provider) {
        return BitComponents.FLUID_KNOWLEDGE.get(provider);
    }

    @Override
    public void writeInfo(FluidBitInfo info, PacketByteBuf buf) {
        buf.writeIdentifier(Registry.FLUID.getId(info.getResource()));
        buf.writeDouble(info.getValue());
        buf.writeLong(info.getResearch());
        buf.writeLong(info.getRatio());
        buf.writeBoolean(info.isResearchable());

        buf.writeVarInt(info.getResearchRequirements().size());
        for (var requirement : info.getResearchRequirements()) {
            requirement.writeToPacket(buf);
        }
    }

    @Override
    public FluidBitInfo readInfo(PacketByteBuf buf) {
        Fluid fluid = Registry.FLUID.get(buf.readIdentifier());
        double value = buf.readDouble();
        long research = buf.readLong();
        long ratio = buf.readLong();
        boolean researchable = buf.readBoolean();

        int size = buf.readVarInt();
        var requirements = new ArrayList<ResearchRequirement>(size);
        for (int i = 0; i < size; i++) {
            requirements.add(ResearchRequirement.readFromPacket(buf));
        }

        return new FluidBitInfo(fluid, value, research, ratio, researchable, requirements);
    }
}
