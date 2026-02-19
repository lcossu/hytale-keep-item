package com.wasd94.keepitem.components;


import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Component;

import java.util.HashMap;
import java.util.Map;

public class KeepItemMapComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, KeepItemMapComponent> type;

    public static ComponentType<EntityStore, KeepItemMapComponent> getComponentType() {
        return type;
    }

    public static void setComponentType(ComponentType<EntityStore, KeepItemMapComponent> type) {
        KeepItemMapComponent.type = type;
    }

    public static final BuilderCodec<KeepItemMapComponent> CODEC =
            BuilderCodec.builder(KeepItemMapComponent.class, KeepItemMapComponent::new)
                    .append(new KeyedCodec<>("KeepItemMap",
                                    new MapCodec<>(Codec.BOOLEAN, HashMap::new, false)),
                            (data, value) -> data.keepItemMap = value, // setter
                            data -> data.keepItemMap) // getter
                    .add()
                    .build();

    private Map<String, Boolean> keepItemMap = new HashMap<>();

    public Map<String, Boolean> getKeepItemMap() {
        return keepItemMap;
    }

    public void setKeepItemMapElement(String keepItemMapID, Boolean value) {
        if (!value){
            this.keepItemMap.remove(keepItemMapID);
            return;
        }
        this.keepItemMap.put(keepItemMapID, value);
    }

    public Boolean getKeepItemMapElement(String keepItemMapID) {
        return this.keepItemMap.getOrDefault(keepItemMapID, false);
    }

    public Boolean containsKeepItemMapElement(String keepItemMapID) {
        return this.keepItemMap.containsKey(keepItemMapID);
    }

    public void removeKeepItemMapElement(String keepItemMapID) {
        this.keepItemMap.remove(keepItemMapID);
    }

    public KeepItemMapComponent() {
    }

    public static String getKey(ItemContext context) {
        boolean isHotbar = context.getContainer().getCapacity() == 9;
        return (context.getSlot() + (isHotbar ? 36 : 0)) + "-" + context.getItemStack().getItemId();
    }

    @Override
    public Component<EntityStore> clone() {
        KeepItemMapComponent copy = new KeepItemMapComponent();
        return copy;
    }
}
