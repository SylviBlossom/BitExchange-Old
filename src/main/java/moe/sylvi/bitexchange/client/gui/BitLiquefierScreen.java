package moe.sylvi.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import moe.sylvi.bitexchange.*;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.block.entity.BitLiquefierBlockEntity;
import moe.sylvi.bitexchange.mixin.DrawableHelperMixin;
import moe.sylvi.bitexchange.mixin.SpriteMixin;
import moe.sylvi.bitexchange.screen.BitFactoryScreenHandler;
import moe.sylvi.bitexchange.screen.BitLiquefierScreenHandler;
import moe.sylvi.bitexchange.screen.slot.SlotInput;
import moe.sylvi.bitexchange.screen.slot.SlotStorage;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.TextureHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

public class BitLiquefierScreen extends HandledScreen<ScreenHandler> {
    //A path to the gui texture. In this example we use the texture from the dispenser
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_liquefier.png");
    private Text bitText;
    private float progress;

    public BitLiquefierScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        updateBitText();
        //updateProgress();
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        drawRedSlots(matrices, x, y);
        drawFluid(matrices, x + 134, y + 15, 16, 56);
        drawTexture(matrices, x + 134, y + 15, 178, 18, 16, 56);
        //drawTexture(matrices, x + 26, y + 17, 0, backgroundHeight, (int)Math.floor(88 * progress), 16);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        super.drawForeground(matrices, mouseX, mouseY);
        if (bitText != null) {
            textRenderer.draw(matrices, bitText, 88 - (textRenderer.getWidth(bitText) / 2), 38, 0xFFFFFF);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
        drawFluidTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        updateBitText();
        //updateProgress();
    }

    private BitLiquefierScreenHandler getHandler() {
        return (BitLiquefierScreenHandler)getScreenHandler();
    }

    private BitLiquefierBlockEntity getBlockEntity() {
        return (BitLiquefierBlockEntity) MinecraftClient.getInstance().world.getBlockEntity(getHandler().getPos());
    }

