package dev.mayaqq.embellishment.registries.blockEntities;

import dev.mayaqq.embellishment.registries.EmbeBlockEntities;
import dev.mayaqq.embellishment.registries.entities.HollowMerchantEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MerchantsEffigyBlockEntity extends EffigyBlockEntity {

    private HollowMerchantEntity villager = null;

    public MerchantsEffigyBlockEntity(BlockPos pos, BlockState blockState) {
        super(EmbeBlockEntities.MERCHANTS_EFFIGY.get(), pos, blockState);
    }

    public MerchantsEffigyBlockEntity(BlockPos pos, BlockState blockState, HollowMerchantEntity villager) {
        super(EmbeBlockEntities.MERCHANTS_EFFIGY.get(), pos, blockState);
        this.villager = villager;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MerchantsEffigyBlockEntity blockEntity) {
        if (blockEntity.villager != null) {
            blockEntity.villager.tick();
        }
    }

    public void setHollowMerchant(HollowMerchantEntity villager) {
        this.villager = villager;
    }

    public HollowMerchantEntity getHollowMerchant() {
        return villager;
    }

    public boolean villagerPresent() {
        return villager != null;
    }

    public HollowMerchantEntity getVillager() {
        return villager;
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);

        if (villagerPresent()) {
            CompoundTag villagerCompound = new CompoundTag();
            getVillager().saveWithoutId(villagerCompound);
            compound.put("Merchant", villagerCompound);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
        if (compound.contains("Merchant")) {
            CompoundTag merchant = compound.getCompound("Merchant");
            HollowMerchantEntity villager = new HollowMerchantEntity(level, VillagerType.PLAINS);
            villager.addAdditionalSaveData(merchant);

            setHollowMerchant(villager);
        }
        super.loadAdditional(compound, provider);
    }
}
