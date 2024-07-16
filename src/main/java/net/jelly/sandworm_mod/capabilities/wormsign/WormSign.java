package net.jelly.sandworm_mod.capabilities.wormsign;

import net.minecraft.nbt.CompoundTag;

public class WormSign {
    private int wormSign;
    private int lastJumpTime;
    private int thisJumpTime;
    private double multiplier = 1;
    private int playSignTimer = 0;

    // WS
    public int getWS() {
        return wormSign;
    }
    public void addWS(int add) {
        this.wormSign += add;
    }
    public void subWS(int sub) {
        if(this.wormSign-sub > 0) this.wormSign -= sub;
        else this.wormSign = 0;
    }
    public void setWS(int ws) {this.wormSign = ws;}

    // multiplier
    public void addMultiplier(double mult) {
        multiplier+=mult;
        if(multiplier > 3) multiplier = 3;
        if(multiplier < 0) multiplier = 0;
        if(multiplier == Double.NaN) multiplier = 0;
    }

    public void setMultiplier(double mult) {
        multiplier=mult;
    }

    public double getMultiplier() {return multiplier;}

    // lastJumpTime
    public int getLastJumpTime() { return lastJumpTime; }
    public void setLastJumpTime(int val) { this.lastJumpTime = val; }

    // thisJumpTime
    public int getThisJumpTime() {
        return thisJumpTime;
    }
    public void addThisJumpTime(int add) {
        this.thisJumpTime += add;
    }
    public void setThisJumpTime(int val) { this.thisJumpTime = val; }
    public void decrementSignTimer() { if(playSignTimer > 0) playSignTimer--; }
    public void setSignTimer() { playSignTimer = 1100; }
    public int getSignTimer() { return playSignTimer; }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("worm_sign", wormSign);
    }

    public void loadNBTData(CompoundTag nbt) {
        wormSign = nbt.getInt("thirst");
    }
}
