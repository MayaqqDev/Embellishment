package dev.mayaqq.embellishment.registries.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public class EmbeMerchantMenu extends MerchantMenu {
    public EmbeMerchantMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory);
    }

    public EmbeMerchantMenu(int containerId, Inventory playerInventory, Merchant trader) {
        super(containerId, playerInventory, trader);
    }
}
