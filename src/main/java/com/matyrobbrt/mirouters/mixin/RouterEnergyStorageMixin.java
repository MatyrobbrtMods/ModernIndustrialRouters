package com.matyrobbrt.mirouters.mixin;

import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "me/desht/modularrouters/block/tile/ModularRouterBlockEntity$RouterEnergyBuffer")
public class RouterEnergyStorageMixin {
    @Shadow @Final
    ModularRouterBlockEntity this$0;

    @Inject(method = "updateForEnergyUpgrades", at = @At("TAIL"))
    private void invalidateOnUpdate(int nEnergyUpgrades, CallbackInfo ci) {
        this.this$0.getLevel().invalidateCapabilities(this$0.getBlockPos());
    }
}
