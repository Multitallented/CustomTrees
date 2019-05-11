package org.redcastlemedia.bukkit.customtrees;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Multi on 11/19/2015.
 */
public class SaplingListener implements Listener {
    private final CustomTrees plugin;

    public SaplingListener(CustomTrees plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("worldedit")) {
            CustomTrees.worldEdit = (WorldEditPlugin) event.getPlugin();
        }
    }
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("worldedit")) {
            CustomTrees.worldEdit = null;
        }
    }


    @EventHandler
    public void onSaplingGrowth(StructureGrowEvent event) {
        if (event.isCancelled() || CustomTrees.worldEdit == null) {
            return;
        }
        Material saplingMaterial = event.getLocation().getBlock().getType();
        if (saplingMaterial != Material.OAK_SAPLING &&
                saplingMaterial != Material.BIRCH_SAPLING &&
                saplingMaterial != Material.SPRUCE_SAPLING &&
                saplingMaterial != Material.JUNGLE_SAPLING &&
                saplingMaterial != Material.DARK_OAK_SAPLING &&
                saplingMaterial != Material.ACACIA_SAPLING &&
                saplingMaterial != Material.BROWN_MUSHROOM &&
                saplingMaterial != Material.RED_MUSHROOM) {
            return;
        }

        Biome biome = event.getLocation().getBlock().getBiome();

        HashMap<Material, HashSet<CustomTree>> treeMap = CustomTrees.trees.get(biome.name());
        if (treeMap == null) {
            treeMap = CustomTrees.trees.get("DEFAULT");
            if (treeMap == null) {
                return;
            }
        }


        File schematicFile = null;
        int xOffset = 0;
        int yOffset = 0;
        int zOffset = 0;
        {
            HashSet<CustomTree> treeSet = treeMap.get(saplingMaterial);
            if (treeSet == null) {
                treeSet = CustomTrees.trees.get("DEFAULT").get(saplingMaterial);
            }
            if (treeSet == null) {
                return;
            }

            event.setCancelled(true);

            int totalWeight = 0;
            for (CustomTree customTree : treeSet) {
                totalWeight += customTree.getWeight();
            }
            double randomWeight = ((double) totalWeight) * Math.random();

            for (CustomTree customTree : treeSet) {
                randomWeight -= customTree.getWeight();
                if (randomWeight > 0) {
                    continue;
                }
                schematicFile = new File(plugin.getDataFolder(), customTree.getName() + ".schematic");
                xOffset = customTree.getXOffset();
                yOffset = customTree.getYOffset();
                zOffset = customTree.getZOffset();
                break;
            }

            if (schematicFile == null || !schematicFile.exists()) {
                System.out.println("schematic " + schematicFile.getName() + " does not exist");
                return;
            }
        }

        try {
            BlockVector3 to = BlockVector3.at(event.getLocation().getX() + xOffset, event.getLocation().getY() + yOffset, event.getLocation().getZ() + zOffset);
            Clipboard clipboard = ClipboardFormats.findByFile(schematicFile)
                    .getReader(new FileInputStream(schematicFile)).read();

            int length = clipboard.getRegion().getLength();
            int width = clipboard.getRegion().getWidth();
            clipboard.setOrigin(BlockVector3.at(width / 2 * -1,0,length / 2 * -1));

            //c1.setOffset(new Vector(width / 2 * -1, 0, length / 2 * -1));

            int a = (int) (event.getLocation().getX() + xOffset - (width / 2));
            int b = (int) (event.getLocation().getY() + yOffset);
            int c = (int) (event.getLocation().getZ() + zOffset - (length / 2));

            for (int x=a; x<a+width; x++) {
                for (int y=b; y<b+clipboard.getRegion().getHeight(); y++) {
                    for (int z=c; z<c+length; z++) {

                        //skip air blocks
                        if (clipboard.getBlock(BlockVector3.at(x-a, y-b, z-c)).getBlockType().getMaterial().isAir()) {
                            continue;
                        }

                        Block block = event.getWorld().getBlockAt(x, y, z);
                        if (block.getType().isSolid() &&
                                block.getType() != Material.OAK_LEAVES &&
                                block.getType() != Material.BIRCH_LEAVES &&
                                block.getType() != Material.SPRUCE_LEAVES &&
                                block.getType() != Material.JUNGLE_LEAVES &&
                                block.getType() != Material.DARK_OAK_LEAVES &&
                                block.getType() != Material.ACACIA_LEAVES &&
                                block.getType() != Material.GRASS &&
                                block.getType() != Material.DIRT &&
                                block.getType() != Material.MYCELIUM &&
                                block.getType() != Material.PODZOL &&
                                block.getType() != Material.STONE &&
                                block.getType() != Material.OAK_LOG &&
                                block.getType() != Material.BIRCH_LOG &&
                                block.getType() != Material.SPRUCE_LOG &&
                                block.getType() != Material.JUNGLE_LOG &&
                                block.getType() != Material.DARK_OAK_LOG &&
                                block.getType() != Material.ACACIA_LOG) {
                            return;
                        }

                        //TODO possibly check if the area is protected?
                    }
                }
            }

            AffineTransform transform = new AffineTransform();

            int rotation = (int) (Math.random() * 3.9999);
            rotation *= 90;
            if (rotation != 0) {
                transform.rotateY(rotation);
//                if (rotation == 90) {
//                    int tempOffset = xOffset;
//                    xOffset = -1 * zOffset;
//                    zOffset = tempOffset;
//                } else if (rotation == 180) {
//                    xOffset = -1 * xOffset;
//                    zOffset = -1 * zOffset;
//                } else if (rotation == 270) {
//                    int tempOffset = xOffset;
//                    xOffset = -1 * zOffset;
//                    zOffset = tempOffset;
//                }
            }

//            System.out.println(rotation + ": " + xOffset + "," + yOffset + "," + zOffset);

            event.getLocation().getBlock().setType(Material.AIR);

            EditSession session = CustomTrees.worldEdit.getWorldEdit().getEditSessionFactory()
                    .getEditSession(new BukkitWorld(event.getWorld()), -1);

            ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(),
                    clipboard.getOrigin(), session, to);

            if (!transform.isIdentity()) {
                copy.setTransform(transform);
            }
            copy.setSourceMask(new ExistingBlockMask(clipboard));

            Operations.completeLegacy(copy);
            session.flushSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
// c1.paste(session, new Vector(event.getLocation().getX() + xOffset, event.getLocation().getY() + yOffset, event.getLocation().getZ() + zOffset), true);
    }
}