package com.example.basicfabricmod.duck;

import net.minecraft.client.network.ServerInfo;

/**
 * Implemented by MultiplayerServerListWidgetEntryMixin to expose the entry's
 * private ServerInfo field, so other mixins can match our managed list's
 * selection back onto the real (hidden) vanilla ServerEntry instances.
 */
public interface ServerEntryAccessor {
    ServerInfo basicfabricmod$getServer();
}
