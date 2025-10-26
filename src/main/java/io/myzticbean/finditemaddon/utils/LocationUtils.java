/**
 * QSFindItemAddOn: An Minecraft add-on plugin for the QuickShop Hikari
 * and Reremake Shop plugins for Spigot server platform.
 * Copyright (C) 2021  myzticbean
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.myzticbean.finditemaddon.utils;

import io.myzticbean.finditemaddon.FindItemAddOn;
import io.myzticbean.finditemaddon.models.enums.PlayerPermsEnum;
import io.myzticbean.finditemaddon.utils.log.Logger;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author myzticbean
 */
@UtilityClass
public class LocationUtils {

    private static final Set<Material> damagingBlocks = new HashSet<>();
    private static final Set<Material> nonSuffocatingBlocks = new HashSet<>();
    private static final Set<Material> walledSignBlocks = new HashSet<>();
    private static final int BELOW_SAFE_BLOCK_CHECK_LIMIT = 20;

    static {
        walledSignBlocks.addAll(Arrays.stream(Material.values())
                .filter(p -> p.toString().toLowerCase().contains("_wall_sign"))
                .collect(Collectors.toSet())
        );
        // Initializing Damaging blocks
        damagingBlocks.add(Material.LAVA);
        damagingBlocks.add(Material.CACTUS);
        damagingBlocks.add(Material.CAMPFIRE);
        damagingBlocks.add(Material.SOUL_CAMPFIRE);
        damagingBlocks.add(Material.MAGMA_BLOCK);
        damagingBlocks.add(Material.FIRE);
        damagingBlocks.add(Material.SOUL_FIRE);
        damagingBlocks.add(Material.SWEET_BERRY_BUSH);
        damagingBlocks.add(Material.WITHER_ROSE);
        damagingBlocks.add(Material.END_PORTAL);

        // Initializing Non Suffocating blocks
        nonSuffocatingBlocks.add(Material.AIR);
        // Glass
        nonSuffocatingBlocks.add(Material.GLASS);
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("_glass"))
                        .collect(Collectors.toSet())
        );
        // Glass Panes
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("glass_pane"))
                        .collect(Collectors.toSet())
        );
        // Leaves
        nonSuffocatingBlocks.add(Material.ACACIA_LEAVES);
        nonSuffocatingBlocks.add(Material.BIRCH_LEAVES);
        nonSuffocatingBlocks.add(Material.DARK_OAK_LEAVES);
        nonSuffocatingBlocks.add(Material.JUNGLE_LEAVES);
        nonSuffocatingBlocks.add(Material.OAK_LEAVES);
        nonSuffocatingBlocks.add(Material.SPRUCE_LEAVES);
        nonSuffocatingBlocks.add(Material.AZALEA_LEAVES);
        nonSuffocatingBlocks.add(Material.FLOWERING_AZALEA_LEAVES);
        // Slabs
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("_slab"))
                        .collect(Collectors.toSet())
        );
        // Walled Signs
        nonSuffocatingBlocks.addAll(walledSignBlocks);
        // Hanging Signs
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("_hanging_sign"))
                        .collect(Collectors.toSet())
        );
        // Stairs
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("_stairs"))
                        .collect(Collectors.toSet())
        );
        // Fences
        nonSuffocatingBlocks.addAll(
                Arrays.stream(Material.values())
                        .filter(p -> p.toString().toLowerCase().contains("_fence"))
                        .collect(Collectors.toSet())
        );
        // MISC
        nonSuffocatingBlocks.add(Material.HONEY_BLOCK);
        nonSuffocatingBlocks.add(Material.BELL);
        nonSuffocatingBlocks.add(Material.CHEST);
        nonSuffocatingBlocks.add(Material.TRAPPED_CHEST);
        nonSuffocatingBlocks.add(Material.HOPPER);
        nonSuffocatingBlocks.add(Material.COMPOSTER);
        nonSuffocatingBlocks.add(Material.GRINDSTONE);
        nonSuffocatingBlocks.add(Material.STONECUTTER);
        nonSuffocatingBlocks.add(Material.END_PORTAL_FRAME);
        nonSuffocatingBlocks.add(Material.PISTON_HEAD);
    }

    @Nullable
    public static Location findSafeLocationAroundShop(Location shopLocation, Player player) {
        Logger.logDebugInfo("Finding safe location around the shop");
        Location roundedShopLoc = getRoundedDestination(shopLocation);
        Logger.logDebugInfo("Rounded location: " + roundedShopLoc.getX() + ", " + roundedShopLoc.getY() + ", " + roundedShopLoc.getZ());
        // Creating a list of four block locations in 4 sides of the shop
        List<Location> possibleSafeLocList = new ArrayList<>();
        possibleSafeLocList.add(new Location(
                roundedShopLoc.getWorld(),
                roundedShopLoc.getX() + 1,
                roundedShopLoc.getY(),
                roundedShopLoc.getZ()
        ));
        possibleSafeLocList.add(new Location(
                roundedShopLoc.getWorld(),
                roundedShopLoc.getX() - 1,
                roundedShopLoc.getY(),
                roundedShopLoc.getZ()
        ));
        possibleSafeLocList.add(new Location(
                roundedShopLoc.getWorld(),
                roundedShopLoc.getX(),
                roundedShopLoc.getY(),
                roundedShopLoc.getZ() + 1
        ));
        possibleSafeLocList.add(new Location(
                roundedShopLoc.getWorld(),
                roundedShopLoc.getX(),
                roundedShopLoc.getY(),
                roundedShopLoc.getZ() - 1
        ));
        for(Location possibleSafeLoc : possibleSafeLocList) {
            Logger.logDebugInfo("Possible safe location: " + possibleSafeLoc.getX() + ", " + possibleSafeLoc.getY() + ", " + possibleSafeLoc.getZ());
            if(possibleSafeLoc.getBlock().getType().equals(FindItemAddOn.getQsApiInstance().getShopSignMaterial())
                || walledSignBlocks.contains(possibleSafeLoc.getBlock().getType())) {
                Logger.logDebugInfo("Shop sign block found at " + possibleSafeLoc.getX() + ", " + possibleSafeLoc.getY() + ", " + possibleSafeLoc.getZ());
                // Adding a check for a safe location check bypass permission
                if(PlayerUtil.hasPermission(player, PlayerPermsEnum.FINDITEM_SHOPTP_BYPASS_SAFETYCHECK.value())) {
                    Location blockBelow = new Location(
                            possibleSafeLoc.getWorld(),
                            possibleSafeLoc.getBlockX(),
                            possibleSafeLoc.getBlockY() - 1,
                            possibleSafeLoc.getBlockZ()
                    );
                    possibleSafeLoc = lookAt(getRoundedDestination(new Location(
                                blockBelow.getWorld(),
                                blockBelow.getX(),
                                blockBelow.getY() + 1,
                                blockBelow.getZ()
                        )
                    ), roundedShopLoc);
                    return possibleSafeLoc;
                }
                // check if the block above is suffocating
                Location blockAbove = new Location(
                        possibleSafeLoc.getWorld(),
                        possibleSafeLoc.getBlockX(),
                        possibleSafeLoc.getBlockY() + 1,
                        possibleSafeLoc.getBlockZ());
                Logger.logDebugInfo("Block above shop sign: " + blockAbove.getX() + ", " + blockAbove.getY() + ", " + blockAbove.getZ());
                if(!isBlockSuffocating(blockAbove)) {
                    Location blockBelow = null;
                    boolean safeLocFound = false;
                    for(int i = 1; i <= BELOW_SAFE_BLOCK_CHECK_LIMIT; i++) {
                        blockBelow = new Location(
                                possibleSafeLoc.getWorld(),
                                possibleSafeLoc.getBlockX(),
                                possibleSafeLoc.getBlockY() - i,
                                possibleSafeLoc.getBlockZ()
                        );
                        Logger.logDebugInfo("Block below shop sign: "
                                + blockBelow.getBlock().getType() + " " + blockBelow.getX() + ", " + blockBelow.getY() + ", " + blockBelow.getZ());
                        if(blockBelow.getBlock().getType().equals(Material.AIR)
                            || blockBelow.getBlock().getType().equals(Material.CAVE_AIR)
                            || blockBelow.getBlock().getType().equals(Material.VOID_AIR)
                            || blockBelow.getBlock().getType().equals(FindItemAddOn.getQsApiInstance().getShopSignMaterial())) {
                            // do nothing and let the loop run
                            Logger.logDebugInfo("Shop or Air found below");
                        }
                        else if(!isBlockDamaging(blockBelow)) {
                            Logger.logDebugInfo("Safe block found!");
                            safeLocFound = true;
                            break;
                        }
                        else {
                            break;
                        }
                    }
                    if(safeLocFound) {
                        possibleSafeLoc = lookAt(getRoundedDestination(new Location(
                                        blockBelow.getWorld(),
                                        blockBelow.getX(),
                                        blockBelow.getY() + 1,
                                        blockBelow.getZ()
                                )
                        ), roundedShopLoc);
                        Logger.logDebugInfo("Safe location found: " + possibleSafeLoc.getX() + ", " + possibleSafeLoc.getY() + ", " + possibleSafeLoc.getZ());
                        return possibleSafeLoc;
                    }
                    else {
                        return null;
                    }
                }
                else {
                    Logger.logDebugInfo("Block above shop sign found not air. Block type: " + blockAbove.getBlock().getType());
                    return null;
                }
            }
            else {
                Logger.logDebugInfo("Block not shop sign. Block type: " + possibleSafeLoc.getBlock().getType());
            }
        }
        Logger.logDebugInfo("No safe block found near shop");
        return null;
    }

    // Found the below function from this thread: https://bukkit.org/threads/lookat-and-move-functions.26768/
    private static Location lookAt(Location loc, Location lookAt) {
        //Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();

        // Values of change in distance (make it relative)
        double dx = lookAt.getX() - loc.getX();
        double dy = lookAt.getY() - loc.getY();
        double dz = lookAt.getZ() - loc.getZ();

        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }

        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));

        // Set pitch
        loc.setPitch((float) -Math.atan(dy / dxz));

        // Set values, convert to degrees (invert the yaw since Bukkit uses a different yaw dimension format)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);

        return loc;
    }

    private static boolean isBlockDamaging(Location loc) {
        return damagingBlocks.contains(loc.getBlock().getType());
    }

    private static boolean isBlockSuffocating(Location loc) {
        return !nonSuffocatingBlocks.contains(loc.getBlock().getType());
    }

    private static Location getRoundedDestination(final Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int)Math.round(loc.getY());
        int z = loc.getBlockZ();
        return new Location(world, (double)x + 0.5D, (double)y, (double)z + 0.5D, loc.getYaw(), loc.getPitch());
    }
}
