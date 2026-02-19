package com.wasd94.keepitem.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.*;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.wasd94.keepitem.KeepItem;
import com.wasd94.keepitem.components.KeepItemMapComponent;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;

public class InventoryChangeKeepItemHandler {
    public static ArrayList<String> movedItemsList = new ArrayList<>();

    public static void revertQuickStackForKeepItems(@NonNullDecl LivingEntityInventoryChangeEvent event) {
        boolean isHotBar = event.getItemContainer().getCapacity() == 9;
//        Transaction basetransaction = event.getTransaction();
//        KeepItem.LOGGER.atInfo().log("Event: " + basetransaction.getClass().getSimpleName() + " triggered for inventory " + (isHotBar ? "hotbar" : "storage") + " change.");
        LivingEntity entity = event.getEntity();
        if (entity == null) return;

        Ref<EntityStore> ref = entity.getReference();
        if (ref == null) return;

        if (event.getTransaction() instanceof ListTransaction<?> transaction) { // manage quick stacking multiple items from player inventory to storage
            KeepItemMapComponent settings = ref.getStore().getComponent(ref, KeepItemMapComponent.getComponentType());
            if (settings == null) return;
            if (transaction.getList().isEmpty()) return;
            MoveTransaction<?> mt = transaction.getList().stream()
                    .filter(MoveTransaction.class::isInstance)
                    .map(MoveTransaction.class::cast)
                    .filter(x -> x.getMoveType() == MoveType.MOVE_FROM_SELF)
                    .findFirst()
                    .orElse(null);
            if (mt == null) return;

            handleRevertingQuickstack(event, mt, isHotBar, settings);

        } else if (event.getTransaction() instanceof MoveTransaction<?> moveTransaction) { // manage moving a single item inside player inventory or hotbar

            KeepItemMapComponent settings = ref.getStore().getComponent(ref, KeepItemMapComponent.getComponentType());
            if (settings == null) return;
            String objId = moveTransaction.getRemoveTransaction().getSlotBefore().getItemId();
            KeepItem.LOGGER.atInfo().log("Processing MoveTransaction for inventory " + (isHotBar ? "hotbar" : "storage") + " change of object " + objId + " (" + moveTransaction.getMoveType() + ").");
            ItemContainer backpack = event.getEntity().getInventory().getBackpack();
            ItemContainer storage = event.getEntity().getInventory().getStorage();
            ItemContainer hotbar = event.getEntity().getInventory().getHotbar();

            if (moveTransaction.getMoveType() == MoveType.MOVE_FROM_SELF) {
                handleMoveFromSelf(event, moveTransaction, backpack, storage, hotbar, settings, ref);
            } else {
                handleMoveToSelf(event, moveTransaction, storage, hotbar, settings, ref, backpack);
            }

        } else {
            if (event.getTransaction() instanceof ItemStackTransaction itemStackTransaction) {
                KeepItem.LOGGER.atInfo().log("Ignoring transaction of type " + event.getTransaction().getClass().getSimpleName() + " of " + itemStackTransaction.getSlotTransactions().getLast().getSlot() + " for inventory " + (isHotBar ? "hotbar" : "storage") + " change.");
            } else if (event.getTransaction() instanceof SlotTransaction slotTransaction) {
                KeepItem.LOGGER.atInfo().log("Ignoring transaction of type " + event.getTransaction().getClass().getSimpleName() + " of slot " + slotTransaction.getSlot() + " of object " + slotTransaction.getSlotAfter().getItemId() + " for inventory " + (isHotBar ? "hotbar" : "storage") + " change.");
            } else
                KeepItem.LOGGER.atInfo().log("Ignoring transaction of type " + event.getTransaction().getClass().getSimpleName() + " for inventory " + (isHotBar ? "hotbar" : "storage") + " change.");
        }
    }

