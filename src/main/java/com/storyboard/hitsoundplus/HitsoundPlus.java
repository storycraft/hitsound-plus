package com.storyboard.hitsoundplus;

import com.storyboard.hitsoundplus.config.ConfigManager;
import com.storyboard.hitsoundplus.config.json.JsonConfigFile;
import com.storyboard.hitsoundplus.config.json.JsonConfigPrettyFile;
import com.storyboard.hitsoundplus.gui.GuiManager;
import com.storyboard.hitsoundplus.hitsound.PlayerHitSound;
import com.storyboard.hitsoundplus.overlay.ComboCounter;
import com.storyboard.hitsoundplus.util.AsyncTask;

import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = HitsoundPlus.MODID, version = HitsoundPlus.VERSION, clientSideOnly = true)
public class HitsoundPlus 
{
    public static final String MODID = "hitsound-plus";
    public static final String VERSION = "1.0";

    private Minecraft client;
    private Logger logger;

    private GuiManager guiManager;

    private PlayerHitSound hitsoundPlayer;
    private ComboCounter comboCounter;
    
    private ConfigManager configManager;
    private JsonConfigFile configFile;

    @EventHandler
    public void onPreinit(FMLPreInitializationEvent e) {
        client = Minecraft.getMinecraft();
        logger = e.getModLog();

        configManager = new ConfigManager(e.getModConfigurationDirectory(), logger);
        configFile = new JsonConfigPrettyFile();
        configManager.loadConfig(configFile).getSync();
    }
    
    @EventHandler
    public void init(FMLInitializationEvent e) {
        guiManager = new GuiManager(this);
        
        hitsoundPlayer = new PlayerHitSound(this);
        comboCounter = new ComboCounter(this);

        MinecraftForge.EVENT_BUS.register(guiManager);

        MinecraftForge.EVENT_BUS.register(hitsoundPlayer);
        MinecraftForge.EVENT_BUS.register(comboCounter);

        reloadConfig().getSync();
        applyFromConfig();
    }
    
    public PlayerHitSound getHitsoundPlayer() {
        return hitsoundPlayer;
    }
    
    public ComboCounter getComboCounter() {
        return comboCounter;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public JsonConfigFile getConfigFile() {
        return configFile;
    }

    public Minecraft getClient() {
        return client;
    }

    public Logger getLogger() {
        return logger;
    }

    public AsyncTask<Void> saveConfig() {
        return configManager.saveConfig(configFile);
    }

    public AsyncTask<Void> reloadConfig() {
        return configManager.loadConfig(configFile);
    }

    public void applyFromConfig() {
        hitsoundPlayer.setSoundEnabled(isConfigHitsoundEnabled());

        comboCounter.setEnabled(isConfigComboCounterEnabled());
        comboCounter.setSoundEnabled(isConfigCombobreakSoundEnabled());
    }

    public boolean isConfigComboCounterEnabled() {
        if (!configFile.contains("combo_counter")) {
            setConfigComboCounterEnabled(true);
        }

        return configFile.get("combo_counter").getAsBoolean();
    }

    public boolean isConfigCombobreakSoundEnabled() {
        if (!configFile.contains("combobreak_sound")) {
            setConfigCombobreakSoundEnabled(true);
        }

        return configFile.get("combobreak_sound").getAsBoolean();
    }

    public boolean isConfigHitsoundEnabled() {
        if (!configFile.contains("sound_when_click_entity")) {
            setConfigHitsoundEnabled(true);
        }

        return configFile.get("sound_when_click_entity").getAsBoolean();
    }

    public void setConfigComboCounterEnabled(boolean flag) {
        configFile.set("combo_counter", flag);
    }

    public void setConfigCombobreakSoundEnabled(boolean flag) {
        configFile.set("combobreak_sound", flag);
    }

    public void setConfigHitsoundEnabled(boolean flag) {
        configFile.set("sound_when_click_entity", flag);
    }
    
}
