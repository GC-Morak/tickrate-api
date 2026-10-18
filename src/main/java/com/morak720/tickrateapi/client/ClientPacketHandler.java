package com.morak720.tickrateapi.client;

import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.network.TimerSyncPayload;
import com.morak720.tickrateapi.util.TickRateAreaUtil;
import net.minecraft.Util;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPacketHandler {
    public static void handleTimerSync(final TimerSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.isReset()) {
                TickRateAreaUtil.removeClientTimer(payload.entityUuid());
            } else {
                CustomTimer timer = TickRateAreaUtil.getClientTimer(payload.entityUuid());
                if (timer == null) {
                    TickRateAreaUtil.putClientTimer(payload.entityUuid(), 
                        new CustomTimer(payload.targetTickrate(), Util.getMillis()));
                } else {
                    timer.updateTickrate(payload.targetTickrate());
                }
            }
        });
    }
}
