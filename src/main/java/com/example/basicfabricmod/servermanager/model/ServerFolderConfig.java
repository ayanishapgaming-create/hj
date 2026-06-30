package com.example.basicfabricmod.servermanager.model;

import java.util.ArrayList;
import java.util.List;

public final class ServerFolderConfig {
    private int version = 1;
    private List<ServerFolder> folders = new ArrayList<>();
    private List<FolderServerBinding> bindings = new ArrayList<>();

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<ServerFolder> getFolders() {
        return folders;
    }

    public void setFolders(List<ServerFolder> folders) {
        this.folders = folders == null ? new ArrayList<>() : folders;
    }

    public List<FolderServerBinding> getBindings() {
        return bindings;
    }

    public void setBindings(List<FolderServerBinding> bindings) {
        this.bindings = bindings == null ? new ArrayList<>() : bindings;
    }
}
