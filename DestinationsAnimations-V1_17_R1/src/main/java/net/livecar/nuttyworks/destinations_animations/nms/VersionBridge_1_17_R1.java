package net.livecar.nuttyworks.destinations_animations.nms;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.nms.v1_17_R1.entity.EntityHumanNPC;
import net.citizensnpcs.nms.v1_17_R1.entity.SkeletonWitherController;
import net.citizensnpcs.nms.v1_17_R1.entity.WitherController;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Location;
import net.livecar.nuttyworks.destinations_animations.plugin.FacingDirection;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.*;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftVillager;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;
import java.util.logging.Level;

public class VersionBridge_1_17_R1 implements VersionBridge {
    //Fishing Variables
    private double hookGravity = 0.125D;

    @Override
    public void SleepNPC(NPC npc, Player player, float yaw) {
        final NPCDestinationsTrait destTrait = npc.getTrait(NPCDestinationsTrait.class);
        Double blockY = destTrait.currentLocation.destination.getY();
        Double blockX = destTrait.currentLocation.destination.getX();
        Double blockZ = destTrait.currentLocation.destination.getZ();
        Float locYaw = destTrait.currentLocation.destination.getYaw();
        npc.teleport(destTrait.currentLocation.destination, PlayerTeleportEvent.TeleportCause.PLUGIN);

        while (true) {
            Location destLoc = destTrait.currentLocation.destination.clone();
            destLoc.setY(blockY);

            switch (DestinationsPlugin.Instance.getMCUtils.getSlabType(destLoc.getBlock())) {
                case DOUBLE:
                case TOP:
                    blockY = destTrait.currentLocation.destination.clone().getBlockY() + 1D;
                    continue;
                case BOTTOM:
                    blockY = destTrait.currentLocation.destination.clone().getBlockY() + 0.50D;
                    continue;
                default:
                    break;
            }

            if (destLoc.getBlock().getBlockData() instanceof Bed) {
                Bed targetBed = (Bed) destLoc.getBlock().getBlockData();
                switch (getBlockDirection(destLoc.getBlock())) {
                    case NORTH:
                        if (targetBed.getPart() == Bed.Part.FOOT)
                            blockZ = blockZ - 1D;
                        break;
                    case EAST:
                        if (targetBed.getPart() == Bed.Part.FOOT)
                            blockX = blockX + 1D;
                        break;
                    case SOUTH:
                        if (targetBed.getPart() == Bed.Part.FOOT)
                            blockZ = blockZ + 1D;
                        break;
                    case WEST:
                        if (targetBed.getPart() == Bed.Part.FOOT)
                            blockZ = blockZ - 1D;
                        break;
                }

                blockY = destTrait.currentLocation.destination.clone().getBlockY() + 0.50D;
                break;
            }

            if (destLoc.getBlock().getType() == Material.FARMLAND) {
                blockY = destTrait.currentLocation.destination.clone().getBlockY() + 0.94D;
                break;
            }

            if (destLoc.getBlock().getType() == Material.DIRT_PATH) {
                blockY = destTrait.currentLocation.destination.clone().getBlockY() + 0.94D;
                break;
            }

            if (destLoc.getBlock().getType().isSolid()) {
                blockY = destTrait.currentLocation.destination.clone().getBlockY() + 1D;
                continue;
            }
            break;
        }

        final BlockPos blockPosition = new BlockPos(blockX,
                blockY,
                blockZ);

        final Location blockLoc = new Location(npc.getEntity().getWorld(), blockX, blockY, blockZ);

        new BukkitRunnable() {
            @Override
            public void run() {
                switch (npc.getEntity().getType()) {
                    case WITHER_SKELETON:
                        ((WitherSkeleton) ((SkeletonWitherController) npc.getEntity()).getBukkitEntity()).startSleeping(blockPosition);
                        //getHandle(((SkeletonWith)npc.getEntity())).entitySleep(blockPosition);
                        break;
                    case ZOMBIE_VILLAGER:
                        ((ZombieVillager) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case CREEPER:
                        ((Creeper) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case SKELETON:
                        ((Skeleton) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case GIANT:
                        ((Giant) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case ZOMBIE:
                        ((Zombie) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case PIGLIN:
                        ((Piglin) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case ZOMBIFIED_PIGLIN:
                        ((ZombifiedPiglin) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case ENDERMAN:
                        ((EnderMan) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case WITCH:
                        ((Witch) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case WOLF:
                        ((Wolf) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case VILLAGER:
                        ((CraftVillager) npc.getEntity()).sleep(blockLoc);
                        //((EntityVillager) npc.getEntity()).entitySleep(blockPosition);
                        break;
                    case DROWNED:
                        ((Drowned) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case PILLAGER:
                        ((Pillager) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case WANDERING_TRADER:
                        ((WanderingTrader) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case FOX:
                        ((Fox) npc.getEntity()).startSleeping(blockPosition);
                        break;
                    case PLAYER:
                        getHandle(((EntityHumanNPC.PlayerNPC) npc.getEntity())).startSleeping(blockPosition);
                        break;
                }
            }
        }.runTaskLater(DestinationsPlugin.Instance, 2L);

        final Float finYaw = locYaw;

        new BukkitRunnable() {
            @Override
            public void run() {
                net.citizensnpcs.util.Util.assumePose(npc.getEntity(), finYaw, 0);
            }
        }.runTaskLater(DestinationsPlugin.Instance, 15L);

    }

    private static ServerPlayer getServerPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static net.minecraft.world.entity.LivingEntity getHandle(org.bukkit.entity.LivingEntity entity) {
        if (!(entity instanceof CraftEntity)) {
            return null;
        }
        return (LivingEntity) ((CraftEntity) entity).getHandle();
    }

    @Override
    public void unsleepNPC(NPC npc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                switch (npc.getEntity().getType()) {
                    case WITHER_SKELETON:
                        getHandle(((WitherController.WitherNPC) npc.getEntity())).stopSleeping();
                        break;
                    case ZOMBIE_VILLAGER:
                        ((ZombieVillager) npc.getEntity()).stopSleeping();
                        break;
                    case CREEPER:
                        ((Creeper) npc.getEntity()).stopSleeping();
                        break;
                    case SKELETON:
                        ((Skeleton) npc.getEntity()).stopSleeping();
                        break;
                    case GIANT:
                        ((Giant) npc.getEntity()).stopSleeping();
                        break;
                    case ZOMBIE:
                        ((Zombie) npc.getEntity()).stopSleeping();
                        break;
                    case ZOMBIFIED_PIGLIN:
                        ((ZombifiedPiglin) npc.getEntity()).stopSleeping();
                        break;
                    case PIGLIN:
                        ((Piglin) npc.getEntity()).stopSleeping();
                        break;
                    case ENDERMAN:
                        ((EnderMan) npc.getEntity()).stopSleeping();
                        break;
                    case WITCH:
                        ((Witch) npc.getEntity()).stopSleeping();
                        break;
                    case WOLF:
                        ((Wolf) npc.getEntity()).stopSleeping();
                        break;
                    case VILLAGER:
                        ((Villager) npc.getEntity()).stopSleeping();
                        break;
                    case DROWNED:
                        ((Drowned) npc.getEntity()).stopSleeping();
                        break;
                    case PILLAGER:
                        ((Pillager) npc.getEntity()).stopSleeping();
                        break;
                    case WANDERING_TRADER:
                        ((WanderingTrader) npc.getEntity()).stopSleeping();
                        break;
                    case FOX:
                        ((Fox) npc.getEntity()).stopSleeping();
                        break;
                    case PLAYER:
                        getHandle(((EntityHumanNPC.PlayerNPC) npc.getEntity())).stopSleeping();
                        break;
                }
            }
        }.runTaskLater(DestinationsPlugin.Instance, 5L);
    }

    //Chest animation code
    @Override
    public void changeChestState(Location blockLocation, Boolean open) {
        int openClose = (open) ? (byte) 1 : 0; // The byte of data used for the
        // note and animation packet (1
        // if true, 0 if false)

        BlockPos position = new BlockPos(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        //PacketPlayOutBlockAction
        ClientboundBlockEventPacket clientboundBlockEventPacket;

        for (Player player : Bukkit.getOnlinePlayers()) {
            ServerPlayer serverPlayer = getServerPlayer(player);
            ServerGamePacketListenerImpl connection = serverPlayer.connection;

            switch (blockLocation.getBlock().getType()) {
                case CHEST:
                case TRAPPED_CHEST:
                    if (open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_CHEST_OPEN"), 1F, 1F);
                    if (!open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_CHEST_CLOSE"), 1F, 1F);

                   connection.send(new ClientboundBlockEventPacket(position, Blocks.CHEST, 1, openClose));
                    break;
                case ENDER_CHEST:
                    if (open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_ENDER_CHEST_OPEN"), 1F, 1F);
                    if (!open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_ENDER_CHEST_CLOSE"), 1F, 1F);

                    connection.send(new ClientboundBlockEventPacket(position, Blocks.ENDER_CHEST, 1, openClose));
                    break;
                case SHULKER_BOX:
                case BLACK_SHULKER_BOX:
                case CYAN_SHULKER_BOX:
                case BLUE_SHULKER_BOX:
                case BROWN_SHULKER_BOX:
                case GRAY_SHULKER_BOX:
                case GREEN_SHULKER_BOX:
                case LIGHT_BLUE_SHULKER_BOX:
                case LIGHT_GRAY_SHULKER_BOX:
                case LIME_SHULKER_BOX:
                case MAGENTA_SHULKER_BOX:
                case ORANGE_SHULKER_BOX:
                case PINK_SHULKER_BOX:
                case PURPLE_SHULKER_BOX:
                case RED_SHULKER_BOX:
                case WHITE_SHULKER_BOX:
                case YELLOW_SHULKER_BOX:
                    if (open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("ENTITY_SHULKER_OPEN"), 1F, 1F);
                    if (!open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("ENTITY_SHULKER_CLOSE"), 1F, 1F);
                    connection.send(new ClientboundBlockEventPacket(position, Blocks.WHITE_SHULKER_BOX, 1, openClose));
                    break;
                case BARREL:
                    if (open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_BARREL_OPEN"), 1F, 1F);
                    if (!open)
                        player.getWorld().playSound(blockLocation, Sound.valueOf("BLOCK_BARREL_CLOSE"), 1F, 1F);
                    break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack[] getFishDrops() {
        // Need to add a random fish to the inventory of the NPC
        Double perc = (Math.random() * 100) + 1;
        ItemStack[] drop = new ItemStack[1];
        if (perc < 60) {
            // raw fish
            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Fish Caught (Raw_Fish:0) Fish");
            drop[0] = new ItemStack(Material.TROPICAL_FISH, 1, (short) 0);
        } else if (perc < 75) {
            // raw salmon
            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Fish Caught (Raw_Fish:1) Salmon");
            drop[0] = new ItemStack(Material.TROPICAL_FISH, 1, (short) 1);
        } else if (perc < 78) {
            // clownfish
            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Fish Caught (Raw_Fish:1) Clown");
            drop[0] = new ItemStack(Material.TROPICAL_FISH, 1, (short) 2);
        } else {
            // pufferfish
            DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Fish Caught (Raw_Fish:1) Puffer");
            drop[0] = new ItemStack(Material.TROPICAL_FISH, 1, (short) 3);
        }
        return drop;
    }

    @Override
    public Material getWaterBlock() {
        return Material.WATER;
    }

    @Override
    public FishHook CastFishingLine(Location destination, NPC npc, int gain) {
        Location from = npc.getEntity().getLocation().clone();
        from = from.add(0, .33, 0);

        ServerLevel nmsWorld = ((CraftWorld) destination.getWorld()).getHandle();
        FishingHook hook = new FishingHook(((CraftPlayer) npc.getEntity()).getHandle(), nmsWorld, 1, 1);
        nmsWorld.addEntity(hook, CreatureSpawnEvent.SpawnReason.DEFAULT);
        FishHook thisHook = (FishHook) hook.getBukkitEntity();
        thisHook.setShooter((ProjectileSource) npc.getEntity());
        thisHook.setVelocity(calculateVelocity(from.toVector(), destination.toVector(), gain));
        return thisHook;
    }

    @Override
    public void PlaySound(Location location, Animations_Location setting) {
        for (Player plrEntity : Bukkit.getOnlinePlayers()) {
            if (plrEntity.getWorld() == location.getWorld()) {
                if (plrEntity.getLocation().distanceSquared(location) < 2116) {
                    org.bukkit.SoundCategory sndCat = SoundCategory.PLAYERS;
                    try {
                        SoundCategory.valueOf(setting.soundCategory.toString());
                    } catch (Exception err) {
                    }

                    plrEntity.playSound(location, setting.sound, sndCat, setting.volume, setting.pitch);
                }
            }
        }
    }

    @Override
    public void AddToComposter(NPC npc, Location composter) {
        ItemStack[] npcInventory = npc.getTrait(Inventory.class).getContents();
        Levelled compBlock = (Levelled) composter.getBlock().getBlockData();
        Random rnd = new Random(composter.getWorld().getTime());

        for (int npcSlot = 0; npcSlot < npcInventory.length; npcSlot++) {
            if (npcInventory[npcSlot] == null)
                continue;
            if (npcSlot < 2 || (npcSlot > 35 && npcSlot < 41))
                continue;

            Float amount = GetComposterValue(npcInventory[npcSlot]);
            if (amount > 0) {
                while (npcInventory[npcSlot].getAmount() > 0) {
                    if (rnd.nextFloat() <= amount) {
                        npcInventory[npcSlot].setAmount(npcInventory[npcSlot].getAmount());
                        if (compBlock.getLevel() >= compBlock.getMaximumLevel()) {
                            for (int emptySlot = 0; emptySlot < npcInventory.length; emptySlot++) {
                                if (emptySlot < 2 || (emptySlot > 35 && emptySlot < 41))
                                    continue;
                                if (npcInventory[emptySlot] == null || npcInventory[emptySlot].getType() == Material.AIR) {
                                    npcInventory[emptySlot] = new ItemStack(Material.BONE_MEAL);
                                    npcInventory[emptySlot].setAmount(1);
                                    break;
                                } else if (npcInventory[emptySlot].getType() == Material.BONE_MEAL && npcInventory[emptySlot].getAmount() < npcInventory[emptySlot].getMaxStackSize()) {
                                    npcInventory[emptySlot].setAmount(npcInventory[emptySlot].getAmount() + 1);
                                    break;
                                }
                            }
                            compBlock.setLevel(0);
                        } else {
                            compBlock.setLevel(compBlock.getLevel() + 1);
                        }
                    }
                    if ((npcInventory[npcSlot].getAmount() - 1) < 0) {
                        npcInventory[npcSlot] = null;
                    } else {
                        npcInventory[npcSlot].setAmount(npcInventory[npcSlot].getAmount() - 1);
                    }
                }

                net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), composter);
                net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());

                composter.getBlock().setBlockData(compBlock);
                npc.getTrait(Inventory.class).setContents(npcInventory);
                return;
            }
        }
    }

    @Override
    public ItemStack[] GetBlockInventory(Block block) {
        switch (block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
            case ENDER_CHEST:
                return ((Chest) block.getState()).getBlockInventory().getContents();
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                return ((ShulkerBox) block.getState()).getInventory().getContents();
            case BARREL:
                return ((Barrel) block.getState()).getInventory().getContents();
            default:
                return null;
        }
    }

    @Override
    public void SetBlockInventory(Block block, ItemStack[] inventory) {
        switch (block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
            case ENDER_CHEST:
                ((Chest) block.getState()).getBlockInventory().setContents(inventory);
                break;
            case SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
                ((ShulkerBox) block.getState()).getInventory().setContents(inventory);
                break;
            case BARREL:
                ((Barrel) block.getState()).getInventory().setContents(inventory);
                break;
        }
    }

    @Override
    public void SetPassenger(org.bukkit.entity.Entity vehicle, org.bukkit.entity.Entity passenger) {
        vehicle.addPassenger(passenger);
    }

    @Override
    public FacingDirection getBlockDirection(Block block) {
        if (block.getBlockData() instanceof Directional) {
            switch (((Directional) block.getBlockData()).getFacing()) {
                case NORTH:
                    return FacingDirection.NORTH;
                case EAST:
                    return FacingDirection.EAST;
                case SOUTH:
                    return FacingDirection.SOUTH;
                case WEST:
                    return FacingDirection.WEST;
                case NORTH_EAST:
                    return FacingDirection.NORTH_EAST;
                case NORTH_WEST:
                    return FacingDirection.NORTH_WEST;
                case SOUTH_EAST:
                    return FacingDirection.SOUTH_EAST;
                case SOUTH_WEST:
                    return FacingDirection.SOUTH_WEST;
            }
        }
        return null;
    }

    private Float GetComposterValue(ItemStack item) {
        Float itemValue = 0.0F;
        switch (item.getType()) {
            //30%
            case BEETROOT_SEEDS:
            case DRIED_KELP:
            case GRASS:
            case ACACIA_LEAVES:
            case BIRCH_LEAVES:
            case DARK_OAK_LEAVES:
            case JUNGLE_LEAVES:
            case OAK_LEAVES:
            case SPRUCE_LEAVES:
            case MELON_SEEDS:
            case NETHER_WART:
            case PUMPKIN_SEEDS:
            case SPRUCE_SAPLING:
            case ACACIA_SAPLING:
            case BAMBOO_SAPLING:
            case BIRCH_SAPLING:
            case DARK_OAK_SAPLING:
            case JUNGLE_SAPLING:
            case OAK_SAPLING:
            case SEAGRASS:
            case SWEET_BERRIES:
            case WHEAT_SEEDS:
                itemValue = item.getAmount() * 0.3F;
                break;

            //50%
            case CACTUS:
            case DRIED_KELP_BLOCK:
            case MELON_SLICE:
            case SUGAR_CANE:
            case TALL_GRASS:
            case TWISTING_VINES:
            case WEEPING_VINES:
            case VINE:
            case NETHER_SPROUTS:
                itemValue = item.getAmount() * 0.5F;
                break;

            //65%
            case APPLE:
            case BEETROOT:
            case CARROT:
            case COCOA_BEANS:
            case FERN:
            case LARGE_FERN:
            case CHORUS_FLOWER:
            case CORNFLOWER:
            case SUNFLOWER:
            case LILY_PAD:
            case MELON:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
            case POTATO:
            case POTATOES:
            case PUMPKIN:
            case SEA_PICKLE:
            case WHEAT:
            case CRIMSON_FUNGUS:
            case WARPED_FUNGUS:
            case CRIMSON_ROOTS:
            case WARPED_ROOTS:
                itemValue = item.getAmount() * 0.65F;
                break;

            //85%
            case BAKED_POTATO:
            case BREAD:
            case COOKIE:
            case HAY_BLOCK:
            case BROWN_MUSHROOM_BLOCK:
            case RED_MUSHROOM_BLOCK:
            case NETHER_WART_BLOCK:
            case WARPED_WART_BLOCK:
                itemValue = item.getAmount() * 0.85F;
                break;

            //100%
            case CAKE:
            case PUMPKIN_PIE:
                itemValue = item.getAmount() * 1F;
                break;
        }

        return itemValue;
    }

    // Code borrowed from @SethBling :)
    private Vector calculateVelocity(Vector from, Vector to, int heightGain) {
        // Block locations
        int endGain = to.getBlockY() - from.getBlockY();
        double horizDist = Math.sqrt(distanceSquared(from, to));

        // Height gain
        int gain = heightGain;

        double maxGain = gain > (endGain + gain) ? gain : (endGain + gain);

        // Solve quadratic equation for velocity
        double a = -horizDist * horizDist / (4 * maxGain);
        double b = horizDist;
        double c = -endGain;

        double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);

        // Vertical velocity
        double vy = Math.sqrt(maxGain * hookGravity);

        // Horizontal velocity
        double vh = vy / slope;

        // Calculate horizontal direction
        double dx = (to.getBlockX() - from.getBlockX()) + 0.5;
        double dz = (to.getBlockZ() - from.getBlockZ()) + 0.5;
        double mag = Math.sqrt(dx * dx + dz * dz);
        double dirx = dx / mag;
        double dirz = dz / mag;

        // Horizontal velocity components
        double vx = vh * dirx;
        double vz = vh * dirz;

        return new Vector(vx, vy, vz);
    }

    private double distanceSquared(Vector from, Vector to) {
        double dx = to.getBlockX() - from.getBlockX();
        double dz = to.getBlockZ() - from.getBlockZ();

        return dx * dx + dz * dz;
    }

}
