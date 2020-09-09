package org.redcastlemedia.bukkit.customtrees;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Multi on 11/19/2015.
 */
public class CustomTrees extends JavaPlugin {

    public static FileConfiguration config;
    public static final HashMap<String, HashMap<Material, HashSet<CustomTree>>> trees = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new SaplingListener(this), this);

        File folder = getDataFolder();
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                getLogger().severe("Unable to create data folder.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        }
        File schematicsFolder = new File(folder, "schematics");
        if (!schematicsFolder.exists()) {
            getLogger().severe("Unable to find schematics folder.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
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
        loadTrees();
    }

    private void loadTrees() {
        File biomeFolder = new File(getDataFolder(), "biomes");
        if (!biomeFolder.exists()) {
            getLogger().severe("Unable to find biomes folder");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        for (File file : biomeFolder.listFiles()) {
            FileConfiguration config = new YamlConfiguration();
            HashMap<Material, HashSet<CustomTree>> treeMap = new HashMap<>();
            try {
                config.load(file);
                for (String materialName : config.getConfigurationSection("materials").getKeys(false)) {
                    ConfigurationSection section  = config.getConfigurationSection("materials." + materialName);

                    Material material = Material.valueOf(materialName);
                    HashSet<CustomTree> matSet = new HashSet<>();

                    for (String treeName : section.getKeys(false)) {
                        CustomTree customTree = new CustomTree();
                        customTree.setMaterial(material);
                        customTree.setName(treeName);
                        customTree.setXOffset(section.getInt(treeName + ".x-offset", 0));
                        customTree.setYOffset(section.getInt(treeName + ".y-offset", 0));
                        customTree.setZOffset(section.getInt(treeName + ".z-offset", 0));
                        customTree.setWeight(section.getInt(treeName + ".weight", 100));
                        matSet.add(customTree);
                    }
                    treeMap.put(material, matSet);
                }
            } catch (Exception e) {
                e.printStackTrace();
                getLogger().severe("Unable to read " + file.getName());
            }
            for (String biomeName : config.getStringList("biomes")) {
                trees.put(biomeName.toUpperCase(), treeMap);
            }
        }
    }

    @Override
    public void onDisable() {

    }
}
