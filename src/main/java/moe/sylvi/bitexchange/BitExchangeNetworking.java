package moe.sylvi.bitexchange;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import moe.sylvi.bitexchange.networking.BitConverterPurchaseC2SPacket;
import moe.sylvi.bitexchange.networking.BitRegistryS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;

public class BitExchangeNetworking {

    public static void registerServerGlobalReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(BitConverterPurchaseC2SPacket.ID, BitConverterPurchaseC2SPacket::receive);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientGlobalReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BitRegistryS2CPacket.ID, BitRegistryS2CPacket::receive);
    }
}
