package moe.sylvi.bitexchange.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface C2SPacket {
    void write(PacketByteBuf buf);

    Identifier getId();

    default void send() {
        var buf = PacketByteBufs.create();

        write(buf);

        ClientPlayNetworking.send(getId(), buf);
    }
}
