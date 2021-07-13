package net.livecar.nuttyworks.destinations_animations.plugin;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.livecar.nuttyworks.destinations_animations.Destinations_Animations;
import net.livecar.nuttyworks.destinations_animations.storage.Sitting_NPC;
import net.livecar.nuttyworks.destinations_animations.storage.SleepPacket_Settings;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;

public class Processing {
    private HashMap<UUID, SleepPacket_Settings> sleepPlayed;
    private HashMap<UUID, Sitting_NPC> sittingNPC;
    private Destinations_Animations pluginReference;
    
    public Processing(Destinations_Animations instanceRef) {
        pluginReference = instanceRef;
        sleepPlayed = new HashMap<>();
        sittingNPC = new HashMap<>();
    }
    
    public void onPluginTick() {
        for (Entry<Integer, UUID> entry : pluginReference.monitoredNPC.entrySet()) {
            NPC npc = pluginReference.getCitizensPlugin.getNPCRegistry().getById(entry.getKey());
            if (npc == null || !npc.isSpawned())
                continue;
            
            Animations_Settings animationSettings = pluginReference.npcSettings.get(npc.getId());
            
            if (animationSettings == null || animationSettings.destinationsTrait == null || animationSettings.destinationsTrait.currentLocation == null)
                continue;
            
            if (!animationSettings.destinationsTrait.currentLocation.LocationIdent.equals(entry.getValue()))
                continue;
            
            if (animationSettings.destinationsTrait.currentLocation.destination.distanceSquared(npc.getEntity().getLocation()) > 4) {
                // Need to get the NPC back to it's location
                if (animationSettings.lastAction == null || (animationSettings.lastAction.getTime() + 10000 < new Date().getTime() && !npc.getNavigator().isNavigating())) {
                    animationSettings.lastAction = new Date();
                    DestinationsPlugin.Instance.getPathClass.addToQueue(npc, animationSettings.destinationsTrait, npc.getEntity().getLocation().add(0.0D, -1.0D, 0.0D), animationSettings.destinationsTrait.currentLocation.destination, 120,
                            new ArrayList<Material>(), 0, true, true, true, "DestinationsAnimations");
                }
                continue;
            }
            
            switch (animationSettings.locations.get(entry.getValue()).action) {
                case FISH:
                    FishingCycle(npc, animationSettings, false);
                    continue;
                case FISH_ADD:
                    FishingCycle(npc, animationSettings, true);
                    continue;
                case NONE:
                    continue;
                case SLEEP:
                    this.SleepCycle(npc, animationSettings);
                    continue;
                case SIT:
                    sitNPC(npc, animationSettings);
                    continue;
                case ARMSBOUNCE:
                    armsBounce(npc,animationSettings,animationSettings.locations.get(entry.getValue()));
                    break;
                case SWING:
                    swingNPC(npc, animationSettings, animationSettings.locations.get(entry.getValue()));
                    continue;
                case COMPOST:
                    compostInventory(npc, animationSettings);
                    continue;
                default:
                    continue;
            }
        }
    }
    
    public void armsBounce(NPC npc, Animations_Settings animationSettings, Animations_Location curLoc) {
        if ((curLoc.lastAction.getTime() + curLoc.interval < new Date().getTime())) {
            org.bukkit.util.Vector entV = npc.getEntity().getVelocity();
            entV.setY(0.3);
            npc.getEntity().setVelocity(entV);
            net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
        }
    }
    
    public void swingNPC(NPC npc, Animations_Settings animationSettings, Animations_Location curLoc) {
        
        if ((curLoc.lastAction.getTime() + curLoc.interval < new Date().getTime())) {
            curLoc.lastAction = new Date();
            net.citizensnpcs.util.Util.assumePose(npc.getEntity(), animationSettings.destinationsTrait.currentLocation.destination.clone().getYaw(), animationSettings.destinationsTrait.currentLocation.destination.clone().getPitch());
            net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
            if (curLoc.sound != null) {
                pluginReference.getNMSBridge.PlaySound(npc.getEntity().getLocation(), curLoc);
            }
        }
    }
    
