package com.matyrobbrt.mirouters.mixin;

import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.container.RouterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RouterMenu.class)
public abstract class RouterContainerMixin extends AbstractContainerMenu {
    @Shadow @Final private ModularRouterBlockEntity router;

    protected RouterContainerMixin(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void onInit(int windowId, Inventory invPlayer, BlockPos routerPos, CallbackInfo ci) {
        final var router = this.router;
        this.addDataSlot(new DataSlot() {
            // TODO - fix?
            @Override
            public int get() {
                return (int) router.getData(MIRouters.STORAGE).getAmount();
            }

            @Override
            public void set(int pValue) {
                router.getData(MIRouters.STORAGE).stored = pValue;
            }
        });
    }
}
