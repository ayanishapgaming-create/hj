package com.example.basicfabricmod.servermanager.service;

import com.example.basicfabricmod.geo.CountryLookupResult;
import com.example.basicfabricmod.geo.LookupStatus;
import com.example.basicfabricmod.geo.ServerCountryService;
import com.example.basicfabricmod.servermanager.model.FolderIcon;
import com.example.basicfabricmod.servermanager.model.FolderServerBinding;
import com.example.basicfabricmod.servermanager.model.FolderViewModel;
import com.example.basicfabricmod.servermanager.model.SearchMatcher;
import com.example.basicfabricmod.servermanager.model.ServerEntryViewModel;
import com.example.basicfabricmod.servermanager.model.ServerFolder;
import com.example.basicfabricmod.servermanager.model.ServerFolderConfig;
import com.example.basicfabricmod.servermanager.model.ServerManagerViewModel;
import com.example.basicfabricmod.servermanager.persistence.ServerFolderRepository;
import net.minecraft.client.network.ServerInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ServerFolderManager {
    private static final ServerFolderManager INSTANCE = new ServerFolderManager();

    private final ServerFolderRepository repository = new ServerFolderRepository();
    private ServerFolderConfig config = new ServerFolderConfig();

    private ServerFolderManager() {
        reload();
    }

    public static ServerFolderManager getInstance() {
        return INSTANCE;
    }

    public synchronized void reload() {
        this.config = repository.load();
        normalize();
    }

    public synchronized void save() {
        normalize();
        repository.save(config);
    }

    public synchronized List<ServerFolder> getFolders() {
        return new ArrayList<>(config.getFolders());
    }

    public synchronized ServerFolder createFolder(String name) {
        ServerFolder folder = new ServerFolder(UUID.randomUUID().toString(), name, FolderIcon.FOLDER, false, false, config.getFolders().size());
        config.getFolders().add(folder);
        save();
        return folder;
    }

    public synchronized ServerFolder duplicateFolder(String folderId) {
        ServerFolder source = getFolder(folderId);
        if (source == null) {
            return null;
        }
        ServerFolder duplicate = new ServerFolder(UUID.randomUUID().toString(), source.getName(), source.getIcon(), source.isPinned(), source.isCollapsed(), config.getFolders().size());
        config.getFolders().add(duplicate);
        for (FolderServerBinding binding : new ArrayList<>(config.getBindings())) {
            if (binding.folderId().equals(folderId)) {
                config.getBindings().add(new FolderServerBinding(binding.serverAddress(), duplicate.getId(), binding.favorite(), binding.order()));
            }
        }
        save();
        return duplicate;
    }

    public synchronized void deleteFolder(String folderId, boolean moveServersToRoot) {
        config.setFolders(config.getFolders().stream().filter(folder -> !folder.getId().equals(folderId)).toList());
        if (moveServersToRoot) {
            List<FolderServerBinding> remapped = new ArrayList<>();
            for (FolderServerBinding binding : config.getBindings()) {
                if (binding.folderId().equals(folderId)) {
                    remapped.add(new FolderServerBinding(binding.serverAddress(), "", binding.favorite(), nextOrderForFolder("")));
                } else {
                    remapped.add(binding);
                }
            }
            config.setBindings(remapped);
        } else {
            config.setBindings(config.getBindings().stream().filter(binding -> !binding.folderId().equals(folderId)).toList());
        }
        save();
    }

    public synchronized void clearFolder(String folderId) {
        List<FolderServerBinding> remapped = new ArrayList<>();
        for (FolderServerBinding binding : config.getBindings()) {
            if (binding.folderId().equals(folderId)) {
                remapped.add(new FolderServerBinding(binding.serverAddress(), "", binding.favorite(), nextOrderForFolder("")));
            } else {
                remapped.add(binding);
            }
        }
        config.setBindings(remapped);
        save();
    }

    public synchronized void setPinned(String folderId, boolean pinned) {
        ServerFolder folder = getFolder(folderId);
        if (folder != null) {
            folder.setPinned(pinned);
            save();
        }
    }

    public synchronized void reorderFolder(String folderId, int newOrder) {
        ServerFolder folder = getFolder(folderId);
        if (folder == null) {
            return;
        }
        List<ServerFolder> folders = new ArrayList<>(config.getFolders());
        folders.remove(folder);
        folders.add(Math.max(0, Math.min(newOrder, folders.size())), folder);
        for (int i = 0; i < folders.size(); i++) {
            folders.get(i).setOrder(i);
        }
        config.setFolders(folders);
        save();
    }

    public synchronized void reorderServers(List<String> addresses, String targetFolderId) {
        int order = 0;
        List<FolderServerBinding> updated = new ArrayList<>();
        for (FolderServerBinding binding : config.getBindings()) {
            if (addresses.contains(binding.serverAddress())) {
                continue;
            }
            updated.add(binding);
        }
        for (String address : addresses) {
            FolderServerBinding previous = getBinding(address);
            boolean favorite = previous != null && previous.favorite();
            updated.add(new FolderServerBinding(address, targetFolderId == null ? "" : targetFolderId, favorite, order++));
        }
        config.setBindings(updated);
        save();
    }

    public synchronized void toggleCollapsed(String folderId) {
        for (ServerFolder folder : config.getFolders()) {
            if (folder.getId().equals(folderId)) {
                folder.setCollapsed(!folder.isCollapsed());
                break;
            }
        }
        save();
    }

    public synchronized void setFavorite(String address, boolean favorite) {
        FolderServerBinding binding = getBinding(address);
        String folderId = binding == null ? "" : binding.folderId();
        int order = binding == null ? nextOrderForFolder(folderId) : binding.order();
        upsertBinding(new FolderServerBinding(address, folderId, favorite, order));
        save();
    }

    public synchronized void setFavorites(List<String> addresses, boolean favorite) {
        for (String address : addresses) {
            FolderServerBinding binding = getBinding(address);
            String folderId = binding == null ? "" : binding.folderId();
            int order = binding == null ? nextOrderForFolder(folderId) : binding.order();
            upsertBinding(new FolderServerBinding(address, folderId, favorite, order));
        }
        save();
    }

    public synchronized boolean isFavorite(String address) {
        FolderServerBinding binding = getBinding(address);
        return binding != null && binding.favorite();
    }

    public synchronized void assignServerToFolder(String address, String folderId) {
        FolderServerBinding binding = getBinding(address);
        boolean favorite = binding != null && binding.favorite();
        upsertBinding(new FolderServerBinding(address, folderId == null ? "" : folderId, favorite, nextOrderForFolder(folderId)));
        save();
    }

    public synchronized ServerManagerViewModel buildViewModel(List<ServerInfo> servers, String searchQuery) {
        String normalizedSearch = searchQuery == null ? "" : searchQuery.trim();
        boolean searching = !normalizedSearch.isBlank();
        Map<String, FolderServerBinding> bindingMap = config.getBindings().stream().collect(Collectors.toMap(FolderServerBinding::serverAddress, binding -> binding, (a, b) -> b));
        List<ServerEntryViewModel> rootServers = new ArrayList<>();
        Map<String, List<ServerEntryViewModel>> byFolder = new HashMap<>();

        for (ServerInfo server : servers) {
            FolderServerBinding binding = bindingMap.getOrDefault(server.address, new FolderServerBinding(server.address, "", false, nextOrderForFolder("")));
            CountryLookupResult country = ServerCountryService.getInstance().getOrRequest(server);
            String countryName = country != null && country.status() == LookupStatus.RESOLVED ? country.countryInfo().countryName() : "Unknown Location";
            PingDisplayCache.PingDisplayData pingData = PingDisplayCache.getInstance().get(server);
            ServerEntryViewModel viewModel = new ServerEntryViewModel(server, binding.folderId(), binding.favorite(), binding.order(), countryName, pingData.quality(), pingData.lastUpdatedTime());
            if (!SearchMatcher.matchesServer(viewModel, normalizedSearch)) {
                continue;
            }
            if (binding.folderId().isBlank()) {
                rootServers.add(viewModel);
            } else {
                byFolder.computeIfAbsent(binding.folderId(), ignored -> new ArrayList<>()).add(viewModel);
            }
        }

        Comparator<ServerEntryViewModel> serverComparator = Comparator
                .comparing(ServerEntryViewModel::favorite).reversed()
                .thenComparingInt(ServerEntryViewModel::order)
                .thenComparing(entry -> entry.serverInfo().name.toLowerCase(Locale.ROOT));
        rootServers.sort(serverComparator);

        List<FolderViewModel> folders = config.getFolders().stream()
                .sorted(Comparator.comparing(ServerFolder::isPinned).reversed().thenComparingInt(ServerFolder::getOrder).thenComparing(ServerFolder::getName, String.CASE_INSENSITIVE_ORDER))
                .map(folder -> {
                    List<ServerEntryViewModel> folderServers = new ArrayList<>(byFolder.getOrDefault(folder.getId(), List.of()));
                    folderServers.sort(serverComparator);
                    boolean folderMatches = SearchMatcher.matchesFolder(folder, normalizedSearch);
                    if (!folderMatches && folderServers.isEmpty()) {
                        return null;
                    }
                    return new FolderViewModel(folder, folderServers, folderServers.size());
                })
                .filter(folder -> folder != null)
                .toList();

        return new ServerManagerViewModel(rootServers, folders, normalizedSearch, searching);
    }

    public synchronized ServerFolder getFolder(String folderId) {
        for (ServerFolder folder : config.getFolders()) {
            if (folder.getId().equals(folderId)) {
                return folder;
            }
        }
        return null;
    }

    private void normalize() {
        List<ServerFolder> normalizedFolders = new ArrayList<>();
        Map<String, Boolean> seenIds = new HashMap<>();
        int order = 0;
        for (ServerFolder folder : config.getFolders()) {
            if (folder == null) {
                continue;
            }
            String id = folder.getId();
            if (id == null || id.isBlank() || seenIds.containsKey(id)) {
                id = UUID.randomUUID().toString();
                folder = new ServerFolder(id, folder.getName(), folder.getIcon(), folder.isPinned(), folder.isCollapsed(), order);
            } else {
                folder.setOrder(order);
            }
            seenIds.put(id, Boolean.TRUE);
            normalizedFolders.add(folder);
            order++;
        }
        config.setFolders(normalizedFolders);
        config.setBindings(config.getBindings().stream().filter(binding -> binding != null && binding.serverAddress() != null && !binding.serverAddress().isBlank()).toList());
    }

    private FolderServerBinding getBinding(String address) {
        for (FolderServerBinding binding : config.getBindings()) {
            if (binding.serverAddress().equals(address)) {
                return binding;
            }
        }
        return null;
    }

    private void upsertBinding(FolderServerBinding replacement) {
        List<FolderServerBinding> bindings = new ArrayList<>();
        boolean replaced = false;
        for (FolderServerBinding binding : config.getBindings()) {
            if (binding.serverAddress().equals(replacement.serverAddress())) {
                bindings.add(replacement);
                replaced = true;
            } else {
                bindings.add(binding);
            }
        }
        if (!replaced) {
            bindings.add(replacement);
        }
        config.setBindings(bindings);
    }

    private int nextOrderForFolder(String folderId) {
        int max = -1;
        for (FolderServerBinding binding : config.getBindings()) {
            if (binding.folderId().equals(folderId == null ? "" : folderId)) {
                max = Math.max(max, binding.order());
            }
        }
        return max + 1;
    }
}
