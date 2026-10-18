package com.morak720.tickrateapi.mixin;

import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.data.TickRateSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity")
public class BoundTickingBlockEntityMixin {
    @Shadow @Final LevelChunk this$0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void onTick(CallbackInfo ci) {
        Level level = this$0.getLevel();
        
        // 1. Isola totalmente o lado do cliente
        if (level.isClientSide()) {
            return;
        }

        // Lado do Servidor
        ServerLevel serverLevel = (ServerLevel) level;
        TickRateSavedData data = TickRateSavedData.get(serverLevel);

        TickingBlockEntity ticker = (TickingBlockEntity) (Object) this;
        BlockPos pos = ticker.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);

        for (TickRateArea area : data.getAreas()) {
            int distX = Math.abs(chunkPos.x - area.getChunkX());
            int distZ = Math.abs(chunkPos.z - area.getChunkZ());
            
            if (distX <= area.getRadius() && distZ <= area.getRadius()) {
                TickRateArea.TargetType targetType = area.getTargetType();
                if (targetType == TickRateArea.TargetType.WORLD || targetType == TickRateArea.TargetType.ALL) {
                    long tickCount = serverLevel.getServer().getTickCount();
                    int targetTickRate = area.getTargetTickRate();
                    
                    // Fórmula de tick rate fracionado aplicada ao tick do BlockEntity no servidor
                    if (((tickCount * targetTickRate) % 20) >= targetTickRate) {
                        ci.cancel(); // Pula o tick de máquinas/fornalhas/etc. nesta rodada
                    }
                }
                break;
            }
        }
    }
}
