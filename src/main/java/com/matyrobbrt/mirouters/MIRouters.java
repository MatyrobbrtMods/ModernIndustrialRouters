package com.matyrobbrt.mirouters;

import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import com.matyrobbrt.mirouters.item.EUOutputModule;
import com.matyrobbrt.mirouters.item.EUUpgrade;
import dev.technici4n.grandpower.api.ILongEnergyStorage;
import me.desht.modularrouters.ModularRouters;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.client.util.TintColor;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModItems;
import me.desht.modularrouters.item.MRBaseItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Mod(MIRouters.MODID)
public class MIRouters
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "modernindustrialrouters";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    public static final DeferredItem<EUUpgrade> LV = ITEMS.register("lv_upgrade", () -> new EUUpgrade(CableTier.LV));
    public static final DeferredItem<EUOutputModule> EU_OUTPUT = ITEMS.register("eu_output_module", EUOutputModule::new);
    public static final List<DeferredItem<EUUpgrade>> UPGRADES = List.of(LV);
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EnergyStorage>> STORAGE = ATTACHMENTS.register("eu_energy", () -> AttachmentType.builder((holder) -> new EnergyStorage((ModularRouterBlockEntity) holder))
            .serialize(new IAttachmentSerializer<>() {
                @Override
                public Tag write(EnergyStorage attachment) {
                    final CompoundTag tag = new CompoundTag();
                    tag.putLong("energy", attachment.getAmount());
                    tag.putString("tier", attachment.getTier().name);
                    return tag;
                }

                @Override
                public EnergyStorage read(IAttachmentHolder holder, Tag tag) {
                    final var storage = new EnergyStorage((ModularRouterBlockEntity) holder);
                    final var ctag = ((CompoundTag) tag);
                    storage.stored = ctag.getLong("energy");
                    if (ctag.contains("tier")) {
                        storage.tier = CableTier.getTier(ctag.getString("tier"));
                    }
                    return storage;
                }
            }).build());

    public MIRouters(IEventBus bus) {
        ITEMS.register(bus);
        ATTACHMENTS.register(bus);

        bus.addListener((final RegisterColorHandlersEvent.Item event) -> {
            event.register((stack, idx) -> switch (idx) {
                case 0, 2 -> TintColor.WHITE.getRGB();
                case 1 -> ((ModItems.ITintable) stack.getItem()).getItemTint().getRGB();
                default -> TintColor.BLACK.getRGB();  // shouldn't get here
            }, LV.asItem(), EU_OUTPUT.asItem());
        });

        bus.addListener((final RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(EnergyApi.SIDED, ModBlockEntities.MODULAR_ROUTER.get(), (be, abc) -> {
                if (be.getData(STORAGE).getCapacity() > 0) {
                    return be.getData(STORAGE);
                }
                return new InsertOnlyTrStorage(be.getEnergyStorage());
            });
        });
    }

    private record InsertOnlyTrStorage(IEnergyStorage trStorage) implements MIEnergyStorage.NoExtract {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long receive(long maxAmount, boolean simulate) {
            return trStorage.receiveEnergy((int)Math.max(maxAmount, Integer.MAX_VALUE), simulate);
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public long getAmount() {
            return trStorage.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return trStorage.getMaxEnergyStored();
        }
    }

    public static class EnergyStorage implements MIEnergyStorage {
        private final ModularRouterBlockEntity be;
        public long stored;
        private long max, transferMax;
        // Initially anything should be able to connect
        private CableTier tier = null;
        public EnergyStorage(ModularRouterBlockEntity be) {
            this.be = be;
        }

        public CableTier getTier() {
            return tier;
        }

        public long getTransferRate() {
            return transferMax;
        }

        public void update() {
            UPGRADES.stream()
                    .filter(eu -> be.getUpgradeCount(eu.value()) > 0)
                    .findFirst()
                    .map(DeferredHolder::value)
                    .ifPresentOrElse(up -> {
                        tier = up.tier;
                        max = up.getMax(be.getUpgradeCount(up));
                        transferMax = up.getTransferCap(be.getUpgradeCount(up));
                        if (stored > max) {
                            stored = max;
                            be.setChanged();
                        }
                    }, () -> {
                        stored = max = transferMax = 0;
                        be.setChanged();
                    });
        }

        @Override
        public boolean canConnect(CableTier cableTier) {
            return tier == null || tier == cableTier;
        }

        @Override
        public long receive(long maxReceive, boolean simulate) {
            long amount = Math.min(maxReceive, Math.min(max - stored, transferMax));
            if (amount < 0) return 0;
            if (!simulate) {
                stored += amount;
                be.setChanged();
            }
            return amount;
        }

        @Override
        public long extract(long maxExtract, boolean simulate) {
            long amount = Math.min(maxExtract, Math.min(stored, transferMax));
            if (amount < 0) return 0;
            if (!simulate) {
                stored -= amount;
                be.setChanged();
            }
            return amount;
        }

        @Override
        public long getAmount() {
            return stored;
        }

        @Override
        public long getCapacity() {
            return max;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
