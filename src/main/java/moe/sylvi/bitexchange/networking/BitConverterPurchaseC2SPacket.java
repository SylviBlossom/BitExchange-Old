package moe.sylvi.bitexchange.networking;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.screen.BitConverterScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BitConverterPurchaseC2SPacket implements C2SPacket {
    public static final Identifier ID = new Identifier("bitexchange", "bit_converter_purchase");

    public ItemStack itemStack;
    public SlotActionType actionType;
    public boolean rightClick;

    public BitConverterPurchaseC2SPacket(ItemStack itemStack, SlotActionType actionType, boolean rightClick) {
        this.itemStack = itemStack;
        this.actionType = actionType;
        this.rightClick = rightClick;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeItemStack(itemStack);
        buf.writeEnumConstant(actionType);
        buf.writeBoolean(rightClick);
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        var itemStack = buf.readItemStack();
        var actionType = buf.readEnumConstant(SlotActionType.class);
        var rightClick = buf.readBoolean();

        server.execute(() -> {
            if (!(player.currentScreenHandler instanceof BitConverterScreenHandler)) {
                player.sendMessage(Text.translatable("bitexchange.converter.purchase.no_gui").formatted(Formatting.RED), false);
            }

            var screenHandler = (BitConverterScreenHandler) player.currentScreenHandler;

            screenHandler.receivePurchase(itemStack, actionType, rightClick, player);
        });
    }
}
