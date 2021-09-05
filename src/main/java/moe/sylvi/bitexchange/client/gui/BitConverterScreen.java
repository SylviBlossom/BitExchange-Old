package moe.sylvi.bitexchange.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import moe.sylvi.bitexchange.BitExchange;
import moe.sylvi.bitexchange.bit.BitHelper;
import moe.sylvi.bitexchange.screen.BitConverterScreenHandler;
import moe.sylvi.bitexchange.screen.slot.SlotInput;
import moe.sylvi.bitexchange.screen.slot.SlotStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;

import java.util.Objects;

public class BitConverterScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("bitexchange", "textures/gui/container/bit_converter.png");
    public static final SimpleInventory INVENTORY = new SimpleInventory(32);
    private TextFieldWidget searchBox;
    private Text bitText;
    private float scrollAmount;
    private boolean scrolling;

    public BitConverterScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, LiteralText.EMPTY);
    }

    @Override
    protected void init() {
        backgroundWidth = 176;
        backgroundHeight = 212;
        super.init();
        scrollAmount = 0f;
        scrolling = false;

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        searchBox = new TextFieldWidget(client.textRenderer, x + 9, y + 9, 142, 9, new TranslatableText("itemGroup.search"));
        searchBox.setMaxLength(50);
        searchBox.setDrawsBackground(false);
        searchBox.setVisible(true);
        searchBox.setFocusUnlocked(false);
        searchBox.setTextFieldFocused(true);
        searchBox.setEditableColor(0xFFFFFF);
        searchBox.setText("");
        addSelectableChild(searchBox);

        client.keyboard.setRepeatEvents(true);
    }

    @Override
    public void removed() {
        client.keyboard.setRepeatEvents(false);
    }

    @Override
    public void tick() {
        super.tick();
        getHandler().buildList(searchBox.getText(), scrollAmount);
        updateBitText();
        if (!this.shouldScroll() && this.scrollAmount > 0) {
            this.scrollAmount = 0;
            getHandler().scrollItems(0);
        }
        searchBox.tick();
    }

    public BitConverterScreenHandler getHandler() {
        return (BitConverterScreenHandler)this.handler;
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
        int k = (int)(71.0F * scrollAmount);
        drawTexture(matrices, x + 156, y + 8 + k, 176 + (shouldScroll() ? 0 : 12), 0, 12, 15);
        searchBox.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        searchBox.x = x + 9;
        searchBox.y = y + 9;
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        if (bitText != null) {
            textRenderer.draw(matrices, bitText, 29, 108, 0xFFFFFF);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        String string = this.searchBox.getText();
        if (this.searchBox.charTyped(chr, keyCode)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                scrollAmount = 0;
                getHandler().buildList(this.searchBox.getText(), scrollAmount);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String string = searchBox.getText();
        if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, searchBox.getText())) {
                scrollAmount = 0;
                getHandler().buildList(this.searchBox.getText(), scrollAmount);
            }
            return true;
        } else {
            return searchBox.isFocused() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        if (shouldScroll() && mouseX >= x + 155 && mouseY >= y + 7 && mouseX < x + 169 && mouseY < y + 95) {
            scrolling = true;
        }
        if (searchBox.isMouseOver(mouseX, mouseY) && button == 1) {
            searchBox.setText("");
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (shouldScroll() && scrolling) {
            int i = y + 8;
            int j = i + 85;
            scrollAmount = ((float)mouseY - (float)i - 7.5F) / ((float)(j - i) - 15.0F);
            scrollAmount = MathHelper.clamp(scrollAmount, 0.0F, 1.0F);
            getHandler().scrollItems(scrollAmount);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (this.shouldScroll()) {
            int i = this.getMaxScroll();
            BitExchange.log(Level.INFO, "Scroll: " + (amount / (double)i));
            this.scrollAmount = (float)((double)this.scrollAmount - amount / (double)i);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
            getHandler().scrollItems(scrollAmount);
        }

        return true;
    }

    @Override
    protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
        if (slot != null && slot.inventory == INVENTORY && !searchBox.getText().isEmpty()) {
            searchBox.setSelectionStart(0);
            searchBox.setSelectionEnd(searchBox.getText().length());
        }
        super.onMouseClick(slot, invSlot, clickData, actionType);
    }

    private void drawRedSlots(MatrixStack matrices, int x, int y) {
        if (!getScreenHandler().getCursorStack().isEmpty()) {
            drawRedSlotsForStack(matrices, x, y, getScreenHandler().getCursorStack());
        } else if (focusedSlot != null) {
            if (focusedSlot.inventory == getHandler().getPlayerInventory() && !focusedSlot.getStack().isEmpty()) {
                drawRedSlotsForStack(matrices, x, y, focusedSlot.getStack());
            } else {
                for (int i = 0; i < 2; i++) {
                    if (focusedSlot == handler.slots.get(i)) {
                        int start = BitConverterScreenHandler.PLAYER_SLOT;
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
        for (int i = 0; i < 2; i++) {
            if (!handler.slots.get(i).canInsert(stack)) {
                drawRedSlot(matrices, x, y, handler.slots.get(i));
            }
        }
    }

    private void drawRedSlot(MatrixStack matrices, int x, int y, Slot slot) {
        if (slot instanceof SlotInput) {
            drawTexture(matrices, x + slot.x - 1, y + slot.y - 1, backgroundWidth + 32, 15, 18, 18);
        } else if (slot instanceof SlotStorage) {
            drawTexture(matrices, x + slot.x, y + slot.y , backgroundWidth + 16, 15, 16, 16);
        } else {
            drawTexture(matrices, x + slot.x, y + slot.y, backgroundWidth, 15, 16, 16);
        }
    }

    public void updateBitText() {
        double bits = getHandler().getBits();
        if (bits >= 0) {
            bitText = new LiteralText("Bits: " + BitHelper.format(bits)).formatted(Formatting.DARK_PURPLE);
        } else {
            bitText = new LiteralText("Insert Bit Array").formatted(Formatting.RED);
        }
    }

    public boolean shouldScroll() {
        return getHandler().itemList.size() > 32;
    }

    public int getMaxScroll() {
        return Math.max(1, (int)Math.ceil((getHandler().itemList.size() - 32) / (double)8));
    }
}