    public void sitNPC(NPC npc, Animations_Settings animationSettings) {
        if (sittingNPC.containsKey(npc.getEntity().getUniqueId()))
            return;
    
        Location sitLocation = animationSettings.destinationsTrait.currentLocation.destination.clone();
        Location facingLocation = sitLocation;
        FacingDirection facing = FacingDirection.YawToDirection(animationSettings.destinationsTrait.currentLocation.destination.getYaw(),true);
    
        if (sitLocation.getBlock().getType() == Material.AIR) {
            sitLocation.add(0.0D, -1.0D, 0.0D);
        }
    
        if (sitLocation.getBlock().getType().toString().toLowerCase().contains("carpet")) {
            sitLocation.add(0.0D, -1.0D, 0.0D);
        }
    
        //Fix for issue #9
        if (sitLocation.getBlock().getType().toString().toLowerCase().contains("_plate")) {
            sitLocation.add(0.0D, -1.0D, 0.0D);
        }
        
        sitLocation.setPitch(0);
    
        if (sitLocation.getBlock().getType().toString().toLowerCase().contains("stairs")) {
            //Figure out the stairs placement and match the NPC to it.
            switch (pluginReference.getNMSBridge.getBlockDirection(sitLocation.getBlock())) {
                case NORTH:
                    sitLocation.add(0.0D, -0.5D, 0.15D);
                    facing = FacingDirection.SOUTH;
                    break;
                case EAST:
                    sitLocation.add(-0.15D, -0.5D, 0.0);
                    facing = FacingDirection.WEST;
                    break;
                case SOUTH:
                    sitLocation.add(0.0D, -0.5D, -0.15D);
                    facing = FacingDirection.NORTH;
                    break;
                case WEST:
                    sitLocation.add(0.15D, -0.5D, 0.0D);
                    facing = FacingDirection.EAST;
                    break;
                default:
                    sitLocation.add(0.0D, -0.5D, 0.0D);
                    break;
            }
        }
    
        switch (facing)
        {
            case NORTH:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(0.0D, 0.5, -1.0D);
                sitLocation.setYaw(180);
                break;
            case NORTH_EAST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(1.0D, 0, -1.0D);
                sitLocation.setYaw(45);
                break;
            case EAST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(1.0D, 0, 0.0D);
                sitLocation.setYaw(90);
                break;
            case SOUTH_EAST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(1.0D, 0, 1.0D);
                sitLocation.setYaw(45);
                break;
            case SOUTH:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(0.0D, 0, 1.0D);
                sitLocation.setYaw(0);
                break;
            case SOUTH_WEST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(-1.0D, 0, 1.0D);
                sitLocation.setYaw(45);
                break;
            case WEST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(-1.0D, 0, 0.0D);
                sitLocation.setYaw(270);
                break;
            case NORTH_WEST:
                facingLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(-1.0D, 0, -1.0D);
                sitLocation.setYaw(180);
                break;

        }
    
        sitLocation.setDirection(facingLocation.toVector().clone().subtract(sitLocation.toVector()));
    
        if (DestinationsPlugin.Instance.getMCUtils.isHalfBlock(sitLocation.getBlock().getType())) {
            switch (DestinationsPlugin.Instance.getMCUtils.getSlabType(sitLocation.getBlock())) {
                case TOP:
                    break;
                case BOTTOM:
                    sitLocation.add(0.0D, -0.5D, 0.0D);
                    break;
                case DOUBLE:
                    break;
            }
        }
    
        npc.teleport(sitLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
    
        final Location finalFaceLocation = facingLocation.clone().add(0,-1.0,0);
        ArmorStand sitStand = npc.getEntity().getWorld().spawn(sitLocation, ArmorStand.class);
    
        Sitting_NPC sitNPC = new Sitting_NPC();
        sitNPC.npc = npc;
        sitNPC.originalLocation = npc.getEntity().getLocation();
        sitNPC.attachedArmorStand = sitStand;
    
        sitStand.setVisible(false);
        sitStand.setGravity(false);
        sitStand.setArms(false);
        sitStand.setBasePlate(false);
        sitStand.setSmall(true);
        sitStand.setMarker(false);
    
        new BukkitRunnable() {
            @Override
            public void run() {
                pluginReference.getNMSBridge.SetPassenger(sitStand, npc.getEntity());
                sitNPC.StartSitting();
                
            }
        }.runTaskLater(DestinationsPlugin.Instance, 1L);
        new BukkitRunnable() {
            @Override
            public void run() {
                net.citizensnpcs.util.Util.faceLocation(npc.getEntity(),finalFaceLocation);
            
            }
        }.runTaskLater(DestinationsPlugin.Instance, 2L);
        
        sittingNPC.put(npc.getEntity().getUniqueId(), sitNPC);
    }
    
    public void SleepCycle(NPC npc, Animations_Settings animationSettings) {
        if (!npc.isSpawned())
            return;
        
        if (animationSettings.destinationsTrait.getPendingDestinations() != null && animationSettings.destinationsTrait.getPendingDestinations().size() > 0)
            return;
        
        if (animationSettings.destinationsTrait.currentLocation == null)
            return;
        
        if (animationSettings.destinationsTrait.currentLocation.destination.distance(npc.getEntity().getLocation()) > 2)
            return;
        
        for (Player plrEntity : Bukkit.getOnlinePlayers()) {
            
            Location plrLocation = plrEntity.getLocation().clone();
            plrLocation.setY(0.0);
            Location destLocation = animationSettings.destinationsTrait.currentLocation.destination.clone();
            destLocation.setY(0.0);
            
            if (!sleepPlayed.containsKey(plrEntity.getUniqueId())) {
                SleepPacket_Settings plrPacket = new SleepPacket_Settings();
                plrPacket.playerID = plrEntity.getUniqueId();
                plrPacket.playedNPCS = new ArrayList<String>();
                sleepPlayed.put(plrEntity.getUniqueId(), plrPacket);
            }
            
            if ((plrEntity.getWorld() == npc.getEntity().getWorld()) && (plrLocation.distanceSquared(destLocation) < pluginReference.entityRadius) && !sleepPlayed.get(plrEntity.getUniqueId()).containsNPC(npc)) {
                animationSettings.isSleeping = true;
                sleepPlayed.get(plrEntity.getUniqueId()).addNPC(npc);
                pluginReference.getNMSBridge.SleepNPC(npc, plrEntity, animationSettings.destinationsTrait.currentLocation.destination.getYaw());
            } else if (plrEntity.getWorld().equals(npc.getEntity().getWorld()) && (plrLocation.distanceSquared(destLocation) >= pluginReference.entityRadius)) {
                // Remove the player from the list of packets being sent, so it
                // can resend if they come back
                if (sleepPlayed.containsKey(plrEntity.getUniqueId())) {
                    sleepPlayed.get(plrEntity.getUniqueId()).removeNPC(npc);
                }
            }
        }
        animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 250l);
    }
    
