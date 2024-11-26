package com.matyrobbrt.mirouters;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import me.desht.modularrouters.api.event.AddModuleTargetEvent;
import me.desht.modularrouters.api.event.ExecuteModuleEvent;
import me.desht.modularrouters.api.event.RegisterRouterContainerData;
import me.desht.modularrouters.api.event.RouterCompiledEvent;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.DataSlot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.BlockCapability;

import java.util.List;

public class ModuleCustomiser {
    @SubscribeEvent
    static void onModule(ExecuteModuleEvent event) {
        if (event.getModule().getModule() == ModItems.ENERGY_OUTPUT_MODULE.value()) {
            var routerStorage = event.getRouter().getData(MIRouters.STORAGE);
            if (event.getModule().getTarget() != null && routerStorage.getCapacity() > 0 && routerStorage.getTier() != null) {
                MIEnergyStorage otherStorage = getCapability(event.getModule().getTarget(), EnergyApi.SIDED);
                if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                    event.setExecuted(EnergyStorageUtil.move(routerStorage, otherStorage, routerStorage.getTransferRate()) > 0);
                }
                event.setCanceled(true);
            }
        } else if (event.getModule().getModule() == ModItems.ENERGY_DISTRIBUTOR_MODULE.value()) {
            var routerStorage = event.getRouter().getData(MIRouters.STORAGE);
            if (routerStorage.getCapacity() > 0 && routerStorage.getTier() != null) {
                List<ModuleTarget> inRange = event.getModule().getTargets().stream()
                        .filter((targetx) -> targetx.isSameWorld(event.getRouter().getLevel()) && event.getRouter().getBlockPos().distSqr(targetx.gPos.pos()) <= (double) event.getModule().getRangeSquared())
                        .toList();

                if (!inRange.isEmpty()) {
                    long toSend = routerStorage.getAmount() / inRange.size();
                    boolean doBeam = event.getRouter().getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
                    long total = 0;
                    for (ModuleTarget target : inRange) {
                        MIEnergyStorage otherStorage = getCapability(target, EnergyApi.SIDED);
                        if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                            long sent = EnergyStorageUtil.move(routerStorage, otherStorage, toSend);
                            if (sent > 0 && doBeam) {
                                event.getRouter().addItemBeam(new BeamData.Builder(event.getRouter(), target.gPos.pos(), 0xE04040).build());
                            }
                            total += sent;
                        }
                    }
                    event.setExecuted(total > 0);
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    static void checkValidTarget(final AddModuleTargetEvent event) {
        // Allow setting EU-powered machines as distributor targets
        if (event.getModuleType() == ModItems.ENERGY_DISTRIBUTOR_MODULE.value() && !event.isValid()) {
            if (event.getContext().getLevel().getCapability(EnergyApi.SIDED, event.getContext().getClickedPos(), event.getContext().getClickedFace()) != null) {
                event.setValid(true);
            }
        }
    }

    @SubscribeEvent
    static void onCompileUpgrades(final RouterCompiledEvent.Upgrades event) {
        // Update the EU storage
        event.getRouter().getData(MIRouters.STORAGE).update();
    }

    @SubscribeEvent
    static void registerMenu(final RegisterRouterContainerData event) {
        final var router = event.getRouter();
        event.register(ResourceLocation.fromNamespaceAndPath(MIRouters.MOD_ID, "eu"), new DataSlot() {
            // TODO - fix to actually be longs
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

    private static <T, C> T getCapability(ModuleTarget target, BlockCapability<T, Direction> cap) {
        return target.getCapability(cap).orElse(null);
    }
}
