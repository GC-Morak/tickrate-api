package com.morak720.tickrateapi.network;

import com.morak720.tickrateapi.TickRateAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record TimerSyncPayload(UUID entityUuid, int targetTickrate, boolean isReset) implements CustomPacketPayload {
    public static final Type<TimerSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TickRateAPI.MOD_ID, "timer_sync"));

    public static final StreamCodec<FriendlyByteBuf, TimerSyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, TimerSyncPayload>() {
        @Override
        public TimerSyncPayload decode(FriendlyByteBuf buf) {
            UUID uuid = buf.readUUID();
            int rate = buf.readVarInt();
            boolean reset = buf.readBoolean();
            return new TimerSyncPayload(uuid, rate, reset);
        }

        @Override
        public void encode(FriendlyByteBuf buf, TimerSyncPayload payload) {
            buf.writeUUID(payload.entityUuid());
            buf.writeVarInt(payload.targetTickrate());
            buf.writeBoolean(payload.isReset());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
