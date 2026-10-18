package com.morak720.tickrateapi.mixin;

import com.morak720.tickrateapi.data.CustomTimer;
import com.morak720.tickrateapi.util.TickRateAreaUtil;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private double tickrateapi$modifyX(double x, Entity entity, double x_orig, double y, double z, float rotation,
            float partialTicks) {
        CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
        if (timer != null) {
            float customTicks = timer.getLivePartialTick(Util.getMillis());
            return x + (entity.getX() - entity.xOld) * (customTicks - partialTicks);
        }
        return x;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private double tickrateapi$modifyY(double y, Entity entity, double x, double y_orig, double z, float rotation,
            float partialTicks) {
        CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
        if (timer != null) {
            float customTicks = timer.getLivePartialTick(Util.getMillis());
            return y + (entity.getY() - entity.yOld) * (customTicks - partialTicks);
        }
        return y;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private double tickrateapi$modifyZ(double z, Entity entity, double x, double y, double z_orig, float rotation,
            float partialTicks) {
        CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
        if (timer != null) {
            float customTicks = timer.getLivePartialTick(Util.getMillis());
            return z + (entity.getZ() - entity.zOld) * (customTicks - partialTicks);
        }
        return z;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float tickrateapi$modifyPartialTicks(float partialTicks, Entity entity, double x, double y, double z,
            float rotation, float partialTicks_param) {
        CustomTimer timer = TickRateAreaUtil.getClientTimer(entity.getUUID());
        if (timer != null) {
            return timer.getLivePartialTick(Util.getMillis());
        }
        return partialTicks;
    }
}
