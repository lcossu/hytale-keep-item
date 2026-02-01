package com.wasd94.keepitem.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.wasd94.keepitem.components.KeepItemMapComponent;
import com.wasd94.keepitem.ui.KeepItemPage;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class KeepItemCommand extends AbstractPlayerCommand {
    private final FlagArg clear;

    public KeepItemCommand() {
        super("keepItem", "With this command you can open the UI to choose which items should not be moved during quick stack.", false);
        clear = this.withFlagArg("clear", "Clears all keep item settings.");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        LOGGER.atInfo().log("Called KeepItemCommand");
        if (player == null) return;
        Inventory inventory = player.getInventory();
        if (inventory == null) return;
        if (!clear.get(commandContext)) {
            if (player.getPageManager().getCustomPage() == null) {
                KeepItemMapComponent keepItemMapComponent = store.getComponent(ref, KeepItemMapComponent.getComponentType());
                if (keepItemMapComponent == null) {
                    keepItemMapComponent = new KeepItemMapComponent();
                }
                // Get elements to show
                player.getPageManager().openCustomPage(ref, store, new KeepItemPage(playerRef, inventory, keepItemMapComponent));
            } else {
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } else {
            KeepItemMapComponent keepItemMapComponent = store.getComponent(ref, KeepItemMapComponent.getComponentType());
            if (keepItemMapComponent != null) {
                keepItemMapComponent.getKeepItemMap().clear();
                store.putComponent(ref, KeepItemMapComponent.getComponentType(), keepItemMapComponent);
                LOGGER.atInfo().log("Cleared KeepItemMapComponent for player " + playerRef.getUsername());
            }
        }
    }
}

