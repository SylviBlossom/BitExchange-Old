package moe.sylvi.bitexchange.mixin;

import moe.sylvi.bitexchange.BitRegistries;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "reloadResources(Ljava/util/Collection;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), cancellable = true)
    private void mixinReloadResources(Collection<String> datapacks, CallbackInfoReturnable<CompletableFuture<Void>> info) {
        info.setReturnValue(info.getReturnValue().thenRun(() -> {
            BitRegistries.build((MinecraftServer)(Object)this);
        }));
    }
    @Inject(method = "loadWorld()V", at = @At("HEAD"))
    private void mixinLoadWorld(CallbackInfo info) {
        BitRegistries.build((MinecraftServer)(Object)this);
    }
}
