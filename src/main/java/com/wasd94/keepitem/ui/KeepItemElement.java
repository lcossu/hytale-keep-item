package com.wasd94.keepitem.ui;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceInteraction;
import com.hypixel.hytale.server.core.inventory.ItemContext;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class KeepItemElement extends ChoiceElement {
    private final ItemContext itemContext;
    private boolean isKept = false;

    public KeepItemElement(ItemContext itemContext, boolean isKept) {
        this.itemContext = itemContext;
        this.isKept = isKept;
        this.interactions = new ChoiceInteraction[]{new KeepItemInteraction(itemContext)};
    }

    public void addButton(@Nonnull UICommandBuilder commandBuilder, UIEventBuilder eventBuilder, String selector, PlayerRef playerRef) {
        commandBuilder.append("#ElementList", "KeepItemElement.ui");
        commandBuilder.set(selector + " #Icon.ItemId", this.itemContext.getItemStack().getItemId().toString());
        commandBuilder.set(selector + " #Name.TextSpans", Message.translation(this.itemContext.getItemStack().getItem().getTranslationKey()));
        String detail = "x" + this.itemContext.getItemStack().getQuantity();
        commandBuilder.set(selector + " #Detail.Text", detail);
        commandBuilder.set(selector + " #Selected.Text", isKept ? "Keep" : "");
    }
}
