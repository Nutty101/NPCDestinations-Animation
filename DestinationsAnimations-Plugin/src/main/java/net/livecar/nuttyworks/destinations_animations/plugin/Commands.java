package net.livecar.nuttyworks.destinations_animations.plugin;

import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Location.enSoundCategory;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Settings.enAction;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.listeners.commands.CommandInfo;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.util.logging.Level;

public class Commands {
    @CommandInfo(
            name = "locanimate",
            group = "External Plugin Commands",
            languageFile = "animations",
            helpMessage = "command_locanimate_help",
            arguments = {"#", "<animation>", "#", "<sound>", "volume", "pitch", "<soundcategory>"},
            permission = {"npcdestinations.editall.locanimate", "npcdestinations.editown.locanimate"},
            allowConsole = true,
            minArguments = 2,
            maxArguments = 7)
    public boolean npcDest_locAnimation(DestinationsPlugin destRef, CommandSender sender, NPC npc, String[] inargs, boolean isOwner, NPCDestinationsTrait destTrait) {

        if (inargs.length < 2) {
            destRef.getMessageManager.sendMessage("animations", sender, "messages.command_badargs");
            return true;
        }

        int nIndex = Integer.parseInt(inargs[1]);
        if (nIndex > destTrait.NPCLocations.size() - 1) {
            destRef.getMessageManager.sendMessage("animations", sender, "messages.command_badargs");
            return true;
        }

        Destinations_Plugin addonReference = (Destinations_Plugin) destRef.getPluginManager.getPluginByName("Animations");
        Destination_Setting destSetting = destTrait.NPCLocations.get(nIndex);
        Animations_Settings animSetting = null;

        if (!addonReference.pluginReference.npcSettings.containsKey(npc.getId())) {
            animSetting = new Animations_Settings();
            animSetting.setNPC(npc.getId());
            addonReference.pluginReference.npcSettings.put(npc.getId(), animSetting);
        } else {
            animSetting = addonReference.pluginReference.npcSettings.get(npc.getId());
        }

        if (inargs.length == 2) {
            // Remove the settings, and detach this from the location.
            if (addonReference.pluginReference.monitoredNPC.containsKey(npc.getId())) {
                if (addonReference.pluginReference.monitoredNPC.get(npc.getId()).equals(destSetting.LocationIdent)) {
                    addonReference.pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Animations_Plugin.onNavigationNewDestination|NPC:" + npc.getId()
                            + "|New Location,clearing monitors and releasing control.");
                    destTrait.unsetMonitoringPlugin();
                    addonReference.pluginReference.npcSettings.get(npc.getId()).currentDestination = null;
                    addonReference.pluginReference.monitoredNPC.remove(npc.getId());
                }
            }
            if (animSetting.locations.containsKey(destSetting.LocationIdent)) {
                animSetting.locations.remove(destSetting.LocationIdent);
                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_removed", destTrait, destSetting);
            }
            return true;
        }

        if (inargs[2].equalsIgnoreCase("clear")) {
            animSetting.locations.remove(destSetting.LocationIdent);
            addonReference.pluginReference.monitoredNPC.remove(npc.getId());
            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_removed", destTrait, destSetting, Material.AIR);
            return true;
        }

        enAction animation;
        if (enAction.valueOf(inargs[2].toUpperCase()) != null)
            animation = enAction.valueOf(inargs[2].toUpperCase());
        else {
            addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_badargs", destTrait, destSetting);
            return true;
        }

        switch (animation) {
            case ARMSBOUNCE:
            case SWING:
                // [index] swing <interval> <sound> <volume> <pitch> <category>
                // [locanimate, 1, swing, 500]
                animSetting.locations.remove(destSetting.LocationIdent);

                Animations_Location locArg = new Animations_Location();

                locArg.action = animation;

                //Interval
                if (inargs.length > 3 && addonReference.pluginReference.getDestinationsPlugin.getUtilitiesClass.isNumeric(inargs[3]))
                    locArg.interval = Integer.parseInt(inargs[3]);

                //Sound name
                if (inargs.length > 4) {
                    //apache uses try/catch. Faster to just loop this enum as it's small
                    for (Sound snd : Sound.values()) {
                        if (snd.name().equals(inargs[4].toUpperCase())) {
                            locArg.sound = snd;
                        }
                    }
                    if (locArg.sound == null) {
                        addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_badargs", destTrait, destSetting);
                        return true;
                    }
                }

                //volume
                if (inargs.length > 5 && addonReference.pluginReference.getDestinationsPlugin.getUtilitiesClass.isNumeric(inargs[5]))
                    locArg.volume = Float.parseFloat(inargs[5]);

                //pitch
                if (inargs.length > 6 && addonReference.pluginReference.getDestinationsPlugin.getUtilitiesClass.isNumeric(inargs[6]))
                    locArg.pitch = Float.parseFloat(inargs[6]);

                //Sound category
                if (inargs.length > 7) {
                    //apache uses try/catch. Faster to just loop this enum as it's small
                    for (enSoundCategory sndCat : enSoundCategory.values()) {
                        if (sndCat.name().equals(inargs[7].toUpperCase())) {
                            locArg.soundCategory = sndCat;
                        }
                    }
                    if (locArg.soundCategory == null) {
                        addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_badargs", destTrait, destSetting);
                        return true;
                    }
                }

                animSetting.locations.put(destSetting.LocationIdent, locArg);

                addonReference.pluginReference.monitoredNPC.remove(npc.getId());

                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_added_active", destTrait, destSetting, Material.AIR);
                if (destSetting.LocationIdent.equals(destTrait.currentLocation.LocationIdent) && !addonReference.pluginReference.monitoredNPC.containsKey(npc.getId()))
                    addonReference.pluginReference.monitoredNPC.put(npc.getId(), destSetting.LocationIdent);
                return true;
            case CHEST:
            case CHEST_FILL:
            case CHEST_EMPTY:
            case FISH:
            case FISH_ADD:
            case SIT:
            case COMPOST:
            case SLEEP:
                animSetting.locations.remove(destSetting.LocationIdent);

                Animations_Location newLocSetting = new Animations_Location();

                newLocSetting.action = enAction.valueOf(inargs[2].toUpperCase());

                animSetting.locations.put(destSetting.LocationIdent, newLocSetting);

                addonReference.pluginReference.monitoredNPC.remove(npc.getId());

                addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_added_active", destTrait, destSetting, Material.AIR);
                if (destSetting.LocationIdent.equals(destTrait.currentLocation.LocationIdent) && !addonReference.pluginReference.monitoredNPC.containsKey(npc.getId()))
                    addonReference.pluginReference.monitoredNPC.put(npc.getId(), destSetting.LocationIdent);
                return true;
            default:
                break;

        }

        addonReference.pluginReference.getDestinationsPlugin.getMessageManager.sendMessage("animations", sender, "messages.command_badargs", destTrait, destSetting);
        return true;

    }

}
