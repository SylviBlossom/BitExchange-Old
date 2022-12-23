package moe.sylvi.bitexchange.mixin;

import moe.sylvi.bitexchange.BitExchangeNetworking;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.networking.BitRegistryS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow public abstract PlayerManager getPlayerManager();

    @Inject(method = "reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), cancellable = true)
    private void mixinReloadResources(Collection<String> datapacks, CallbackInfoReturnable<CompletableFuture<Void>> info) {
        info.setReturnValue(info.getReturnValue().thenRun(() -> {
            BitRegistries.build((MinecraftServer)(Object)this);
            for (var player : getPlayerManager().getPlayerList()) {
                for (var registry : BitRegistries.REGISTRY) {
                    new BitRegistryS2CPacket(registry).send(player);
                }
            }
        }));
    }
    @Inject(method = "loadWorld()V", at = @At("HEAD"))
    private void mixinLoadWorld(CallbackInfo info) {
        BitRegistries.build((MinecraftServer)(Object)this);
    }
}
