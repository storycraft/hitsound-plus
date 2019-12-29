package com.storyboard.hitsoundplus.overlay;

import java.util.ArrayList;
import java.util.List;

import com.storyboard.hitsoundplus.HitsoundPlus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class ComboCounter {

    private static final int LEFT_MARGIN = 5;
    private static final int BOTTOM_MARGIN = 5;

    private static final float POPOUT_SCALE = .6f;
    private static final float POPOUT_SMALL_SCALE = .1f;

    private static final float POPOUT_DURATION = 150f;

    private static final float IDLE_START = 5000;
    private static final float COMBO_FADE = 500;

    private static final int SCREEN_SPRITE_WIDTH = 15;
    private static final int SCREEN_SPRITE_HEIGHT = 20;

    private static final int MIN_COMBO_REQUIRED = 10;

    private HitsoundPlus mod;
    private Minecraft minecraft;

    private List<ResourceLocation> numberTextureList;
    private ResourceLocation numberXTexture;

    private volatile boolean enabled;
    private volatile boolean soundEnabled;

    private long lastComboChange;
    private int lastCombo;
    private int currentCombo;

    private float lastHurtTime;

    private MovingObjectPosition lastPos;

    private ResourceLocation soundComboBreak;

    public ComboCounter(HitsoundPlus mod) {
        this.mod = mod;
        this.minecraft = Minecraft.getMinecraft();

        this.enabled = false;
        this.soundEnabled = false;

        this.currentCombo = 0;
        this.lastHurtTime = 0;

        this.soundComboBreak = new ResourceLocation(HitsoundPlus.MODID, "combo.break");

        this.lastPos = null;

        initNumberTexture();
    }

    public int getCombo() {
        return currentCombo;
    }

    public int getLastCombo() {
        return lastCombo;
    }

    public long getLastComboChange() {
        return lastComboChange;
    }

    public void setCombo(int combo) {
        this.lastCombo = combo;
        this.currentCombo = combo;
        this.lastComboChange = System.currentTimeMillis();
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    protected void initNumberTexture() {
        Minecraft minecraft = Minecraft.getMinecraft();

        this.numberTextureList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            ResourceLocation resource = new ResourceLocation(HitsoundPlus.MODID, "overlay/score-" + i + ".png");
            numberTextureList.add(resource);

            minecraft.getTextureManager().loadTexture(resource, new SimpleTexture(resource));
        }

        this.numberXTexture = new ResourceLocation(HitsoundPlus.MODID, "overlay/score-x.png");
        minecraft.getTextureManager().loadTexture(numberXTexture, new SimpleTexture(numberXTexture));
    }

    @SubscribeEvent
    public void onWorldChange(PlayerChangedDimensionEvent e) {
        if (e.player.isUser()) {
            setCombo(0);
        }
    }

    @SubscribeEvent
    public void onLogin(ClientConnectedToServerEvent e) {
        setCombo(0);
    }

    @SubscribeEvent
    public void onPlayerHurt(PlayerTickEvent e) {
        if (e.phase == Phase.START && e.player.isUser() && soundEnabled) {
            if (lastHurtTime != e.player.hurtTime) {
                if (lastHurtTime < e.player.hurtTime) {
                    if (getCombo() >= MIN_COMBO_REQUIRED) {
                        e.player.playSound(soundComboBreak.toString(), 1f, 1f);
                    }

                    setCombo(0);
                }

                lastHurtTime = e.player.hurtTime;
            }
        }
    }

    
    @SubscribeEvent
    public void onHit(AttackEntityEvent e) {
        if (e.entityPlayer == null || !enabled || !e.entityPlayer.isUser()) {
            return;
        }

        if ((System.currentTimeMillis() - getLastComboChange()) >= IDLE_START) {
            setCombo(0);
        }

        setCombo(getCombo() + 1);
    }

    @SubscribeEvent
    public void onRenderEnd(RenderWorldLastEvent e) {
        if (soundEnabled && currentCombo > 0) {
            Minecraft mc = mod.getClient();
            MovingObjectPosition pos = mc.objectMouseOver;

            if (pos != null) {
                if (lastPos != null && pos != lastPos
                    && lastPos.typeOfHit == MovingObjectType.ENTITY && pos.typeOfHit != MovingObjectType.ENTITY) {

                    if (!lastPos.entityHit.isDead && currentCombo >= MIN_COMBO_REQUIRED && pos.typeOfHit == MovingObjectType.MISS) {
                        mc.thePlayer.playSound(soundComboBreak.toString(), 1f, 1f);
                    }
                    setCombo(0);
                }

                lastPos = pos;
            }
        }
    }

    @SubscribeEvent
    public void onScreenDraw(RenderGameOverlayEvent.Post e) {
        if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && enabled && getCombo() > 0) {
            long timeFromLastHit = (System.currentTimeMillis() - getLastComboChange());
            boolean fade = timeFromLastHit >= IDLE_START;
            
            float alpha = 1;

            if (fade) {
                if (timeFromLastHit - COMBO_FADE >= IDLE_START) {
                    return;
                }

                alpha = 1 - (timeFromLastHit - IDLE_START) / COMBO_FADE;
            }

            ResourceLocation[] list = getRequiredTexture(getCombo());

            ScaledResolution scaledresolution = new ScaledResolution(minecraft);
            int width = scaledresolution.getScaledWidth();
            int height = scaledresolution.getScaledHeight();

            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            
            boolean drawScaleOverlay = timeFromLastHit <= POPOUT_DURATION;
            boolean overlayScale = timeFromLastHit >= 150 && (timeFromLastHit - 150) <= POPOUT_DURATION;

            float scaleOverlayProgress = 0;
            float overlayProgress = 1;

            if (drawScaleOverlay)
                scaleOverlayProgress = timeFromLastHit / POPOUT_DURATION;

            if (overlayScale)
                overlayProgress = (timeFromLastHit - 150) / POPOUT_DURATION;

            int i;
            for (i = 0; i < list.length; i++) {
                ResourceLocation resource = list[i];

                drawComboText(i, width, height, resource, drawScaleOverlay, scaleOverlayProgress, overlayProgress, alpha);
            }

            drawComboText(i, width, height, numberXTexture, drawScaleOverlay, scaleOverlayProgress, overlayProgress, alpha);

            GlStateManager.color(1, 1, 1); //restore to avoid hud fade

            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
        }
    }

    private void drawComboText(int index, int screenWidth, int screenHeight, ResourceLocation resource, boolean drawScaleOverlay, float scaleOverlayProgress, float overlayProgress, float alpha) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        GlStateManager.translate(LEFT_MARGIN + index * SCREEN_SPRITE_WIDTH, screenHeight - BOTTOM_MARGIN, 0);

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        minecraft.getTextureManager().bindTexture(resource);

        if (drawScaleOverlay) {
            GlStateManager.color(1, 1, 1, (1 - scaleOverlayProgress) * 0.75f * alpha);

            float scaleOverlayScale = 1 + POPOUT_SCALE - scaleOverlayProgress * POPOUT_SCALE;
            GlStateManager.scale(scaleOverlayScale, scaleOverlayScale, 1);
            Gui.drawModalRectWithCustomSizedTexture(0, -SCREEN_SPRITE_HEIGHT, 0, 0, SCREEN_SPRITE_WIDTH, SCREEN_SPRITE_HEIGHT, SCREEN_SPRITE_WIDTH, SCREEN_SPRITE_HEIGHT);
        }

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        GlStateManager.color(1, 1, 1, alpha);

        float overlayScale = 1 + POPOUT_SMALL_SCALE - scaleOverlayProgress * POPOUT_SMALL_SCALE;
        GlStateManager.scale(overlayScale, overlayScale, 1);

        Gui.drawModalRectWithCustomSizedTexture(0, -SCREEN_SPRITE_HEIGHT, 0, 0, SCREEN_SPRITE_WIDTH, SCREEN_SPRITE_HEIGHT, SCREEN_SPRITE_WIDTH, SCREEN_SPRITE_HEIGHT);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    protected ResourceLocation[] getRequiredTexture(int combo) {
        int size = ((int) Math.log10(combo)) + 1;
        ResourceLocation[] list = new ResourceLocation[size];

        for (int i = size - 1; i >= 0; i--) {
            list[i] = numberTextureList.get(combo % 10);
            combo /= 10;
        }
        return list;
    }
}
