package com.matyrobbrt.mirouters.mixin;

import com.matyrobbrt.mirouters.MIRouters;
import com.matyrobbrt.mirouters.client.EUWidget;
import me.desht.modularrouters.client.gui.ModularRouterScreen;
import me.desht.modularrouters.container.RouterMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModularRouterScreen.class)
public abstract class RouterScreenMixin extends AbstractContainerScreen<RouterMenu> {
    @Unique
    private EUWidget euWidget;
    public RouterScreenMixin(RouterMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void addEU(CallbackInfo ci) {
        var attach = getMenu().getRouter().getData(MIRouters.STORAGE);
        this.addRenderableWidget(this.euWidget = new EUWidget(attach, this.leftPos + 44, this.topPos + 39));
        euWidget.visible = isEUVisible();
    }

    @Inject(at = @At("TAIL"), method = "containerTick")
    private void euReset(CallbackInfo ci) {
        euWidget.visible = isEUVisible();
    }

    @Unique
    private boolean isEUVisible() {
        return MIRouters.UPGRADES.stream().anyMatch(pred -> getMenu().getRouter().getUpgradeCount(pred.get()) > 0);
    }
}
