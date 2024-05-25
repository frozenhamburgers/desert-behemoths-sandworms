package net.jelly.jelllymod.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import team.lodestar.lodestone.handlers.ScreenshakeHandler;
import team.lodestar.lodestone.systems.screenshake.ScreenshakeInstance;

import java.util.function.Supplier;

public class ExampleS2CPacket {
    private final float intensity;
    private final BlockPos position;

    public ExampleS2CPacket(float intensity, BlockPos position) {
        this.intensity = intensity;
        this.position = position;
    }

    public ExampleS2CPacket(FriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readBlockPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(this.intensity);
        buf.writeBlockPos(this.position);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // HERE WE ARE ON THE CLIENT!
            System.out.println("screenshake packet: " + intensity);
            Minecraft.getInstance().level.playLocalSound(this.position, SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 20f*intensity, intensity, true);
            // ScreenshakeHandler.addScreenshake(new ScreenshakeInstance(1).setIntensity(6.65f * this.intensity));
        });
        return true;
    }

}
