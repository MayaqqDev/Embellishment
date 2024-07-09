package dev.mayaqq.embellishment.registries.entities;

import dev.mayaqq.embellishment.registries.EmbeEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;

public class HollowMerchantEntity extends Villager {

    public HollowMerchantEntity(EntityType<HollowMerchantEntity> hollowMerchantEntityEntityType, Level level) {
        super(hollowMerchantEntityEntityType, level);
    }

    public HollowMerchantEntity(Level level, VillagerType villagerType) {
        super(EmbeEntities.HOLLOW_MERCHANT.get(), level, villagerType);
    }
}
