package com.storyboard.hitsoundplus.gui;

import com.storyboard.hitsoundplus.HitsoundPlus;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiManager {

    private static final int ENTRY_BTN_ID = 191229;

    private HitsoundPlus mod;

    public GuiManager(HitsoundPlus mod) {
        this.mod = mod;
    }

    @SubscribeEvent
    public void onGuiInit(InitGuiEvent.Post e) {
        if (e.gui instanceof GuiOptions) {
            e.buttonList.add(new GuiButton(ENTRY_BTN_ID, e.gui.width - 100, e.gui.height - 44, 98, 20, "Hitsound option"));
        }
    }

    @SubscribeEvent
    public void onGuiAction(ActionPerformedEvent.Post e) {
        if (e.gui instanceof GuiOptions && e.button.id == ENTRY_BTN_ID) {
            mod.getClient().displayGuiScreen(new HitsoundOptionGui(mod, e.gui));
        }
    }

}