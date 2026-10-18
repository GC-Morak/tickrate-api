package com.morak720.tickrateapi.event;

import com.morak720.tickrateapi.TickRateAPI;
import com.morak720.tickrateapi.command.TickAreaCommand;
import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.data.TickRateSavedData;
import com.morak720.tickrateapi.network.TimerSyncPayload;
import com.morak720.tickrateapi.util.TickRateAreaUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = TickRateAPI.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ModEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        TickAreaCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();
        TickRateSavedData data = TickRateSavedData.get(level);

        List<TickRateArea> toRemove = new ArrayList<>();
        ChunkPos playerChunk = player.chunkPosition();

        for (TickRateArea area : data.getAreas()) {
            if (area.isToggleOnLeave() && area.getCreatorUuid().equals(player.getUUID())) {
                int distX = Math.abs(playerChunk.x - area.getChunkX());
                int distZ = Math.abs(playerChunk.z - area.getChunkZ());

                if (distX > area.getRadius() || distZ > area.getRadius()) {
                    toRemove.add(area);
                }
            }
        }

        if (!toRemove.isEmpty()) {
            for (TickRateArea area : toRemove) {
                data.removeArea(area);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();
        TickRateSavedData data = TickRateSavedData.get(level);

        List<TickRateArea> toRemove = new ArrayList<>();

        for (TickRateArea area : data.getAreas()) {
            if (area.isToggleOnLeave() && area.getCreatorUuid().equals(player.getUUID())) {
                toRemove.add(area);
            }
        }

        for (TickRateArea area : toRemove) {
            data.removeArea(area);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Entity trackedEntity = event.getTarget();
        CustomTimer timer = TickRateAreaUtil.getServerTimer(trackedEntity.getUUID());
        if (timer != null) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            TimerSyncPayload payload = new TimerSyncPayload(trackedEntity.getUUID(), (int) timer.getTickrate(), false);
            player.connection.send(new ClientboundCustomPayloadPacket(payload));
        }
    }
}
