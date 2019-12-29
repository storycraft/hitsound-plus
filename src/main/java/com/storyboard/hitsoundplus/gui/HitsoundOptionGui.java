package com.storyboard.hitsoundplus.gui;

import java.io.IOException;

import com.storyboard.hitsoundplus.HitsoundPlus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class HitsoundOptionGui extends GuiScreen {

    private static final int DONE_BTN = 4444;

    private static final int HITSOUND_CHECKBOX = 1234;
    private static final int COMBO_COUNTER_CHECKBOX = 2345;
    private static final int COMBO_BREAK_SOUND_CHECKBOX = 3456;

    private HitsoundPlus mod;

    private Minecraft client;

    private GuiScreen lastGui;

    private boolean isConfigLoaded;

    private GuiCheckBox hitsoundBox;
    private GuiCheckBox comboCounterBox;
    private GuiCheckBox comboBreakSoundBox;

    public HitsoundOptionGui(HitsoundPlus mod, GuiScreen lastGui) {
        super();

        this.mod = mod;
        
        this.lastGui = lastGui;

        client = mod.getClient();

        this.isConfigLoaded = false;

        initConfig();
    }

    protected void initConfig() {
        mod.reloadConfig().run().thenRun(() -> {
            isConfigLoaded = true;
        });
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(DONE_BTN, 135 + (this.width - 135) / 2 - 75, this.height - 35, 150, 20, "Done"));

        int half = this.height / 2;

        buttonList.add(hitsoundBox = new GuiCheckBox(HITSOUND_CHECKBOX, 10, half - 20, "Play hitsound", mod.isConfigHitsoundEnabled()));
        buttonList.add(comboCounterBox = new GuiCheckBox(COMBO_COUNTER_CHECKBOX, 10, half, "Show combo counter", mod.isConfigComboCounterEnabled()));
        buttonList.add(comboBreakSoundBox = new GuiCheckBox(COMBO_BREAK_SOUND_CHECKBOX, 10, half + 20, "Play sound on combo break", mod.isConfigCombobreakSoundEnabled()));

        super.initGui();
    }

    public void saveSettings() {
        mod.setConfigHitsoundEnabled(hitsoundBox.isChecked());
        mod.setConfigComboCounterEnabled(comboCounterBox.isChecked());
        mod.setConfigCombobreakSoundEnabled(comboBreakSoundBox.isChecked());

        mod.saveConfig().getSync();
        mod.applyFromConfig();
    }

    @Override
    protected void keyTyped(char typed, int key) throws IOException {
        if (key == 1) {
            saveSettings();
        }
 
        super.keyTyped(typed, key);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == DONE_BTN) {
            saveSettings();

            client.displayGuiScreen(lastGui);
            return;
        }
 
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawCenteredString(fontRendererObj, "Hitsound settings", this.width / 2, 15, 0xffffffff);

        if (!isConfigLoaded) {
            this.drawCenteredString(fontRendererObj, "Loading config...", this.width / 2,
                    this.height / 2, 0xffffffff);

            return;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}