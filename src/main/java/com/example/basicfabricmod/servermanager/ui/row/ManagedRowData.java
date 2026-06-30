package com.example.basicfabricmod.servermanager.ui.row;

import com.example.basicfabricmod.servermanager.model.ServerEntryViewModel;
import com.example.basicfabricmod.servermanager.model.ServerFolder;

public record ManagedRowData(
        ManagedRowKind kind,
        ServerFolder folder,
        ServerEntryViewModel server,
        String label,
        int serverCount
) {
    public static ManagedRowData root(String label, int count) {
        return new ManagedRowData(ManagedRowKind.ROOT, null, null, label, count);
    }

    public static ManagedRowData folder(ServerFolder folder, int count) {
        return new ManagedRowData(ManagedRowKind.FOLDER, folder, null, folder.getName(), count);
    }

    public static ManagedRowData server(ServerEntryViewModel server) {
        return new ManagedRowData(ManagedRowKind.SERVER, null, server, server.serverInfo().name, 0);
    }
}
