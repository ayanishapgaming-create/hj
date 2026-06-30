package com.example.basicfabricmod.servermanager.ui.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Small animation helper for folder expand/collapse transitions.
 */
public final class FolderAnimationController {
    private static final long DURATION_MS = 180L;
    private final Map<String, AnimationState> states = new ConcurrentHashMap<>();

    public float getExpansion(String folderId, boolean expanded, boolean animate) {
        if (!animate) {
            return expanded ? 1.0F : 0.0F;
        }

        long now = System.currentTimeMillis();
        AnimationState state = states.compute(folderId, (id, old) -> {
            if (old == null || old.targetExpanded != expanded) {
                float current = old == null ? (expanded ? 1.0F : 0.0F) : old.progressAt(now);
                return new AnimationState(current, expanded, now);
            }
            return old;
        });
        return state.progressAt(now);
    }

    private record AnimationState(float startProgress, boolean targetExpanded, long startTime) {
        private float progressAt(long now) {
            float elapsed = (now - startTime) / (float) DURATION_MS;
            if (elapsed >= 1.0F) {
                return targetExpanded ? 1.0F : 0.0F;
            }
            float start = startProgress;
            float end = targetExpanded ? 1.0F : 0.0F;
            return start + (end - start) * elapsed;
        }
    }
}
