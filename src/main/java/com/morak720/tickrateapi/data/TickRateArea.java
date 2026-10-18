package com.morak720.tickrateapi.data;

import net.minecraft.nbt.CompoundTag;
import java.util.UUID;

public class TickRateArea {
    public enum TargetType {
        WORLD,
        PLAYER,
        ALL
    }

    private final int chunkX;
    private final int chunkZ;
    private final int radius;
    private final int targetTickRate;
    private final UUID creatorUuid;
    private final boolean toggleOnLeave;
    private final TargetType targetType;

    public TickRateArea(int chunkX, int chunkZ, int radius, int targetTickRate, UUID creatorUuid, boolean toggleOnLeave) {
        this(chunkX, chunkZ, radius, targetTickRate, creatorUuid, toggleOnLeave, TargetType.WORLD);
    }

    public TickRateArea(int chunkX, int chunkZ, int radius, int targetTickRate, UUID creatorUuid, boolean toggleOnLeave, TargetType targetType) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.radius = radius;
        this.targetTickRate = targetTickRate;
        this.creatorUuid = creatorUuid;
        this.toggleOnLeave = toggleOnLeave;
        this.targetType = targetType;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public int getRadius() {
        return radius;
    }

    public int getTargetTickRate() {
        return targetTickRate;
    }

    public UUID getCreatorUuid() {
        return creatorUuid;
    }

    public boolean isToggleOnLeave() {
        return toggleOnLeave;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("ChunkX", chunkX);
        tag.putInt("ChunkZ", chunkZ);
        tag.putInt("Radius", radius);
        tag.putInt("TargetTickRate", targetTickRate);
        tag.putUUID("CreatorUuid", creatorUuid);
        tag.putBoolean("ToggleOnLeave", toggleOnLeave);
        tag.putString("TargetType", targetType.name());
        return tag;
    }

    public static TickRateArea load(CompoundTag tag) {
        TargetType targetType = TargetType.WORLD;
        if (tag.contains("TargetType")) {
            try {
                targetType = TargetType.valueOf(tag.getString("TargetType"));
            } catch (IllegalArgumentException e) {
                targetType = TargetType.WORLD;
            }
        }
        return new TickRateArea(
            tag.getInt("ChunkX"),
            tag.getInt("ChunkZ"),
            tag.getInt("Radius"),
            tag.getInt("TargetTickRate"),
            tag.getUUID("CreatorUuid"),
            tag.getBoolean("ToggleOnLeave"),
            targetType
        );
    }
}
