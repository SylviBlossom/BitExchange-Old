package moe.sylvi.bitexchange.data.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.BitRegistries;
import moe.sylvi.bitexchange.bit.info.BitInfo;
import moe.sylvi.bitexchange.bit.registry.BitRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.server.AbstractTagProvider;
import net.minecraft.data.server.RecipeProvider;
import net.minecraft.item.Items;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.List;

public abstract class BitProvider implements DataProvider {
    protected DataGenerator.PathResolver pathResolver;
    protected List<BitProviderBuilder> builders;

    public BitProvider(FabricDataGenerator generator) {
        pathResolver = generator.createPathResolver(DataGenerator.OutputType.DATA_PACK, "bit_registry");
        builders = Lists.newArrayList();
    }

    public abstract void build();

    public BitProviderItemBuilder itemBuilder(Identifier path) {
        var builder = new BitProviderItemBuilder(path);
        builders.add(builder);
        return builder;
    }

    public BitProviderFluidBuilder fluidBuilder(Identifier path) {
        var builder = new BitProviderFluidBuilder(path);
        builders.add(builder);
        return builder;
    }

    @Override
    public String getName() {
        return "Bit Values";
    }

    @Override
    public void run(DataWriter writer) throws IOException {
        build();
        for (var builder : builders) {
            BitExchange.log(Level.INFO, "Writing " + pathResolver.resolveJson(builder.getPath()));
            DataProvider.writeToPath(writer, builder.build(), pathResolver.resolveJson(builder.getPath()));
        }
        //pathResolver = DataProvider.writeToPath(writer,);
    }
}
