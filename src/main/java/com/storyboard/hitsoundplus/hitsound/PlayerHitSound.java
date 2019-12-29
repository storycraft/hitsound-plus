package com.storyboard.hitsoundplus.hitsound;

import com.storyboard.hitsoundplus.HitsoundPlus;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerHitSound {

    private HitsoundPlus mod;

    private boolean soundEnabled;

    private ResourceLocation soundHitNormalLoc;
    private ResourceLocation soundHitClapLoc;
    private ResourceLocation soundHitFinishLoc;

    private boolean lastSprintState;
    private boolean playerResprinted;

    public PlayerHitSound(HitsoundPlus mod) {
        this.mod = mod;

        this.soundEnabled = false;

        this.soundHitNormalLoc = new ResourceLocation(HitsoundPlus.MODID, "hitsound.normal");
        this.soundHitClapLoc = new ResourceLocation(HitsoundPlus.MODID, "hitsound.clap");
        this.soundHitFinishLoc = new ResourceLocation(HitsoundPlus.MODID, "hitsound.finish");

        this.lastSprintState = false;
        this.playerResprinted = false;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    @SubscribeEvent
    public void capturePlayerSprint(RenderWorldLastEvent e) {
        EntityPlayerSP player = mod.getClient().thePlayer;

        boolean sprinting = player.isSprinting();

        if (lastSprintState != sprinting) {
            if (sprinting) {
                this.playerResprinted = true;
            }

            lastSprintState = sprinting;
        }
    }

    @SubscribeEvent
    public void onLeftInteract(AttackEntityEvent e){
        if (e.entityPlayer == null || !e.entityPlayer.isUser() || !soundEnabled){
            return;
        }

        Entity target = e.target;
        EntityPlayerSP attacker = (EntityPlayerSP) e.entityPlayer;

        boolean crit = attacker.fallDistance > 0.0F && !attacker.onGround && !attacker.isOnLadder() && !attacker.isInWater() && !attacker.isPotionActive(Potion.blindness) && !attacker.isRiding();

        World world = attacker.getEntityWorld();

        world.playSound(target.posX, target.posY, target.posZ, soundHitNormalLoc.toString(), 1f, 1f, false);

        if (playerResprinted) { //W tap
            world.playSound(target.posX, target.posY, target.posZ, soundHitFinishLoc.toString(), 1f, 1f, false);
            playerResprinted = false;
        }
    
        if (crit) { //Crit
            world.playSound(target.posX, target.posY, target.posZ, soundHitClapLoc.toString(), 1f, 1f, false);
        }
    }
}
