package com.example.basicfabricmod.servermanager.ui.state;

import com.example.basicfabricmod.servermanager.ui.row.ManagedRowData;

import java.util.ArrayList;
import java.util.List;

public final class VisibleRowCache {
    private List<ManagedRowData> rows = List.of();
    private int hash;

    public boolean update(List<ManagedRowData> nextRows) {
        int nextHash = nextRows.hashCode();
        if (nextHash == hash && rows.size() == nextRows.size()) {
            return false;
        }
        rows = new ArrayList<>(nextRows);
        hash = nextHash;
        return true;
    }

    public List<ManagedRowData> rows() {
        return rows;
    }
}
