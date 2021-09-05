package moe.sylvi.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.sylvi.bitexchange.screen.BitMinerScreenHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BitMinerScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_miner.png");

    public BitMinerScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
        int p = (int)Math.floor(((double)getHandler().getMiningProgress() / getHandler().getMiningSpeed()) * 13);
        drawTexture(matrices, x + 62, y + 36 + (13 - p), 0, backgroundHeight + (13 - p), 52, p);
    }

    private BitMinerScreenHandler getHandler() {
        return (BitMinerScreenHandler)handler;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        // Center the title
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}
