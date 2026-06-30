package com.example.basicfabricmod.servermanager.ui.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class HoverAnimationController {
    private final Map<String, Float> values = new ConcurrentHashMap<>();

    public float update(String key, boolean hovered, boolean enabled) {
        if (!enabled) {
            values.put(key, hovered ? 1.0F : 0.0F);
            return hovered ? 1.0F : 0.0F;
        }
        return values.compute(key, (ignored, value) -> {
            float current = value == null ? 0.0F : value;
            float target = hovered ? 1.0F : 0.0F;
            float delta = 0.18F;
            if (Math.abs(target - current) < delta) {
                return target;
            }
            return current + Math.copySign(delta, target - current);
        });
    }
}
