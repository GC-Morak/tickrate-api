package com.morak720.tickrateapi.data;

public class CustomTimer {
    private float targetTickrate;
    private float msPerTick;
    private double partialTick;
    private long lastMs;
    private long lastTickTimeMs;

    public CustomTimer(float targetTickrate, long currentMillis) {
        this.targetTickrate = targetTickrate;
        this.msPerTick = 1000.0f / targetTickrate;
        this.lastMs = currentMillis;
        this.lastTickTimeMs = currentMillis;
        this.partialTick = 0.0;
    }

    public float getTickrate() {
        return this.targetTickrate;
    }

    public void updateTickrate(float newTickrate) {
        if (this.targetTickrate != newTickrate) {
            this.targetTickrate = newTickrate;
            this.msPerTick = 1000.0f / newTickrate;
            // Mantém partialTick mas ajusta para o novo ritmo
        }
    }

    public int advanceTime(long currentMillis) {
        long deltaMs = currentMillis - lastMs;
        if (deltaMs < 0) deltaMs = 0;
        lastMs = currentMillis;

        double tickDelta = (double) deltaMs / msPerTick;
        partialTick += tickDelta;

        int ticksToRun = (int) partialTick;
        if (ticksToRun > 0) {
            partialTick -= ticksToRun;
            lastTickTimeMs = currentMillis;
        }
        return ticksToRun;
    }

    public float getLivePartialTick(long currentMillis) {
        if (msPerTick <= 0) return 0.0f;
        float progress = (float) (currentMillis - lastTickTimeMs) / msPerTick;
        if (progress < 0.0f) progress = 0.0f;
        if (progress > 1.0f) progress = 1.0f;
        return progress;
    }
}
