package org.redcastlemedia.bukkit.customtrees;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

/**
 * Created by Multi on 11/19/2015.
 */
public class SaplingListener implements Listener {
    private final CustomTrees plugin;

    public SaplingListener(CustomTrees plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("unused")
    public void onSaplingGrowth(StructureGrowEvent event) {
        Material saplingMaterial = event.getLocation().getBlock().getType();
        if (!Util.isGrowableMaterial(saplingMaterial)) {
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
        Clipboard clipboard = null;
        CustomTree selectedCustomTree = null;
        {
            HashSet<CustomTree> treeSet = treeMap.get(saplingMaterial);
            if (treeSet == null) {
                treeSet = CustomTrees.trees.get("DEFAULT").get(saplingMaterial);
            }
            if (treeSet == null) {
                return;
            }

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
                File schematicsFolder = new File (plugin.getDataFolder(), "schematics");
                schematicFile = new File(schematicsFolder, customTree.getName() + ".schematic");

                if (!schematicFile.exists()) {
                    schematicFile = new File(schematicsFolder, customTree.getName() + ".schem");
                    if (!schematicFile.exists()) {
                        plugin.getLogger().log(Level.SEVERE, "schematic {0} does not exist", customTree.getName());
                        return;
                    }
                }
                xOffset = customTree.getXOffset();
                yOffset = customTree.getYOffset();
                zOffset = customTree.getZOffset();

                final ClipboardFormat schematicFormat = ClipboardFormats.findByFile(schematicFile);

                if(schematicFormat == null) {
                    plugin.getLogger().log(Level.SEVERE, "corrupt schematic file {0}", schematicFile.getName());
                    return;
                }

                try {
                    clipboard = schematicFormat.load(schematicFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
            if (schematicFile == null) {
                plugin.getLogger().log(Level.SEVERE, "no custom tree found");
                return;
            }


            event.setCancelled(true);
        }

        try {
            AffineTransform transform = new AffineTransform();

            int rotation = new Random().nextInt(3);
            rotation *= 90;
            if (rotation != 0) {
                transform = transform.rotateY(rotation);
                if (rotation == 90) {
                    int tempOffset = xOffset;
                    xOffset = -1 * zOffset;
                    zOffset = tempOffset;
                } else if (rotation == 180) {
                    xOffset = -1 * xOffset;
                    zOffset = -1 * zOffset;
                } else if (rotation == 270) {
                    int tempOffset = zOffset;
                    xOffset = -1 * zOffset;
                    zOffset = tempOffset;
                }

            }

            BlockVector3 pastePosition = BlockVector3.at(event.getLocation().getX() + xOffset,
                                              event.getLocation().getY() + yOffset,
                                              event.getLocation().getZ() + zOffset);

            int length = clipboard.getRegion().getLength();
            int width = clipboard.getRegion().getWidth();
            clipboard.setOrigin(BlockVector3.at(width / 2,0,length / 2));

            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            if (rotation != 0) {
                clipboardHolder.setTransform(transform);
            }

            int a = (int) (event.getLocation().getX() + xOffset - (width / 2));
            int b = (int) (event.getLocation().getY() + yOffset);
            int c = (int) (event.getLocation().getZ() + zOffset - (length / 2));

            for (int x=a; x<a+width; x++) {
                for (int y=b; y<b+clipboard.getRegion().getHeight(); y++) {
                    for (int z=c; z<c+length; z++) {

                        //skip air blocks
                        if (clipboard.getBlock(BlockVector3.at(x-a, y-b, z-c))
                                .getBlockType().getMaterial().isAir()) {
                            continue;
                        }

                        Block block = event.getWorld().getBlockAt(x, y, z);
                        if (!Util.isNaturalMaterial(block.getType())) {
                            return;
                        }

                        //TODO possibly check if the area is protected?
                    }
                }
            }

            event.getLocation().getBlock().setType(Material.AIR);

            com.sk89q.worldedit.world.World faweWorld = BukkitAdapter.adapt(event.getWorld());

            final EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(faweWorld, -1);

            Operation operation = clipboardHolder
                    .createPaste(editSession)
                    .to(pastePosition)
                    .ignoreAirBlocks(true)
                    .build();


            try {
                Operations.complete(operation);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }
            editSession.flushSession();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
