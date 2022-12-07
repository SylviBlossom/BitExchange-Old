package moe.sylvi.bitexchange.render;

import com.google.common.collect.Lists;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.block.entity.BitLiquefierBlockEntity;
import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.client.model.ExtraModelProvider;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;
import net.fabricmc.fabric.impl.client.model.ModelLoadingRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.fabricmc.fabric.impl.renderer.RendererAccessImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelUtil;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BitLiquefierRenderer implements BlockEntityRenderer<BitLiquefierBlockEntity> {
    private static final Identifier BIT_LIQUEFIER_MODEL_ID = new Identifier(BitExchange.MOD_ID, "block/bit_liquefier");
    private static final float EDGE_SIZE = 3f / 16f;
    private static final float INNER_SIZE = 1f - (EDGE_SIZE * 2f);

    @Override
    public void render(BitLiquefierBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        render(entity.getStoredVariant(), entity.getFillPercent(), entity.getWorld(), entity.getPos(), matrices, vertexConsumers, light, overlay);
    }

    private void render(FluidVariant variant, float percent, @Nullable BlockRenderView view, @Nullable BlockPos pos, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (percent == 0f || variant.isBlank()) {
            return;
        }

        matrices.push();

        var handler = FluidVariantRendering.getHandlerOrDefault(variant.getFluid());
        var attribHandler = FluidVariantAttributes.getHandlerOrDefault(variant.getFluid());

        var sprite = handler.getSprites(variant)[0];
        var color = handler.getColor(variant, view, pos);
        var flipped = attribHandler.isLighterThanAir(variant);
        var luminance = variant.getFluid().getDefaultState().getBlockState().getLuminance();

        var renderer = RendererAccessImpl.INSTANCE.getRenderer();
        var consumer = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

        var builder = renderer.meshBuilder();
        var emitter = builder.getEmitter();

        var newColor = ColorHelper.swapRedBlueIfNeeded(color);

        emitFluidFace(emitter, sprite, newColor, flipped, Direction.UP, 1f, flipped ? 0f : (1f - percent));
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.DOWN, 1f, flipped ? (1f - percent) : 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.NORTH, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.EAST, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.SOUTH, percent, 0f);
        emitFluidFace(emitter, sprite, newColor, flipped, Direction.WEST, percent, 0f);

        var mesh = builder.build();

        var newLight = (light & 0xFFFF_0000) | (Math.max((light >> 4) & 0xF, luminance) << 4);
        for (List<BakedQuad> quads : ModelHelper.toQuadLists(mesh)) {
            renderQuads(quads, matrices, consumer, newLight, overlay);
        }

        matrices.pop();
    }

    private void emitFluidFace(QuadEmitter emitter, Sprite sprite, int color, boolean flipped, Direction direction, float height, float depth) {
        var minU = sprite.getMinU();
        var minV = sprite.getMinV();

        var uMult = sprite.getMaxU() - minU;
        var vMult = sprite.getMaxV() - minV;

        var bottomleft = flipped ? (1f - EDGE_SIZE - (height * INNER_SIZE)) : EDGE_SIZE;
        var right = 1f - EDGE_SIZE;
        var top = flipped ? (1f - EDGE_SIZE) : (EDGE_SIZE + (height * INNER_SIZE));
        var deep = EDGE_SIZE + (depth * INNER_SIZE);

        emitter.square(direction, bottomleft, bottomleft, right, top, deep);
        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_ROTATE_NONE);
        emitter.spriteColor(0, color, color, color, color);
        emitter.sprite(0, 0, minU + bottomleft * uMult, minV + (1f - top) * vMult);
        emitter.sprite(1, 0, minU + bottomleft * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(2, 0, minU + right * uMult, minV + (1f - bottomleft) * vMult);
        emitter.sprite(3, 0, minU + right * uMult, minV + (1f - top) * vMult);
        emitter.emit();
    }

    private void renderQuads(List<BakedQuad> quads, MatrixStack matrices, VertexConsumer consumer, int light, int overlay) {
        for (BakedQuad bq : quads) {
            float[] brightness = new float[] {1f, 1f, 1f, 1f};
            int[] lights = new int[]{light, light, light, light};
            consumer.quad(matrices.peek(), bq, brightness, 1f, 1f, 1f, lights, overlay, true);
        }
    }
}
