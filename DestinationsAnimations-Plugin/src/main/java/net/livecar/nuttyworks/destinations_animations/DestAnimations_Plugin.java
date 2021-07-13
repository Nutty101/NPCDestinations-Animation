package net.livecar.nuttyworks.destinations_animations;

import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.livecar.nuttyworks.destinations_animations.nms.*;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

public class DestAnimations_Plugin extends org.bukkit.plugin.java.JavaPlugin implements org.bukkit.event.Listener {

    public void onDisable() {
        if (Destinations_Animations.Instance != null && Destinations_Animations.Instance.getDestinationsPlugin != null) {
            Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.debugMessage(Level.CONFIG, "nuDestinationAnimations.onDisable()|Stopping Internal Processes");

            //Clean up sitting NPCs
            Destinations_Animations.Instance.getProcessingClass.CleanupAnimations();
        }
    }

    public void onEnable() {
        if (Bukkit.getServer().getPluginManager().getPlugin("NPC_Destinations") == null) {
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations2 not found, not registering as plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            if (getServer().getPluginManager().getPlugin("NPC_Destinations").getDescription().getVersion().startsWith("1")) {
                Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations V1 was found, This requires V2. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            } else if (!getServer().getPluginManager().getPlugin("NPC_Destinations").isEnabled()) {
                Bukkit.getLogger().log(java.util.logging.Level.INFO, "[" + getDescription().getName() + "] " + "NPCDestinations was found, but was disabled. Not registering as plugin");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            Destinations_Animations.Instance = new Destinations_Animations();
            Destinations_Animations.Instance.getPluginReference = this;
            Destinations_Animations.Instance.getDestinationsPlugin = DestinationsPlugin.Instance;
            // Setup the default paths in the storage folder.
            Destinations_Animations.Instance.languagePath = new File(DestinationsPlugin.Instance.getDataFolder(), "/Languages/");

            // Generate the default folders and files.
            Destinations_Animations.Instance.getDefaultConfigs();

            // Force destinations to refresh its language files.
            Destinations_Animations.Instance.getDestinationsPlugin.getLanguageManager.loadLanguages(true);
        }

        // Global references
        Destinations_Animations.Instance.getCitizensPlugin = DestinationsPlugin.Instance.getCitizensPlugin;


        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);

        // What version of NMS are we
        if (getServer().getClass().getPackage().getName().endsWith("v1_8_R3")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_8_R3();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_9_R2")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_9_R2();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_10_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_10_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_11_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_11_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_12_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_12_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_13_R2")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_13_R2();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_14_R1") || getServer().getClass().getPackage().getName().endsWith("v1_14_R2")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_14_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_15_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_15_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_16_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_16_R1();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_16_R2")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_16_R2();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_16_R3")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_16_R3();
        } else if (getServer().getClass().getPackage().getName().endsWith("v1_17_R1")) {
            Destinations_Animations.Instance.getNMSBridge = new VersionBridge_1_17_R1();
        } else {
            // Unknown version, abort loading of this plugin
            Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(this, "animations", "console_messages.plugin_unknownversion");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @EventHandler
    public void CitizensLoaded(final CitizensEnableEvent event) {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            try {
                // Loop monitored and processing the fishing
                Destinations_Animations.Instance.getProcessingClass.onPluginTick();
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                sw.toString(); // stack trace as a string
                if (Destinations_Animations.Instance.getDestinationsPlugin != null)
                    Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.logToConsole(Destinations_Animations.Instance.getPluginReference, "Error:" + sw);
                else
                    Destinations_Animations.Instance.logToConsole("Error on Animationtick: " + sw);
            }
        }, 1L, 5L);
    }

    @EventHandler
    public void CitizensDisabled(final CitizensDisableEvent event) {
        Bukkit.getServer().getScheduler().cancelTasks(this);
        if (Destinations_Animations.Instance.getDestinationsPlugin == null) {
            Destinations_Animations.Instance.logToConsole("Disabled..");
        } else {
            Destinations_Animations.Instance.getDestinationsPlugin.getMessageManager.consoleMessage(this, "animations", "console_messages.plugin_ondisable");
        }
        Destinations_Animations.Instance = null;
    }

    @EventHandler
    public void npcDespawned(final NPCDespawnEvent event) {
        Destinations_Animations.Instance.getProcessingClass.undoAnimations(event.getNPC());
    }

}
