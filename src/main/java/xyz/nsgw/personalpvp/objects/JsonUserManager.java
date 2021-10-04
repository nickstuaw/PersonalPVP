/*
 * Copyright (c) 2021.
 */

package xyz.nsgw.personalpvp.objects;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import xyz.nsgw.personalpvp.PPVPPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class JsonUserManager implements UserManager {

    private final LoadingCache<@NotNull UUID, @NotNull PvpUser> userCache;
    private final File jsonFile;

    public JsonUserManager() {

        this.jsonFile = new File(PPVPPlugin.inst().getDataFolder(), "users.json");

        try {
            if (!this.jsonFile.exists()) {
                this.jsonFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.userCache = CacheBuilder.newBuilder()
                .removalListener(this::saveUser)
                .build(CacheLoader.from(this::loadUser));
    }

    private Map<UUID,PvpUser> getMap() {
        Map<UUID, PvpUser> map = new HashMap<>();
        try {
            Gson gson = new Gson();
            BufferedReader br = new BufferedReader(new FileReader(this.jsonFile));
            map = gson.fromJson(br, map.getClass());
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void saveMap(final @NotNull Map<UUID,PvpUser> map) {
        try {
            Gson gson = new Gson();
            BufferedWriter bw = new BufferedWriter(new FileWriter(this.jsonFile));
            bw.write(gson.toJson(map));
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PvpUser loadUser(@NotNull UUID uuid) {
        return getMap().getOrDefault(uuid, new PvpUser(uuid));
    }

    private void saveUser(@NotNull final RemovalNotification<@NotNull UUID, @NotNull PvpUser> notification) {
        PvpUser u = notification.getValue();
        Map<UUID,PvpUser> map = getMap();
        map.put(u.getUuid(), u);
        saveMap(map);
    }

    @Override
    public PvpUser wrap(@NotNull UUID key) {
        try {
            return this.userCache.get(key);
        }
        catch(final ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public PvpUser wrapIfLoaded(@NotNull UUID key) {
        return this.userCache.getIfPresent(key);
    }

    @Override
    public void validate(@NotNull PvpUser u) {
        this.userCache.put(u.getUuid(), u);
    }

    @Override
    public void invalidate(@NotNull PvpUser u) {
        this.userCache.invalidate(u.getUuid());
    }
}
