package com.example.basicfabricmod.servermanager.ui.state;

import com.example.basicfabricmod.servermanager.ui.row.ManagedRowData;

import java.util.ArrayList;
import java.util.List;

public final class MultiplayerUiState {
    private String searchQuery = "";
    private final List<ManagedRowData> visibleRows = new ArrayList<>();

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null ? "" : searchQuery;
    }

    public List<ManagedRowData> getVisibleRows() {
        return visibleRows;
    }

    public void replaceRows(List<ManagedRowData> rows) {
        visibleRows.clear();
        visibleRows.addAll(rows);
    }
}
