package com.matyrobbrt.mirouters.mixin;

import com.matyrobbrt.mirouters.item.EUUpgrade;
import me.desht.modularrouters.item.upgrade.EnergyUpgrade;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnergyUpgrade.class)
public class EnergyUpgradeMixin extends UpgradeItem {
    @Override
    public boolean isCompatibleWith(UpgradeItem other) {
        return !(other instanceof EUUpgrade);
    }
}
