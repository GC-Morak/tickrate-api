package com.morak720.tickrateapi.mixin;

import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.data.TickRateArea;
import com.morak720.tickrateapi.network.TimerSyncPayload;
import com.morak720.tickrateapi.util.TickRateAreaUtil;
import net.minecraft.Util;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "guardEntityTick", at = @At("HEAD"), cancellable = true)
    public void onGuardEntityTick(Consumer<Entity> tickConsumer, Entity entity, CallbackInfo ci) {
        Level level = (Level) (Object) this;

        // Lado do Cliente
        if (level.isClientSide()) {
            CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
            if (timer != null) {
                ci.cancel(); // Cancela o tick padrão do cliente

                int ticksToRun = timer.advanceTime(Util.getMillis());
                for (int i = 0; i < Math.min(10, ticksToRun); i++) {
                    tickConsumer.accept(entity);
                }
            }
            return;
        }

        // Lado do Servidor
        ServerLevel serverLevel = (ServerLevel) level;
        TickRateArea activeArea = TickRateAreaUtil.getActiveAreaForEntity(entity);
        UUID uuid = entity.getUUID();

        if (activeArea != null) {
            boolean shouldSlow = false;
            TickRateArea.TargetType targetType = activeArea.getTargetType();
            if (entity instanceof Player) {
                if (targetType == TickRateArea.TargetType.PLAYER || targetType == TickRateArea.TargetType.ALL) {
                    shouldSlow = true;
                }
            } else {
                if (targetType == TickRateArea.TargetType.WORLD || targetType == TickRateArea.TargetType.ALL) {
                    shouldSlow = true;
                }
            }

            if (shouldSlow) {
                ci.cancel(); // Cancela o tick padrão do servidor

                CustomTimer timer = TickRateAreaUtil.getServerTimer(uuid);
                int targetTickrate = activeArea.getTargetTickRate();

                if (timer == null) {
                    timer = new CustomTimer(targetTickrate, Util.getMillis());
                    TickRateAreaUtil.putServerTimer(uuid, timer);
                    tickrateapi$syncTimer(serverLevel, entity, targetTickrate, false);
                } else if (timer.getTickrate() != targetTickrate) {
                    timer.updateTickrate(targetTickrate);
                    tickrateapi$syncTimer(serverLevel, entity, targetTickrate, false);
                }

                int ticksToRun = timer.advanceTime(Util.getMillis());
                for (int i = 0; i < Math.min(10, ticksToRun); i++) {
                    tickConsumer.accept(entity);
                }
            } else {
                CustomTimer timer = TickRateAreaUtil.getServerTimer(uuid);
                if (timer != null) {
                    TickRateAreaUtil.removeServerTimer(uuid);
                    tickrateapi$syncTimer(serverLevel, entity, 20, true);
                }
            }
        } else {
            CustomTimer timer = TickRateAreaUtil.getServerTimer(uuid);
            if (timer != null) {
                TickRateAreaUtil.removeServerTimer(uuid);
                tickrateapi$syncTimer(serverLevel, entity, 20, true);
            }
        }
    }

    @Unique
    private void tickrateapi$syncTimer(ServerLevel level, Entity entity, int targetTickrate, boolean isReset) {
        TimerSyncPayload payload = new TimerSyncPayload(entity.getUUID(), targetTickrate, isReset);
        ClientboundCustomPayloadPacket packet = new ClientboundCustomPayloadPacket(payload);
        level.getChunkSource().broadcastAndSend(entity, packet);
        if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.connection.send(packet);
        }
    }
}
