package net.jelly.sandworm_mod.networking;

import net.jelly.sandworm_mod.SandwormMod;
import net.jelly.sandworm_mod.networking.packet.ExampleC2SPacket;
import net.jelly.sandworm_mod.networking.packet.ExampleS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(SandwormMod.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(ExampleC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ExampleC2SPacket::new)
                .encoder(ExampleC2SPacket::toBytes)
                .consumerMainThread(ExampleC2SPacket::handle)
                .add();

        net.messageBuilder(ExampleS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ExampleS2CPacket::new)
                .encoder(ExampleS2CPacket::toBytes)
                .consumerMainThread(ExampleS2CPacket::handle)
                .add();

//        net.messageBuilder(DrinkWaterC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
//                .decoder(DrinkWaterC2SPacket::new)
//                .encoder(DrinkWaterC2SPacket::toBytes)
//                .consumerMainThread(DrinkWaterC2SPacket::handle)
//                .add();
    }

    public static void sendToServer(Object message) {
        INSTANCE.sendToServer(message);
    }

    public static void sendToPlayer(Object message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}