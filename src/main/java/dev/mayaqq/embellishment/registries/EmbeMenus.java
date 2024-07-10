package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import dev.mayaqq.embellishment.registries.menu.EmbeMerchantMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, Embellishment.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EmbeMerchantMenu>> MERCHANT_MENU = MENUS.register("merchant_menu", () -> new MenuType<>(EmbeMerchantMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
