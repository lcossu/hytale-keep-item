package com.wasd94.keepitem.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
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
    private final FlagArg print;

    public KeepItemCommand() {
        super("keepItem", "With this command you can open the UI to choose which items should not be moved during quick stack.", false);
        clear = this.withFlagArg("clear", "Clears all keep item settings.");
        print = this.withFlagArg("dump", "Print all keep item settings.");
        addAliases("ki", "keepitems", "kp");
    }

    @Override
    public boolean hasPermission(@NonNullDecl CommandSender sender) {
        // Every player can use this command
        return true;
    }

    @Override
    protected void execute(@NonNullDecl CommandContext commandContext, @NonNullDecl Store<EntityStore> store, @NonNullDecl Ref<EntityStore> ref, @NonNullDecl PlayerRef playerRef, @NonNullDecl World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        LOGGER.atInfo().log("Called KeepItemCommand");
        if (player == null) return;
        Inventory inventory = player.getInventory();
        if (inventory == null) return;
        if (clear.get(commandContext)) {
            KeepItemMapComponent keepItemMapComponent = store.getComponent(ref, KeepItemMapComponent.getComponentType());
            if (keepItemMapComponent != null) {
                keepItemMapComponent.getKeepItemMap().clear();
                store.putComponent(ref, KeepItemMapComponent.getComponentType(), keepItemMapComponent);
                LOGGER.atInfo().log("Cleared KeepItemMapComponent for player " + playerRef.getUsername());
                commandContext.sendMessage(Message.raw("Cleared all keep item settings."));
            }
        } else if (print.get(commandContext)) {
            KeepItemMapComponent keepItemMapComponent = store.getComponent(ref, KeepItemMapComponent.getComponentType());
            if (keepItemMapComponent != null) {
                commandContext.sendMessage(Message.raw("Current keep item settings: " + keepItemMapComponent.getKeepItemMap().toString()));
            } else {
                commandContext.sendMessage(Message.raw("No keep item settings found."));
            }
        } else {
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

        }
    }
}

