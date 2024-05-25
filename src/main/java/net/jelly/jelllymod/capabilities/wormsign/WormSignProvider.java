package net.jelly.jelllymod.capabilities.wormsign;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WormSignProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<WormSign> WS = CapabilityManager.get(new CapabilityToken<WormSign>() { });
    private WormSign wormSign = null;
    private final LazyOptional<WormSign> optional = LazyOptional.of(this::createWormSignCap);

    private WormSign createWormSignCap() {
        if(this.wormSign == null) {
            this.wormSign = new WormSign();
        }

        return this.wormSign;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == WS) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createWormSignCap().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createWormSignCap().loadNBTData(nbt);
    }
}
