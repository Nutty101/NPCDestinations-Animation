package net.livecar.nuttyworks.destinations_animations.plugin;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.livecar.nuttyworks.destinations_animations.Destinations_Animations;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Location.enSoundCategory;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Settings.enAction;
import net.livecar.nuttyworks.npc_destinations.api.Destination_Setting;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Destinations_Plugin extends DestinationsAddon {
    Destinations_Animations pluginReference = null;

    public Destinations_Plugin(Destinations_Animations instanceRef) {
        pluginReference = instanceRef;
    }

    @Override
    public String getPluginIcon() {
        return "♒";
    }

    @Override
    public String getActionName() {
        return "Animations";
    }

    @Override
    public String getQuickDescription() {
        String[] response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", "messages.plugin_description", "");
        return response[0];
    }

    @Override
    public String getDestinationHelp(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location) {
        String[] response = new String[0];

        try {

            if (!pluginReference.npcSettings.containsKey(npc.getId())) {
                response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", null, "messages.plugin_destination_generic", npcTrait, location, npc, null, 0);
                return response[0];
            } else {
                Animations_Location animSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent);
                if (animSetting == null) {
                    response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", null, "messages.plugin_destination_generic", npcTrait, location, npc, null, 0);
                    return response[0];
                }

                switch (animSetting.action) {
                    case SWING:
                        response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", null, "messages.plugin_destination_swing", npcTrait, location, npc, null, 0);
                        break;
                    case CHEST:
                    case CHEST_FILL:
                    case FISH:
                    case FISH_ADD:
                    case NONE:
                    case SIT:
                    case SLEEP:
                    default:
                        response = pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", null, "messages.plugin_destination_generic", npcTrait, location, npc, null, 0);
                        break;
                }
            }
            return response[0];
        } catch (Exception err) {
            err.printStackTrace();
            return response[0];
        }
    }

    @Override
    public List<String> parseTabItem(String item, String priorArg) {
        List<String> results = new ArrayList<String>();

        if (item.equalsIgnoreCase("<animation>")) {
            for (enAction action : enAction.values()) {
                results.add(action.name());
            }
        }

        if (item.equalsIgnoreCase("<sound>")) {
            for (Sound sound : Sound.values()) {
                results.add(sound.name());
            }
        }

        if (item.equalsIgnoreCase("<soundcategory>")) {
            for (enSoundCategory soundCat : enSoundCategory.values()) {
                results.add(soundCat.name());
            }
        }

        return results;
    }

    @Override
    public String parseLanguageLine(String message, NPCDestinationsTrait npcTrait, Destination_Setting locationSetting, Material blockMaterial, NPC npc, int ident) {
        if (locationSetting != null) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(locationSetting.LocationIdent)) {
                    if (message.toLowerCase().contains("<animations.setting>")) {
                        enAction npcSetting = pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).action;
                        message = message.replaceAll("<animations\\.setting>", pluginReference.getDestinationsPlugin.getMessageManager.buildMessage("animations", "result_messages." + npcSetting, "")[0]);
                    }
                    if (message.toLowerCase().contains("<animations.interval>")) {
                        message = message.replaceAll("<animations\\.interval>", Integer.toString(pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).interval));
                    }
                    if (message.toLowerCase().contains("<animations.sound>")) {
                        if (pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).sound == null)
                            message = message.replaceAll("<animations\\.sound>", "");
                        else
                            message = message.replaceAll("<animations\\.sound>", pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).sound.toString());
                    }
                    if (message.toLowerCase().contains("<animations.volume>")) {
                        message = message.replaceAll("<animations\\.volume>", Float.toString(pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).volume));
                    }
                    if (message.toLowerCase().contains("<animations.pitch>")) {
                        message = message.replaceAll("<animations\\.pitch>", Float.toString(pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).pitch));
                    }
                    if (message.toLowerCase().contains("<animations.category>")) {
                        if (pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).soundCategory == null)
                            message = message.replaceAll("<animations\\.category>", "");
                        else
                            message = message.replaceAll("<animations\\.category>", pluginReference.npcSettings.get(npc.getId()).locations.get(locationSetting.LocationIdent).soundCategory.toString());
                    }
                }
            }
        }

        if (message.toLowerCase().contains("<animations.setting>"))
            message = message.replaceAll("<animations\\.setting>", "Not Set");

        return message;
    }

    @Override
    public void onLocationLoading(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!storageKey.keyExists("Animations"))
            return;

        Animations_Settings npcAnimation;
        if (!pluginReference.npcSettings.containsKey(npc.getId())) {
            npcAnimation = new Animations_Settings();
            npcAnimation.setNPC(npc.getId());
            pluginReference.npcSettings.put(npc.getId(), npcAnimation);
        } else {
            npcAnimation = pluginReference.npcSettings.get(npc.getId());
        }

        npcAnimation.locations.remove(location.LocationIdent);

        Animations_Location locSetting = new Animations_Location();

        if (enAction.valueOf(storageKey.getString("Animations.Setting", "")) != null)
            locSetting.action = enAction.valueOf(storageKey.getString("Animations.Setting", "NONE"));

        // Remove after a couple versions.
        if (!storageKey.getString("Animations.Variable1", "").equals("")) {
            locSetting.interval = Integer.parseInt(storageKey.getString("Animations.Variable1", ""));
        }

        if (!storageKey.getString("Animations.Interval", "").equals("")) {
            locSetting.interval = Integer.parseInt(storageKey.getString("Animations.Interval", ""));
        }

        if (!storageKey.getString("Animations.Sound", "").equals("")) {
            String soundStr = storageKey.getString("Animations.Sound", "");
            for (Sound snd : Sound.values()) {
                if (snd.name().equals(soundStr.toUpperCase())) {
                    locSetting.sound = snd;
                }
            }
        }

        if (!storageKey.getString("Animations.Category", "").equals("")) {
            String soundStr = storageKey.getString("Animations.Category", "");
            for (enSoundCategory sndCat : enSoundCategory.values()) {
                if (sndCat.name().equals(soundStr.toUpperCase())) {
                    locSetting.soundCategory = sndCat;
                }
            }
        }

        if (!storageKey.getString("Animations.Volume", "").equals("")) {
            locSetting.volume = Float.parseFloat(storageKey.getString("Animations.Volume", "1.0"));
        }

        if (!storageKey.getString("Animations.Pitch", "").equals("")) {
            locSetting.pitch = Float.parseFloat(storageKey.getString("Animations.pitch", "0.0"));
        }

        npcAnimation.locations.put(location.LocationIdent, locSetting);
    }

    @Override
    public void onLocationSaving(NPC npc, NPCDestinationsTrait npcTrait, Destination_Setting location, DataKey storageKey) {
        if (!pluginReference.npcSettings.containsKey(npc.getId()))
            return;
        if (!pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent))
            return;

        if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(location.LocationIdent)) {
            storageKey.setString("Animations.Setting", pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).action.toString());
            storageKey.setInt("Animations.Interval", pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).interval);
            storageKey.setString("Animations.Volume", Float.toString(pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).volume));
            storageKey.setString("Animations.Pitch", Float.toString(pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).pitch));
            if (pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).sound != null)
                storageKey.setString("Animations.Sound", pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).sound.toString());
            if (pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).soundCategory != null)
                storageKey.setString("Animations.Category", pluginReference.npcSettings.get(npc.getId()).locations.get(location.LocationIdent).soundCategory.toString());
        }
    }

    @Override
    public boolean onNavigationReached(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(npc.getId())) {
            if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(destination.LocationIdent)) {
                if (!pluginReference.monitoredNPC.containsKey(npc.getId())) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Animations_Plugin.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                    switch (pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent).action) {
                        case CHEST:
                            pluginReference.getProcessingClass.openChest(npc, pluginReference.npcSettings.get(npc.getId()), pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent).action);
                            break;
                        case CHEST_FILL:
                            pluginReference.getProcessingClass.openChest(npc, pluginReference.npcSettings.get(npc.getId()), pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent).action);
                            break;
                        case CHEST_EMPTY:
                            pluginReference.getProcessingClass.openChest(npc, pluginReference.npcSettings.get(npc.getId()), pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent).action);
                            break;
                        case COMPOST:
                        case FISH:
                        case FISH_ADD:
                        case SLEEP:
                        case SWING:
                        case ARMSBOUNCE:
                        case SIT:
                            // Event triggered
                            trait.setMonitoringPlugin(pluginReference.getPluginReference, destination);
                            pluginReference.monitoredNPC.put(npc.getId(), destination.LocationIdent);
                            break;
                        case NONE:

                        default:
                            break;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onNewDestination(NPC npc, NPCDestinationsTrait trait, Destination_Setting destination) {
        if (pluginReference.npcSettings.containsKey(npc.getId())) {
            if (((Animations_Settings) pluginReference.npcSettings.get(npc.getId())).locations.containsKey(trait.currentLocation.LocationIdent)) {
                if (pluginReference.npcSettings.get(Integer.valueOf(npc.getId())).fishHook != null) {
                    pluginReference.npcSettings.get(Integer.valueOf(npc.getId())).fishHook.remove();
                    pluginReference.npcSettings.get(Integer.valueOf(npc.getId())).fishHook = null;
                }

                pluginReference.getProcessingClass.undoAnimations(npc);

                if (pluginReference.monitoredNPC.containsKey(Integer.valueOf(npc.getId()))) {
                    pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Animations_Plugin.onNavigationNewDestination|NPC:" + npc.getId() + "|New Location,clearing monitors and releasing control.");
                    trait.unsetMonitoringPlugin();
                    pluginReference.monitoredNPC.remove(Integer.valueOf(npc.getId()));
                }
            }
        }
        return false;
    }

    @Override
    public void onEnableChanged(NPC npc, NPCDestinationsTrait trait, boolean enabled) {
        if (enabled) {
            if (pluginReference.npcSettings.containsKey(npc.getId())) {
                if (pluginReference.npcSettings.get(npc.getId()).locations.containsKey(trait.currentLocation.LocationIdent)) {
                    if (!pluginReference.monitoredNPC.containsKey(npc.getId())) {

                        pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Animations_Plugin.onNavigationReached|NPC:" + npc.getId() + "|Monitored location reached, assigning as monitor");
                        switch (pluginReference.npcSettings.get(npc.getId()).locations.get(trait.currentLocation.LocationIdent).action) {
                            case NONE:
                            case CHEST:
                            case CHEST_FILL:
                                break;
                            case COMPOST:
                            case FISH:
                            case FISH_ADD:
                            case SLEEP:
                            case SWING:
                            case SIT:
                                trait.setMonitoringPlugin(pluginReference.getPluginReference, trait.currentLocation);
                                pluginReference.monitoredNPC.put(npc.getId(), trait.currentLocation.LocationIdent);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        } else {
            if (pluginReference.monitoredNPC.containsKey(Integer.valueOf(npc.getId()))) {
                pluginReference.getDestinationsPlugin.getMessageManager.debugMessage(Level.INFO, "Animations_Plugin.onNavigationNewDestination|NPC:" + npc.getId() + "|plugin disabled for this npc, removing monitors.");
                trait.unsetMonitoringPlugin();
                pluginReference.monitoredNPC.remove(Integer.valueOf(npc.getId()));
            }
        }
    }
}