    private void drawFluid(MatrixStack matrices, int x, int y, int width, int height) {
        var be = getBlockEntity();
        var fluid = be.getOuputFluid().getResource();

        if (!fluid.isBlank()) {
            var percent = be.getFillPercent();
            var sprite = FluidVariantRendering.getSprite(fluid);

            var color = FluidVariantRendering.getColor(fluid);
            float r = (color >> 0x10 & 0xff) / 256f;
            float g = (color >> 0x08 & 0xff) / 256f;
            float b = (color & 0xff) / 256f;

            RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
            RenderSystem.setShaderColor(r, g, b, 1.0F);
            RenderSystem.enableBlend();

            var targetHeight = (int)Math.max(1, Math.floor(percent * height));

            var fullCount = targetHeight / sprite.getHeight();
            var remainder = targetHeight % sprite.getHeight();

            int fy = y + height;
            for (int i = 0; i < fullCount; i++) {
                drawSprite(matrices, x, fy - sprite.getHeight(), getZOffset(), width, sprite.getHeight(), sprite);
                fy -= sprite.getHeight();
            }
            if (remainder > 0) {
                DrawableHelperMixin.bitexchange_drawTexturedQuad(matrices.peek().getModel(), x, x + width, fy - remainder, fy, getZOffset(), sprite.getMinU(), sprite.getMaxU(), sprite.getMinV() + (((float)(sprite.getHeight() - remainder)/sprite.getHeight()) * (sprite.getMaxV() - sprite.getMinV())), sprite.getMaxV());
            }

            RenderSystem.disableBlend();
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private void drawFluidTooltip(MatrixStack matrices, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        if (mouseX >= x+134 && mouseX < x+134+16 && mouseY >= y+15 && mouseY < y+15+56) {
            var be = getBlockEntity();
            var resource = be.getOuputFluid().getResource();

            if (!resource.isBlank()) {
                var amount = be.getOuputFluid().getAmount();

                var tooltip = FluidVariantRendering.getTooltip(resource);

                if (amount < (FluidConstants.BUCKET/1000) || hasShiftDown()) {
                    tooltip.add(new LiteralText(amount + " dp"));
                } else {
                    var currentMB = Math.round(((double) amount / FluidConstants.BUCKET) * 1000);
                    var maxMB = Math.round(((double) be.getOuputFluid().getCapacity() / FluidConstants.BUCKET) * 1000);
                    tooltip.add(new LiteralText(currentMB + " mB / " + maxMB + " mB"));
                }

                var fluid = resource.getFluid();
                var fluidInfo = BitRegistries.FLUID.get(fluid);
                if (fluidInfo != null) {
                    var player = MinecraftClient.getInstance().player;

                    var research = BitComponents.FLUID_KNOWLEDGE.get(player).getKnowledge(fluid);
                    var maxResearch = fluidInfo.getResearch();

                    var displayAmount = (double)amount / FluidConstants.BUCKET;
                    var displayResearch = BitHelper.format((double)research / FluidConstants.BUCKET) + "B";
                    var displayMax = BitHelper.format((double)maxResearch / FluidConstants.BUCKET) + "B";

                    BitConfig config = AutoConfig.getConfigHolder(BitConfig.class).getConfig();
                    var name = fluid.getDefaultState().getBlockState().getBlock().getName().shallowCopy();
                    if (research >= maxResearch || config.showUnlearnedValues) {
                        var text = new LiteralText("Bits: ").formatted(Formatting.LIGHT_PURPLE)
                                .append(new LiteralText(BitHelper.format(fluidInfo.getValue() * displayAmount)).formatted(Formatting.YELLOW))
                                .append(new LiteralText(" (").formatted(Formatting.WHITE))
                                .append(new LiteralText(BitHelper.format(fluidInfo.getValue()) + "/B").formatted(Formatting.YELLOW))
                                .append(new LiteralText(")").formatted(Formatting.WHITE));
                        if (config.showUnlearnedValues) {
                            text.append(new LiteralText(" [" + displayResearch + "/" + displayMax + "]").formatted((research < maxResearch) ? Formatting.DARK_GRAY : Formatting.DARK_PURPLE));
                        }
                        tooltip.add(text);
                        if (config.showUnlearnedValues) {
                            BitExchangeClient.addResearchRequirementLines(true, fluidInfo, player, tooltip);
                        }
                    } else if (research < maxResearch) {
                        var text = new LiteralText("Unlearned").formatted(Formatting.DARK_PURPLE);
                        text.append(new LiteralText(" [" + displayResearch + "/" + displayMax + "]").formatted(Formatting.DARK_GRAY));
                        tooltip.add(text);
                        BitExchangeClient.addResearchRequirementLines(true, fluidInfo, player, tooltip);
                    }
                }

                renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }
    }

    private void drawRedSlots(MatrixStack matrices, int x, int y) {
        if (!getScreenHandler().getCursorStack().isEmpty()) {
            drawRedSlotsForStack(matrices, x, y, getScreenHandler().getCursorStack());
        } else if (focusedSlot != null) {
            if (focusedSlot.inventory == getHandler().getPlayerInventory() && !focusedSlot.getStack().isEmpty()) {
                drawRedSlotsForStack(matrices, x, y, focusedSlot.getStack());
            } else {
                for (int i = 0; i < 3; i++) {
                    if (focusedSlot == handler.slots.get(i)) {
                        int start = BitFactoryScreenHandler.PLAYER_SLOT;
                        for (int j = start; j < handler.slots.size(); j++) {
                            ItemStack stack = handler.slots.get(j).getStack();
                            if (!stack.isEmpty() && !handler.slots.get(i).canInsert(stack)) {
                                drawRedSlot(matrices, x, y, handler.slots.get(j));
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawRedSlotsForStack(MatrixStack matrices, int x, int y, ItemStack stack) {
        for (int i = 0; i < 3; i++) {
            if (!handler.slots.get(i).canInsert(stack)) {
                drawRedSlot(matrices, x, y, handler.slots.get(i));
            }
        }
    }

    private void drawRedSlot(MatrixStack matrices, int x, int y, Slot slot) {
        if (slot instanceof SlotInput) {
            drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, backgroundWidth + 32, 0, 18, 18);
        } else if (slot instanceof SlotStorage) {
            drawTexture(matrices, x + slot.x, y + slot.y , backgroundWidth + 16, 0, 16, 16);
        } else {
            drawTexture(matrices, x + slot.x, y + slot.y, backgroundWidth, 0, 16, 16);
        }
    }

    private void updateBitText() {
        double bits = getHandler().getBits();
        if (bits >= 0) {
            bitText = new LiteralText("Bits: " + BitHelper.format(bits)).formatted(Formatting.DARK_PURPLE);
        } else {
            bitText = new LiteralText("Insert Bit Array").formatted(Formatting.RED);
        }
    }

    /*private void updateProgress() {
        double bits = getHandler().getBits();
        double maxBits = getHandler().getResourceBits();
        if (bits >= 0 && maxBits > 0) {
            progress = (float)Math.min(1f, bits / maxBits);
        } else {
            progress = 0f;
        }
    }*/
}