    private static void handleRevertingQuickstack(@NonNullDecl LivingEntityInventoryChangeEvent event, @NonNullDecl MoveTransaction<?> moveTransaction, boolean isHotBar, @NonNullDecl KeepItemMapComponent settings) {
        SlotTransaction removeTransaction = moveTransaction.getRemoveTransaction(); // The transaction that removed the item from the original slot in player inventory
        ItemStack movedItems = removeTransaction.getSlotBefore();
        ItemContext itemContext = new ItemContext(event.getItemContainer(), removeTransaction.getSlot(), movedItems);
        if (movedItemsList.contains(KeepItemMapComponent.getKey(itemContext))) {
            KeepItem.LOGGER.atInfo().log("Already processed quick stacking of " + movedItems.getItemId() + " in slot " + removeTransaction.getSlot() + (isHotBar ? " (hotbar)" : " (storage)") + ", skipping.");
            if (movedItemsList.indexOf(KeepItemMapComponent.getKey(itemContext)) != 0) {
                removeItemsFromContainer((SimpleItemContainer) moveTransaction.getOtherContainer(), movedItems.withQuantity(movedItems.getQuantity()));
            }
            movedItemsList.clear();
            return;
        }
        movedItemsList.add(KeepItemMapComponent.getKey(itemContext));
        if (settings.getKeepItemMapElement(KeepItemMapComponent.getKey(itemContext))) {
            // put back item in original player slot
            event.getItemContainer().setItemStackForSlot(removeTransaction.getSlot(), movedItems);

            // revert item in the other container
            SimpleItemContainer container = (SimpleItemContainer) moveTransaction.getOtherContainer();
            removeItemsFromContainer(container, movedItems.withQuantity(movedItems.getQuantity()));

            KeepItem.LOGGER.atInfo().log("Stopped quick stacking of " + movedItems.getItemId() + " in slot " + removeTransaction.getSlot() + (isHotBar ? " (hotbar)" : " (storage)") + " due to keep item setting.");
        } else {
            KeepItem.LOGGER.atInfo().log("Allowed quick stacking of " + movedItems.getItemId() + " in slot " + removeTransaction.getSlot() + (isHotBar ? " (hotbar)" : " (storage)") + " due to no keep item setting.");
        }
    }

    private static void removeItemsFromContainer(SimpleItemContainer container, ItemStack itemStack) {
        int removedQuantity = 0;
        for (short slot = (short) (container.getCapacity() - 1); slot >= 0; slot--) {
            ItemStack currentStack = container.getItemStack(slot);
            if (currentStack != null && currentStack.isEquivalentType(itemStack)) {
                int quantityToRemove = Math.min(currentStack.getQuantity(), itemStack.getQuantity() - removedQuantity);
                if (quantityToRemove > 0) {
                    container.removeItemStackFromSlot(slot, quantityToRemove);
                    removedQuantity += quantityToRemove;
                    if (removedQuantity >= itemStack.getQuantity()) {
                        break; // Stop if we've removed the desired quantity
                    }
                }
            }
        }
    }

    private static void handleMoveFromSelf(@NonNullDecl LivingEntityInventoryChangeEvent event, @NonNullDecl MoveTransaction<?> moveTransaction, ItemContainer backpack, ItemContainer storage, ItemContainer hotbar, KeepItemMapComponent settings, Ref<EntityStore> ref) {
        ItemContainer toContainer = moveTransaction.getOtherContainer();
        ItemContainer fromContainer = event.getItemContainer();

        if (toContainer == backpack || toContainer == storage || toContainer == hotbar)
            return;
        SlotTransaction removeTransaction = moveTransaction.getRemoveTransaction();
        if (removeTransaction.getSlotAfter() != null && !removeTransaction.getSlotAfter().isEmpty() && removeTransaction.getSlotAfter().isEquivalentType(removeTransaction.getSlotBefore())) {
            // stack was only changed in quantity, not removed
            return;
        }
        ItemStack oldItems = removeTransaction.getSlotBefore();
        ItemContext oldIC = new ItemContext(moveTransaction.getOtherContainer(), removeTransaction.getSlot(), oldItems);
        if (!settings.containsKeepItemMapElement(KeepItemMapComponent.getKey(oldIC)))
            return;
        // just remove the key from settings
        settings.removeKeepItemMapElement(KeepItemMapComponent.getKey(oldIC));
        KeepItem.LOGGER.atInfo().log("Removed keep item setting for item " + oldItems.getItemId() + " from slot " + removeTransaction.getSlot() + (fromContainer.getCapacity() == 9 ? " (hotbar)" : " (storage)"));
        ref.getStore().putComponent(ref, KeepItemMapComponent.getComponentType(), settings);
    }

