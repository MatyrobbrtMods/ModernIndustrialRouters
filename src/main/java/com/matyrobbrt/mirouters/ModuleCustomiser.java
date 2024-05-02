package com.matyrobbrt.mirouters;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import dev.technici4n.grandpower.api.EnergyStorageUtil;
import me.desht.modularrouters.api.event.ExecuteModuleEvent;
import me.desht.modularrouters.api.event.RegisterRouterContainerData;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.logic.ModuleTarget;
import me.desht.modularrouters.util.BeamData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.DataSlot;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

public class ModuleCustomiser {
    @SubscribeEvent
    public void onModule(ExecuteModuleEvent event) {
        if (event.getModule().getModule() == ModItems.ENERGY_OUTPUT_MODULE.value()) {
            var routerStorage = event.getRouter().getData(MIRouters.STORAGE);
            if (event.getModule().getTarget() != null && routerStorage.getCapacity() > 0) {
                MIEnergyStorage otherStorage = event.getModule().getTarget().getCapability(EnergyApi.SIDED, event.getModule().getTarget().face);
                if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                    event.setExecuted(EnergyStorageUtil.move(routerStorage, otherStorage, routerStorage.getTransferRate()) > 0);
                }
                event.setCanceled(true);
            }
        } else if (event.getModule().getModule() == ModItems.ENERGY_DISTRIBUTOR_MODULE.value()) {
            var routerStorage = event.getRouter().getData(MIRouters.STORAGE);
            if (routerStorage.getCapacity() > 0) {
                List<ModuleTarget> inRange = event.getModule().getTargets().stream()
                        .filter((targetx) -> targetx.isSameWorld(event.getRouter().getLevel()) && event.getRouter().getBlockPos().distSqr(targetx.gPos.pos()) <= (double) event.getModule().getRangeSquared())
                        .toList();

                if (!inRange.isEmpty()) {
                    long toSend = routerStorage.getAmount() / inRange.size();
                    boolean doBeam = event.getRouter().getUpgradeCount(ModItems.MUFFLER_UPGRADE.get()) < 2;
                    long total = 0;
                    for (ModuleTarget target : inRange) {
                        MIEnergyStorage otherStorage = target.getCapability(EnergyApi.SIDED, target.face);
                        if (otherStorage != null && otherStorage.canConnect(routerStorage.getTier())) {
                            long sent = EnergyStorageUtil.move(routerStorage, otherStorage, toSend);
                            if (sent > 0 && doBeam) {
                                event.getRouter().addItemBeam(new BeamData(event.getRouter().getTickRate(), target.gPos.pos(), 0xE04040));
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
    public void registerMenu(final RegisterRouterContainerData event) {
        final var router = event.getRouter();
        event.register(new ResourceLocation(MIRouters.MOD_ID, "eu"), new DataSlot() {
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
}