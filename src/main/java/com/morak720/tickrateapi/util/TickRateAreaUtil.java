package com.morak720.tickrateapi.util;

import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.data.TickRateSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TickRateAreaUtil {
    private static final Map<UUID, CustomTimer> SERVER_TIMERS = new HashMap<>();
    private static final Map<UUID, CustomTimer> CLIENT_TIMERS = new HashMap<>();

    public static TickRateArea getActiveAreaForEntity(Entity entity) {
        if (entity.level().isClientSide()) {
            return null;
        }

        ServerLevel level = (ServerLevel) entity.level();
        TickRateSavedData data = TickRateSavedData.get(level);
        ChunkPos entityChunk = entity.chunkPosition();

        for (TickRateArea area : data.getAreas()) {
            int distX = Math.abs(entityChunk.x - area.getChunkX());
            int distZ = Math.abs(entityChunk.z - area.getChunkZ());

            if (distX <= area.getRadius() && distZ <= area.getRadius()) {
                return area;
            }
        }
        return null;
    }

    public static Map<UUID, CustomTimer> getServerTimers() {
        return SERVER_TIMERS;
    }

    public static Map<UUID, CustomTimer> getClientTimers() {
        return CLIENT_TIMERS;
    }

    public static CustomTimer getServerTimer(UUID uuid) {
        return SERVER_TIMERS.get(uuid);
    }

    public static CustomTimer getClientTimer(UUID uuid) {
        return CLIENT_TIMERS.get(uuid);
    }

    public static void putServerTimer(UUID uuid, CustomTimer timer) {
        SERVER_TIMERS.put(uuid, timer);
    }

    public static void putClientTimer(UUID uuid, CustomTimer timer) {
        CLIENT_TIMERS.put(uuid, timer);
    }

    public static void removeServerTimer(UUID uuid) {
        SERVER_TIMERS.remove(uuid);
    }

    public static void removeClientTimer(UUID uuid) {
        CLIENT_TIMERS.remove(uuid);
    }
}
