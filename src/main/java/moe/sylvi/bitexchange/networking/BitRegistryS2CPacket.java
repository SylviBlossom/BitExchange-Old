package moe.sylvi.bitexchange.networking;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class BitRegistryS2CPacket implements S2CPacket {
    public static final Identifier ID = new Identifier(BitExchange.MOD_ID, "bit_registry");

    public BitRegistry registry;

    public BitRegistryS2CPacket(BitRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(BitRegistries.REGISTRY.getId(registry));

        var infoList = registry.getList();
        buf.writeVarInt(infoList.size());

        for (var info : infoList) {
            registry.writeInfo((BitInfo)info, buf);
        }
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        var registryId = buf.readIdentifier();

        if (!BitRegistries.REGISTRY.containsId(registryId)) {
            BitExchange.error("Received bit registry for unknown registry " + registryId);
            return;
        }

        var registry = BitRegistries.REGISTRY.get(registryId);

        var infoCount = buf.readVarInt();
        var infoList = new ArrayList<BitInfo>();

        for (int i = 0; i < infoCount; i++) {
            var info = registry.readInfo(buf);
            infoList.add(info);
        }

        client.execute(() -> {
            registry.load(infoList);
        });
    }
}
