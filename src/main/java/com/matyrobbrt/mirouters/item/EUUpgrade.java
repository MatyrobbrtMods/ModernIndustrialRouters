package com.matyrobbrt.mirouters.item;

import aztech.modern_industrialization.api.energy.CableTier;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.item.upgrade.UpgradeItem;

public class EUUpgrade extends UpgradeItem {
    public final CableTier tier;
    public static final TintColor TINT_COLOR = new TintColor(79, 20, 60);

    public EUUpgrade(CableTier tier) {
        this.tier = tier;
    }

    public long getMax(int amountOfUpgrades) {
        return 1000L * amountOfUpgrades;
    }

    public long getTransferCap(int amountOfUpgrades) {
        return 10L * amountOfUpgrades;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }
}
