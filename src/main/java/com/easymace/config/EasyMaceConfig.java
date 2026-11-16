package com.easymace.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EasyMaceConfig {
    private static EasyMaceConfig INSTANCE;
    public boolean enabled = true;
    public double slamHeightThreshold = 1.5;
    public String preferredEnchantForNormal = "breach";
    public String preferredEnchantForSlam = "density";
    public double hitboxMultiplier = 5.0;
    public boolean hitboxEnabled = true;
    public boolean hitboxAllEntities = true;
    public java.util.List<String> hitboxEntities = new java.util.ArrayList<>();

    public static EasyMaceConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("easymace.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (Files.exists(configPath)) {
            try {
                INSTANCE = gson.fromJson(Files.readString(configPath), EasyMaceConfig.class);
            } catch (Exception e) {
                INSTANCE = new EasyMaceConfig();
            }
        } else {
            INSTANCE = new EasyMaceConfig();
            try {
                Files.writeString(configPath, gson.toJson(INSTANCE));
            } catch (IOException ignored) {}
        }
        if (INSTANCE == null) INSTANCE = new EasyMaceConfig();
    }
}