    public void FishingCycle(NPC npc, Animations_Settings animationSettings, boolean stockFish) {
        Random rnd = new Random(new Date().getTime());
        
        if (animationSettings.fishHook != null) {
            if ((animationSettings.hookDestination.getY() - animationSettings.fishHook.getLocation().getY()) > 0.2D && ((animationSettings.castTime.getTime() + 2500) < new Date().getTime())) {
                
                //Get version specific drops
                InventoryModel inv = new InventoryModel();
                inv.srcInventory = pluginReference.getNMSBridge.getFishDrops();
                inv.dstInventory = npc.getTrait(Inventory.class).getContents();
                inv.dstBlockedSlots = Arrays.asList(new Integer[]{0, 1, 36, 37, 38, 39, 40});
                mergeInventory(inv);
                npc.getTrait(Inventory.class).setContents(inv.dstInventory);
                
                animationSettings.fishHook.remove();
                animationSettings.fishHook = null;
                animationSettings.castTime = null;
                animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 1000l);
                return;
            }
        }
        
        if (animationSettings.nextAnimationTime.getTime() > new Date().getTime())
            return;
        
        boolean pausePlayers = false;
        
        // Check the area near the NPC for players. Pause if so
        for (Player plrEntity : Bukkit.getOnlinePlayers()) {
            if ((plrEntity.getWorld() == npc.getEntity().getWorld()) && (plrEntity.getLocation().distance(npc.getEntity().getLocation()) < 8)) {
                pausePlayers = true;
            }
        }
        
        animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 500l);
        
        if (animationSettings.fishHook != null) {
            // Pull the line in
            animationSettings.fishHook.remove();
            animationSettings.fishHook = null;
            animationSettings.castTime = null;
            animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 1000l);
            Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Hook Removed");
        } else if (!pausePlayers) {
            // Locate water
            int counter = 0;
            while (counter < 50) {
                Location source = new Location(npc.getEntity().getLocation().getWorld(), npc.getEntity().getLocation().getBlockX() + (16 - rnd.nextInt(32)), npc.getEntity().getLocation().getBlockY(), npc.getEntity().getLocation()
                        .getBlockZ() + (16 - rnd.nextInt(32)));
                for (byte y = -5; y <= 1; y++) {
                    Location checkLocation = source.clone().add(0.5, y, 0.5);
                    if (checkLocation.distanceSquared(npc.getEntity().getLocation()) > 8 && checkLocation.clone().add(0, -1, 0).getBlock().getType() == pluginReference.getNMSBridge.getWaterBlock() && checkLocation.clone().getBlock()
                            .getType() == Material.AIR) {
                        net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), checkLocation);
                        
                        try {
                            animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 8000l + (1000l * rnd.nextInt(20)));
                            net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                            animationSettings.hookDestination = checkLocation;
                            Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.onPluginTick()|Hook Cast(" + checkLocation + ")");
                            animationSettings.castTime = new Date();
                            animationSettings.fishHook = pluginReference.getNMSBridge.CastFishingLine(checkLocation, npc, 1);
                            return;
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
                counter++;
            }
        }
        animationSettings.nextAnimationTime = new Date(System.currentTimeMillis() + 1000l);
    }
    
    private void mergeInventory(InventoryModel inventories) {
        for (int srcSlot = 0; srcSlot < inventories.srcInventory.length; srcSlot++) {
            ItemStack item = inventories.srcInventory[srcSlot];
            if (item == null)
                continue;
            if (inventories.srcBlockedSlots.contains(srcSlot))
                continue;
            
            int emptySlot = -1;
            for (int destSlot = 0; destSlot < inventories.dstInventory.length; destSlot++) {
                
                if (inventories.dstBlockedSlots.contains(destSlot))
                    continue;
                
                if (inventories.dstInventory[destSlot] == null && emptySlot == -1) {
                    emptySlot = destSlot;
                    continue;
                } else if (inventories.dstInventory[destSlot] != null && inventories.dstInventory[destSlot].getType() == Material.AIR && emptySlot == -1) {
                    emptySlot = destSlot;
                    continue;
                }
                
                if (inventories.dstInventory[destSlot] != null) {
                    if (inventories.dstInventory[destSlot].getType() == item.getType() && inventories.dstInventory[destSlot].getDurability() == item.getDurability() && inventories.dstInventory[destSlot].getAmount() < inventories.dstInventory[destSlot].getType().getMaxStackSize()) {
                        if ((inventories.dstInventory[destSlot].getAmount() + item.getAmount()) > (inventories.dstInventory[destSlot].getType().getMaxStackSize())) {
                            int leftOver = Math.abs(inventories.srcInventory[srcSlot].getType().getMaxStackSize() - (inventories.dstInventory[destSlot].getAmount() + item.getAmount()));
                            inventories.dstInventory[destSlot].setAmount(item.getAmount() - leftOver);
                            item.setAmount(item.getAmount() - (item.getAmount() - leftOver));
                        } else {
                            inventories.dstInventory[destSlot].setAmount(inventories.dstInventory[destSlot].getAmount() + item.getAmount());
                            inventories.srcInventory[srcSlot].setAmount(0);
                            break;
                        }
                    }
                }
            }
            
            if (emptySlot != -1 && (inventories.dstInventory[emptySlot] == null || inventories.dstInventory[emptySlot].getType() == Material.AIR) && item != null) {
                inventories.dstInventory[emptySlot] = item.clone();
                emptySlot = -1;
                inventories.srcInventory[srcSlot].setAmount(0);
                continue;
            }
        }
    }
    
    public void compostInventory(NPC npc, Animations_Settings animationSettings) {
        int xAxis = 0;
        int zAxis = 0;
        
        double rotation = animationSettings.destinationsTrait.currentLocation.destination.getYaw();
        
        // North: -Z
        // East: +X
        // South: +Z
        // West: -X
        
        if (rotation < 30.0) {
            xAxis = 0;
            zAxis = 1;
        } else if (rotation < 60) {
            xAxis = -1;
            zAxis = 1;
        } else if (rotation < 120) {
            xAxis = -1;
            zAxis = 0;
        } else if (rotation < 150) {
            xAxis = -1;
            zAxis = -1;
        } else if (rotation < 210) {
            xAxis = 0;
            zAxis = -1;
        } else if (rotation < 240) {
            xAxis = 1;
            zAxis = -1;
        } else if (rotation < 300) {
            xAxis = 1;
            zAxis = 0;
        } else if (rotation < 330) {
            xAxis = 1;
            zAxis = 1;
        } else {
            xAxis = 0;
            zAxis = 1;
        }
        
        // Check if we have a chest at the location
        for (byte y = -1; y <= 1; y++) {
            final Location chestLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(xAxis, y, zAxis);
            if (chestLocation.getBlock().getType().toString().toLowerCase().contains("composter")) {
                pluginReference.getNMSBridge.AddToComposter(npc, chestLocation);
            }
        }
    }
    
    public void openChest(NPC npc, Animations_Settings animationSettings, Animations_Settings.enAction action) {
        int xAxis = 0;
        int zAxis = 0;
        
        double rotation = animationSettings.destinationsTrait.currentLocation.destination.getYaw();
        
        // North: -Z
        // East: +X
        // South: +Z
        // West: -X
        
        if (rotation < 30.0) {
            xAxis = 0;
            zAxis = 1;
        } else if (rotation < 60) {
            xAxis = -1;
            zAxis = 1;
        } else if (rotation < 120) {
            xAxis = -1;
            zAxis = 0;
        } else if (rotation < 150) {
            xAxis = -1;
            zAxis = -1;
        } else if (rotation < 210) {
            xAxis = 0;
            zAxis = -1;
        } else if (rotation < 240) {
            xAxis = 1;
            zAxis = -1;
        } else if (rotation < 300) {
            xAxis = 1;
            zAxis = 0;
        } else if (rotation < 330) {
            xAxis = 1;
            zAxis = 1;
        } else {
            xAxis = 0;
            zAxis = 1;
        }
        
        // Check if we have a chest at the location
        for (byte y = -1; y <= 1; y++) {
            final Location chestLocation = animationSettings.destinationsTrait.currentLocation.destination.clone().add(xAxis, y, zAxis);
            
            if (chestLocation.getBlock().getType() == Material.TRAPPED_CHEST ||
                    chestLocation.getBlock().getType() == Material.CHEST ||
                    chestLocation.getBlock().getType() == Material.ENDER_CHEST ||
                    chestLocation.getBlock().getType().toString().toLowerCase().contains("shulker_box") ||
                    chestLocation.getBlock().getType().toString().toLowerCase().equals("barrel")) {
                net.citizensnpcs.util.Util.faceLocation(npc.getEntity(), chestLocation);
                net.citizensnpcs.util.PlayerAnimation.ARM_SWING.play((Player) npc.getEntity());
                
                // Found out chest, lets open it for a few moments
                Random rndEngine = new Random(new Date().getTime());
                pluginReference.getNMSBridge.changeChestState(chestLocation, true);
                
                InventoryModel inv = new InventoryModel();
                
                switch (action) {
                    case CHEST_FILL:
                        inv.dstInventory = pluginReference.getNMSBridge.GetBlockInventory(chestLocation.getBlock());
                        inv.srcInventory = npc.getTrait(Inventory.class).getContents();
                        inv.srcBlockedSlots = Arrays.asList(new Integer[]{0, 1, 36, 37, 38, 39, 40});
                        mergeInventory(inv);
                        pluginReference.getNMSBridge.SetBlockInventory(chestLocation.getBlock(), inv.dstInventory);
                        npc.getTrait(Inventory.class).setContents(inv.srcInventory);
                        break;
                    case CHEST_EMPTY:
                        inv.srcInventory = pluginReference.getNMSBridge.GetBlockInventory(chestLocation.getBlock());
                        inv.dstInventory = npc.getTrait(Inventory.class).getContents();
                        inv.dstBlockedSlots = Arrays.asList(new Integer[]{0, 1, 36, 37, 38, 39, 40});
                        mergeInventory(inv);
                        pluginReference.getNMSBridge.SetBlockInventory(chestLocation.getBlock(), inv.srcInventory);
                        npc.getTrait(Inventory.class).setContents(inv.dstInventory);
                        break;
                }
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        pluginReference.getNMSBridge.changeChestState(chestLocation, false);
                    }
                }.runTaskLater(DestinationsPlugin.Instance, 20L + rndEngine.nextInt(60));
                return;
            }
        }
    }
    
    public void CleanupAnimations() {
        for (Sitting_NPC satNPC : this.sittingNPC.values()) {
            this.undoAnimations(satNPC.npc);
        }
    }
    
    public void undoAnimations(NPC npc) {
        Animations_Settings animationSettings = pluginReference.npcSettings.get(npc.getId());
        
        if (animationSettings == null)
            return;
        
        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.FINE, "Animations_Processing.undoAnimations()");
        
        if (animationSettings.isSleeping) {
            for (Player plrEntity : Bukkit.getOnlinePlayers()) {
                pluginReference.getNMSBridge.unsleepNPC(npc);
                if (this.sleepPlayed.containsKey(plrEntity.getUniqueId())) {
                    this.sleepPlayed.get(plrEntity.getUniqueId()).removeNPC(npc);
                }
            }
            animationSettings.isSleeping = false;
        }
        
        if (sittingNPC.containsKey(npc.getEntity().getUniqueId())) {
            sittingNPC.get(npc.getEntity().getUniqueId()).StopSitting();
            this.sittingNPC.remove(npc.getEntity().getUniqueId());
        }
    }
}
