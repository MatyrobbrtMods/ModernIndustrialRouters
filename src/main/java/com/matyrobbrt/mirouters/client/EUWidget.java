package com.matyrobbrt.mirouters.client;

import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EUWidget extends AbstractWidget {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.parse("modern_industrialization:textures/gui/container/slot_atlas.png");

    private final IEnergyStorage storage;
    public EUWidget(IEnergyStorage storage, int pX, int pY) {
        super(pX, pY, 13, 18, Component.empty());
        this.storage = storage;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        int amount = this.getScaled();
        graphics.blit(TEXTURE_LOCATION, this.getX(), this.getY(), 230.0F, 0.0F, this.width, this.height, 256, 256);
        graphics.blit(TEXTURE_LOCATION, this.getX(), this.getY() + 18 - amount, 243.0F, 18 - amount, this.width, amount, 256, 256);
        if (this.isHovered()) {
            Component text = Component.literal(MiscUtil.commify(this.storage.getEnergyStored()) + " / " + MiscUtil.commify(this.storage.getMaxEnergyStored()) + " EU");
            graphics.renderTooltip(Minecraft.getInstance().font, text, pMouseX, pMouseY);
        }
    }

    private int getScaled() {
        return this.storage.getMaxEnergyStored() <= 0 ? this.height : (int)((long)this.storage.getEnergyStored() * (long)this.height / (long)this.storage.getMaxEnergyStored());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
