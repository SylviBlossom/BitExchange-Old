package moe.sylvi.bitexchange.data;

import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.data.api.BitProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;

public class BitExchangeFluidBitProvider extends BitProvider {
    public BitExchangeFluidBitProvider(FabricDataGenerator generator) {
        super(generator);
    }

    @Override
    public void build() {
        var builder = fluidBuilder(new Identifier(BitExchange.MOD_ID, "fluids/minecraft")).noOverride();
        builder.register(Fluids.WATER, 1,  2);
        builder.register(Fluids.LAVA,  64, 8);
    }
}
