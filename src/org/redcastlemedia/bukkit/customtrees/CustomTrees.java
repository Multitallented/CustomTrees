package org.redcastlemedia.bukkit.customtrees;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Created by Multi on 11/19/2015.
 */
public class CustomTrees extends JavaPlugin {

    public static WorldEditPlugin worldEdit = null;
    public static FileConfiguration config;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        }
        Bukkit.getPluginManager().registerEvents(new SaplingListener(this), this);

        File folder = getDataFolder();
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                getLogger().severe("Unable to create data folder.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            try {
                if (!configFile.createNewFile()) {
                    getLogger().severe("Unable to create config.yml");
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
                }
            } catch (Exception e) {
                getLogger().severe("Unable to create config.yml");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        config = new YamlConfiguration();
        try {
            config.load(configFile);

        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("Failed to read from config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {

    }
}
