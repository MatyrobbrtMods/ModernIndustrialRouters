package com.matyrobbrt.mirouters.item;

import aztech.modern_industrialization.api.energy.CableTier;
import com.matyrobbrt.mirouters.MIRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.ClientUtil;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.config.ConfigHolder;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.upgrade.UpgradeItem;
import me.desht.modularrouters.util.MiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EUUpgrade extends UpgradeItem {
    public final CableTier tier;
    private final long capacity, transfer;
    public static final TintColor TINT_COLOR = new TintColor(79, 20, 60);

    public EUUpgrade(CableTier tier, long capacity, long transfer) {
        this.tier = tier;
        this.capacity = capacity;
        this.transfer = transfer;
    }

    public long getMax(int amountOfUpgrades) {
        return capacity * amountOfUpgrades;
    }

    public long getTransferCap(int amountOfUpgrades) {
        return transfer * amountOfUpgrades;
    }

    @Override
    public TintColor getItemTint() {
        return TINT_COLOR;
    }

    @Override
    public boolean isCompatibleWith(UpgradeItem other) {
        return !(other instanceof EUUpgrade) && other != ModItems.ENERGY_UPGRADE.value();
    }

    @Override
    protected Object[] getExtraUsageParams() {
        return new Object[] { tier.shortEnglishName().withStyle(ChatFormatting.GOLD), capacity, transfer };
    }

    @Override
    public void addUsageInformation(ItemStack itemstack, List<Component> list) {
        super.addUsageInformation(itemstack, list);
        ClientUtil.getOpenItemRouter().ifPresent((router) -> {
            var storage = router.getData(MIRouters.STORAGE);
            list.add(Component.translatable("modernindustrialrouters.in_gui.eu_upgrade", storage.getCapacity(), storage.getTransferRate()));
        });
    }

    @Override
    public int getStackLimit(int slot) {
        return 64;
    }

    @Override
    public void processClientSync(ModularRouterBlockEntity router, @Nullable CompoundTag tag) {
        router.getData(MIRouters.STORAGE).update();
        router.getLevel().invalidateCapabilities(router.getBlockPos());
    }
}
