package com.matyrobbrt.mirouters;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import com.matyrobbrt.mirouters.client.ClientMIRouters;
import com.matyrobbrt.mirouters.item.EUUpgrade;
import me.desht.modularrouters.block.tile.ModularRouterBlockEntity;
import me.desht.modularrouters.core.ModBlockEntities;
import me.desht.modularrouters.core.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

@Mod(MIRouters.MOD_ID)
public class MIRouters {
    public static final String MOD_ID = "modernindustrialrouters";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    // Max speed should be able to power one machine
    public static final DeferredItem<EUUpgrade> LV = ITEMS.register("lv_upgrade", () -> new EUUpgrade(CableTier.LV, 256L * 5, 32L * 2));

    // Basic upgrade is 2 EU per upgrade
    public static final DeferredItem<EUUpgrade> MV = ITEMS.register("mv_upgrade", () -> new EUUpgrade(CableTier.MV, 1024L * 5, 32L * 2 * (2 * 64)));

    // Advanced upgrade is 8 EU per upgrade
    public static final DeferredItem<EUUpgrade> HV = ITEMS.register("hv_upgrade", () -> new EUUpgrade(CableTier.HV, 8192L * 5, 32L * 2 * (8 * 64)));

    // Turbo upgrade is 32 EU per upgrade
    public static final DeferredItem<EUUpgrade> EV = ITEMS.register("ev_upgrade", () -> new EUUpgrade(CableTier.EV, 65536L * 5, 32L * 2 * (32 * 64)));

    // Highly advanced upgrade is 128 EU per upgrade
    public static final DeferredItem<EUUpgrade> SUPERCONDUCTOR = ITEMS.register("superconductor_upgrade", () -> new EUUpgrade(CableTier.SUPERCONDUCTOR, (65536L * 16L) * 5, 32L * 2 * (128 * 64) * 100L));

    public static final List<DeferredItem<EUUpgrade>> UPGRADES = List.of(LV, MV, HV, EV, SUPERCONDUCTOR);
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

    public MIRouters(IEventBus bus, Dist dist) {
        ITEMS.register(bus);
        ATTACHMENTS.register(bus);

        if (dist.isClient()) {
            ClientMIRouters.setup(bus);
        }

        bus.addListener((final RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(EnergyApi.SIDED, ModBlockEntities.MODULAR_ROUTER.get(), (be, abc) -> {
                if (be.getData(STORAGE).getCapacity() > 0) {
                    return be.getData(STORAGE);
                }
                return new InsertOnlyTrStorage(be.getEnergyStorage());
            });
        });

        final var key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation("modularrouters:default"));
        bus.addListener((final BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == key) {
                UPGRADES.forEach(event::accept);
            }
        });

        bus.addListener((final GatherDataEvent event) -> {
            event.getGenerator().addProvider(event.includeClient(), new LanguageProvider(event.getGenerator().getPackOutput(), MOD_ID, "en_us") {
                @Override
                protected void addTranslations() {
                    add(LV.value(), "LV Energy Upgrade");
                    add(HV.value(), "HV Energy Upgrade");
                    add(MV.value(), "MV Energy Upgrade");
                    add(EV.value(), "EV Energy Upgrade");
                    add(SUPERCONDUCTOR.value(), "Superconductor Energy Upgrade");

                    UPGRADES.forEach(up -> add(
                            make(up), "Makes energy modules in a router behave as if they are transferring Modern Industrialisation EU of tier %d\nIncreases a router's energy buffer capacity by %d EU and transfer rate by %d EU/router tick"
                    ));

                    add("modernindustrialrouters.in_gui.eu_upgrade", "• This router's energy capacity: %d EU\n• This router's transfer rate: %d EU/router tick");
                }

                private String make(ItemLike itemLike) {
                    return "modularrouters.itemText.usage.item." + BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
                }
            });

            event.getGenerator().addProvider(event.includeServer(), new RecipeProvider(event.getGenerator().getPackOutput()) {
                @Override
                protected void buildRecipes(RecipeOutput output) {
                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, LV)
                            .pattern("CCC")
                            .pattern(" U ")
                            .pattern("CCC")
                            .define('C', miItem("tin_cable"))
                            .define('U', ModItems.ENERGY_UPGRADE)
                            .unlockedBy("has_item", has(ModItems.ENERGY_UPGRADE))
                            .save(output);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, MV)
                            .pattern("CCC")
                            .pattern(" U ")
                            .pattern("CCC")
                            .define('C', miItem("electrum_cable"))
                            .define('U', ModItems.ENERGY_UPGRADE)
                            .unlockedBy("has_item", has(ModItems.ENERGY_UPGRADE))
                            .save(output);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, HV)
                            .pattern("CCC")
                            .pattern(" U ")
                            .pattern("CCC")
                            .define('C', miItem("aluminum_cable"))
                            .define('U', ModItems.ENERGY_UPGRADE)
                            .unlockedBy("has_item", has(ModItems.ENERGY_UPGRADE))
                            .save(output);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, EV)
                            .pattern("CCC")
                            .pattern(" U ")
                            .pattern("CCC")
                            .define('C', miItem("annealed_copper_cable"))
                            .define('U', ModItems.ENERGY_UPGRADE)
                            .unlockedBy("has_item", has(ModItems.ENERGY_UPGRADE))
                            .save(output);

                    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, SUPERCONDUCTOR)
                            .pattern("CCC")
                            .pattern(" U ")
                            .pattern("CCC")
                            .define('C', miItem("superconductor_cable"))
                            .define('U', ModItems.ENERGY_UPGRADE)
                            .unlockedBy("has_item", has(ModItems.ENERGY_UPGRADE))
                            .save(output);
                }

                private Item miItem(String name) {
                    return BuiltInRegistries.ITEM.get(new ResourceLocation("modern_industrialization", name));
                }
            });
        });

        NeoForge.EVENT_BUS.register(new ModuleCustomiser());
    }

    private record InsertOnlyTrStorage(IEnergyStorage trStorage) implements MIEnergyStorage.NoExtract {
        @Override
        public boolean canConnect(CableTier cableTier) {
            return true;
        }

        @Override
        public long receive(long maxAmount, boolean simulate) {
            return trStorage.receiveEnergy((int) Math.max(maxAmount, Integer.MAX_VALUE), simulate);
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
                        stored = 0;
                        max = 0;
                        transferMax = 0;
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