    private static void handleMoveToSelf(@NonNullDecl LivingEntityInventoryChangeEvent event, @NonNullDecl MoveTransaction<?> moveTransaction, ItemContainer storage, ItemContainer hotbar, KeepItemMapComponent settings, Ref<EntityStore> ref, ItemContainer backpack) {
        ItemContainer fromContainer = moveTransaction.getOtherContainer();
        ItemContainer toContainer = event.getItemContainer();
        SlotTransaction removeTransaction = moveTransaction.getRemoveTransaction();
        if (removeTransaction.getSlotAfter() != null && !removeTransaction.getSlotAfter().isEmpty() && removeTransaction.getSlotAfter().isEquivalentType(removeTransaction.getSlotBefore()))
            return;
        if ((fromContainer == storage || fromContainer == hotbar) &&
                (toContainer == storage || toContainer == hotbar)) { // continue processing MOVE_TO_SELF type transactions inside storage or hotbar (not to/from backpack)

            boolean isFromHotBar = fromContainer.getCapacity() == 9; // evaluate isHotBar for the source container
            boolean isToHotBar = toContainer.getCapacity() == 9; // evaluate isHotBar for the destination container

            ItemStack oldItems = removeTransaction.getSlotBefore();
            ItemContext oldIC = new ItemContext(moveTransaction.getOtherContainer(), removeTransaction.getSlot(), oldItems);
            if (!settings.containsKeepItemMapElement(KeepItemMapComponent.getKey(oldIC)))
                return;

            SlotTransaction addTransaction = (SlotTransaction) moveTransaction.getAddTransaction();
            ItemStack newItems = addTransaction.getSlotAfter();
            ItemContext newIC = new ItemContext(toContainer, addTransaction.getSlot(), newItems);

            // start by removing the key from settings
            boolean oldKeepSetting = settings.getKeepItemMapElement(KeepItemMapComponent.getKey(oldIC));
            settings.removeKeepItemMapElement(KeepItemMapComponent.getKey(oldIC));
            // then set the key for the new item context with the old setting
            settings.setKeepItemMapElement(KeepItemMapComponent.getKey(newIC), oldKeepSetting);
            KeepItem.LOGGER.atInfo().log("Moved keep item setting for item " + oldItems.getItemId() + " from slot " + removeTransaction.getSlot() + (isFromHotBar ? " (hotbar)" : " (storage)") + " to slot " + addTransaction.getSlot() + (isToHotBar ? " (hotbar)" : " (storage)"));
            ref.getStore().putComponent(ref, KeepItemMapComponent.getComponentType(), settings);
        } else if (toContainer == backpack) { // process MOVE_TO_SELF type transactions to backpack only (from storage or hotbar)
            ItemStack oldItems = removeTransaction.getSlotBefore();
            ItemContext oldIC = new ItemContext(moveTransaction.getOtherContainer(), removeTransaction.getSlot(), oldItems);
            if (!settings.containsKeepItemMapElement(KeepItemMapComponent.getKey(oldIC)))
                return;
            // just remove the key from settings
            settings.removeKeepItemMapElement(KeepItemMapComponent.getKey(oldIC));
            KeepItem.LOGGER.atInfo().log("Removed keep item setting for item " + oldItems.getItemId() + " from slot " + removeTransaction.getSlot() + (fromContainer.getCapacity() == 9 ? " (hotbar)" : " (storage)") + " moved to backpack");
            ref.getStore().putComponent(ref, KeepItemMapComponent.getComponentType(), settings);
        } else {
            KeepItem.LOGGER.atInfo().log("Ignoring MOVE_TO_SELF transaction to/from other inventories.");
        }
    }
}
