package moe.sylvi.bitexchange.bit.registry.builder;

import moe.sylvi.bitexchange.bit.info.BitInfo;
import net.minecraft.server.MinecraftServer;

public interface BitRegistryBuilder<R, I extends BitInfo<R>> {
    int getPriority();

    void prepare(MinecraftServer server);

    I process(R resource);

    void postProcess();
}
