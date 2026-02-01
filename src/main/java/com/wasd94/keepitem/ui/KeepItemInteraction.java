package com.wasd94.keepitem.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.wasd94.keepitem.KeepItem;
import com.wasd94.keepitem.components.KeepItemMapComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class KeepItemInteraction extends ChoiceInteraction {

    final ItemContext itemContext;

    public KeepItemInteraction(ItemContext itemContext) {
        this.itemContext = itemContext;
    }

    @Override
    public void run(@NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef) {
        Inventory inventory = store.getComponent(ref, Player.getComponentType()).getInventory();
        KeepItemMapComponent keepItemMapComponent = store.getComponent(ref, KeepItemMapComponent.getComponentType());
        if (inventory == null) return;
        if (keepItemMapComponent == null) {
            keepItemMapComponent = new KeepItemMapComponent();
        }

        boolean isKept = !(keepItemMapComponent.getKeepItemMapElement(KeepItemMapComponent.getKey(itemContext)));

        keepItemMapComponent.setKeepItemMapElement(KeepItemMapComponent.getKey(itemContext), isKept);
        store.putComponent(ref, KeepItemMapComponent.getComponentType(), keepItemMapComponent);
        KeepItem.LOGGER.atInfo().log("KeepItemInteraction run for item: " + itemContext.getItemStack().getItemId() + " isKept set to " + isKept + " " + KeepItemMapComponent.getKey(itemContext));

        // Refresh the UI
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null && player.getPageManager().getCustomPage() != null) {
            player.getPageManager().openCustomPage(ref, store, new KeepItemPage(playerRef, inventory, keepItemMapComponent));
        }
    }
}
