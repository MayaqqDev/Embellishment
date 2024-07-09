package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import dev.mayaqq.embellishment.registries.entities.HollowMerchantEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Embellishment.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HollowMerchantEntity>> HOLLOW_MERCHANT = ENTITIES.register("hollow_merchant", () -> EntityType.Builder.<HollowMerchantEntity>of(HollowMerchantEntity::new, MobCategory.MISC).sized(0.6F, 1.95F).build("hollow_merchant"));
}
