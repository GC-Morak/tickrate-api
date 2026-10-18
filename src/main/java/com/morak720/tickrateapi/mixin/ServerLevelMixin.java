package com.morak720.tickrateapi.mixin;

import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.data.TickRateSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(method = "tickChunk", at = @At("HEAD"), cancellable = true)
    public void onTickChunk(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        ServerLevel level = (ServerLevel) (Object) this;
        if (level.isClientSide()) {
            return;
        }

        TickRateSavedData data = TickRateSavedData.get(level);
        ChunkPos chunkPos = chunk.getPos();

        for (TickRateArea area : data.getAreas()) {
            int distX = Math.abs(chunkPos.x - area.getChunkX());
            int distZ = Math.abs(chunkPos.z - area.getChunkZ());
            
            if (distX <= area.getRadius() && distZ <= area.getRadius()) {
                TickRateArea.TargetType targetType = area.getTargetType();
                if (targetType == TickRateArea.TargetType.WORLD || targetType == TickRateArea.TargetType.ALL) {
                    long tickCount = level.getServer().getTickCount();
                    int targetTickRate = area.getTargetTickRate();
                    
                    // Fórmula de tick rate fracionado aplicada ao tick do chunk
                    if (((tickCount * targetTickRate) % 20) >= targetTickRate) {
                        ci.cancel(); // Pula os ticks casuais (física, plantações, fluidos)
                    }
                }
                break;
            }
        }
    }
}
