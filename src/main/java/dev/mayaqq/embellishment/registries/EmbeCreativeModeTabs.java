package dev.mayaqq.embellishment.registries;

import dev.mayaqq.embellishment.Embellishment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EmbeCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Embellishment.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EMBELLISHMENT = CREATIVE_MODE_TABS.register("embellishment", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.embellishment"))
            .icon(() -> EmbeItems.MERCHANTS_EFFIGY.get().getDefaultInstance())
            .displayItems((parms, output) -> {
                output.accept(EmbeItems.MERCHANTS_EFFIGY);
            })
            .build()
    );
}
