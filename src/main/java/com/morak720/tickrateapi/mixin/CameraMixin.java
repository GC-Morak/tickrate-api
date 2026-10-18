package com.morak720.tickrateapi.mixin;

import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.util.TickRateAreaUtil;
import net.minecraft.client.Camera;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(method = "setup", at = @At("HEAD"), argsOnly = true)
    private float tickrateapi$modifyPartialTicks(float partialTicks, BlockGetter level, Entity entity, boolean detached, boolean thirdPerson) {
        if (entity != null) {
            CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
            if (timer != null) {
                return timer.getLivePartialTick(Util.getMillis());
            }
        }
        return partialTicks;
    }
}
