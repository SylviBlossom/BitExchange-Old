package moe.sylvi.bitexchange.networking;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface S2CPacket {
    void write(PacketByteBuf buf);

    Identifier getId();

    default void send(ServerPlayerEntity player) {
        var buf = PacketByteBufs.create();

        write(buf);

        ServerPlayNetworking.send(player, getId(), buf);
    }
}
