package org.redcastlemedia.bukkit.customtrees;

import org.bukkit.Material;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomTree {
    private int weight;
    private int xOffset;
    private int yOffset;
    private int zOffset;
    private String name;
    private Material material;
}
