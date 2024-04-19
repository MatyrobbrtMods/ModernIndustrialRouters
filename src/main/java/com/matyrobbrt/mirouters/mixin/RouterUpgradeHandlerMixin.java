package com.matyrobbrt.mirouters.mixin;

import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "me.desht.modularrouters.block.tile.ModularRouterBlockEntity$UpgradeHandler")
public abstract class RouterUpgradeHandlerMixin implements IItemHandler {
    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void disallowConcurrentEnergy(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() == ModItems.ENERGY_UPGRADE.asItem() || MIRouters.UPGRADES.stream().anyMatch(it -> it.value() == stack.getItem())) {
            for (int i = 0; i < getSlots(); i++) {
                if (slot != i) {
                    var stackInSlot = getStackInSlot(i);
                    if (stackInSlot.getItem() == ModItems.ENERGY_UPGRADE.asItem() || MIRouters.UPGRADES.stream().anyMatch(it -> it.value() == stackInSlot.getItem())) {
                        cir.setReturnValue(false);
                        return;
                    }
                }
            }
        }
    }
}
