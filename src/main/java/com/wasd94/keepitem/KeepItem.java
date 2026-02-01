package com.wasd94.keepitem;


import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.wasd94.keepitem.commands.KeepItemCommand;
import com.wasd94.keepitem.components.KeepItemMapComponent;
import com.wasd94.keepitem.events.InventoryChangeKeepItemHandler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class KeepItem extends JavaPlugin {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();


    public KeepItem(@NonNullDecl JavaPluginInit init) {
        super(init);
    }


    @Override
    protected void setup() {
        this.getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, InventoryChangeKeepItemHandler::revertQuickStackForKeepItems);
        this.getCommandRegistry().registerCommand(new KeepItemCommand());

        ComponentType<EntityStore, KeepItemMapComponent> poisonComponentType = this.getEntityStoreRegistry()
                .registerComponent(KeepItemMapComponent.class, "KeepItemMapComponent", KeepItemMapComponent.CODEC);
        KeepItemMapComponent.setComponentType(poisonComponentType);

        super.setup();
    }

}
