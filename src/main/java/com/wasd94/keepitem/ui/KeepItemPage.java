package com.wasd94.keepitem.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceBasePage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.wasd94.keepitem.components.KeepItemMapComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;

public class KeepItemPage extends ChoiceBasePage {
    final Inventory inventory;

    public KeepItemPage(@NonNullDecl PlayerRef playerRef, Inventory inventory, @NonNullDecl KeepItemMapComponent keepItemMapComponent) {

        this.inventory = inventory;
        super(playerRef, getItemElements(inventory, keepItemMapComponent), "KeepItemPage.ui");
    }

    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        if (this.getElements().length > 0) {
            super.build(ref, commandBuilder, eventBuilder, store);
        } else {
            commandBuilder.append(this.getPageLayout());
            commandBuilder.clear("#ElementList");
            commandBuilder.appendInline("#ElementList", "Label { Text: \"No items in inventory\"; Style: (Alignment: Center); }");
        }
    }

    @Nonnull
    protected static ChoiceElement[] getItemElements(@Nonnull Inventory inventory, @Nonnull KeepItemMapComponent keepItemMapComponent) {
        ObjectArrayList<ChoiceElement> elements = new ObjectArrayList();
        CombinedItemContainer itemContainer = inventory.getCombinedStorageFirst();

        for (short slot = 0; slot < itemContainer.getCapacity(); ++slot) {
            ItemStack itemStack = itemContainer.getItemStack(slot);
            if (itemStack != null) {

                ItemContext itemContext = new ItemContext(itemContainer, slot, itemStack);
                elements.add(new KeepItemElement(itemContext, keepItemMapComponent.getKeepItemMapElement(KeepItemMapComponent.getKey(itemContext))));
            }
        }
        return elements.toArray(ChoiceElement[]::new);
    }
}

