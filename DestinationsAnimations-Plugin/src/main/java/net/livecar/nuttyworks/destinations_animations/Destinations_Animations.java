package net.livecar.nuttyworks.destinations_animations;

import net.citizensnpcs.Citizens;
import net.livecar.nuttyworks.destinations_animations.nms.VersionBridge;
import net.livecar.nuttyworks.destinations_animations.plugin.Animations_Settings;
import net.livecar.nuttyworks.destinations_animations.plugin.Commands;
import net.livecar.nuttyworks.destinations_animations.plugin.Destinations_Plugin;
import net.livecar.nuttyworks.destinations_animations.plugin.Processing;
import net.livecar.nuttyworks.npc_destinations.DestinationsPlugin;
import net.livecar.nuttyworks.npc_destinations.plugins.DestinationsAddon;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Destinations_Animations {
    public static Destinations_Animations Instance = null;

    public DestinationsPlugin getDestinationsPlugin = null;

    public Citizens getCitizensPlugin = null;

    public VersionBridge getNMSBridge = null;

    public DestAnimations_Plugin getPluginReference = null;

    public Processing getProcessingClass = null;

    public Destinations_Plugin getAnimationPlugin = null;

    public int entityRadius = 2209;

    public Map<Integer, Animations_Settings> npcSettings = new HashMap<>();

    public Map<Integer, UUID> monitoredNPC = new HashMap<>();

    public File languagePath;

    public Destinations_Animations() {
        this.getAnimationPlugin = new Destinations_Plugin(this);
        DestinationsPlugin.Instance.getPluginManager.registerPlugin((DestinationsAddon) this.getAnimationPlugin);
        DestinationsPlugin.Instance.getCommandManager.registerCommandClass(Commands.class);
        this.getProcessingClass = new Processing(this);
    }

    void getDefaultConfigs() {
        if (!DestinationsPlugin.Instance.getDataFolder().exists())
            DestinationsPlugin.Instance.getDataFolder().mkdirs();
        if (!this.languagePath.exists())
            this.languagePath.mkdirs();
        exportConfig(this.languagePath, "en_def-animations.yml");
    }

    private void exportConfig(File path, String filename) {
        DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.FINEST, "nuDestinationsAnimations.exportConfig()|");
        File fileConfig = new File(path, filename);
        if (!fileConfig.isDirectory())
            try {
                exportFile(filename, fileConfig);
            } catch (IOException e1) {
                if (this.getDestinationsPlugin != null) {
                    DestinationsPlugin.Instance.getMessageManager.debugMessage(Level.SEVERE, "nuDestinationsAnimations.exportConfig()|FailedToExtractFile(" + filename + ")");
                } else {
                    logToConsole(" Failed to extract default file (" + filename + ")");
                }
                return;
            }
    }

    public void logToConsole(String logLine) {
        Bukkit.getLogger().log(Level.INFO, "[" + DestinationsPlugin.Instance.getDescription().getName() + "] " + logLine);
    }

    private void exportFile(String source, File destination) throws IOException {
        if (!destination.exists())
            destination.delete();
        if (!destination.getParentFile().exists())
            throw new IOException("Folders missing.");
        if (!destination.createNewFile())
            throw new IOException("Failed to create a new file");
        URL sourceURL = getClass().getResource("/" + source);
        if (sourceURL == null)
            throw new IOException("Missing resource file");
        byte[] ioBuffer = new byte[1024];
        int bytesRead = 0;
        try {
            URLConnection inputConnection = sourceURL.openConnection();
            inputConnection.setUseCaches(false);
            InputStream fileIn = inputConnection.getInputStream();
            OutputStream fileOut = new FileOutputStream(destination);
            while ((bytesRead = fileIn.read(ioBuffer)) > 0)
                fileOut.write(ioBuffer, 0, bytesRead);
            fileOut.flush();
            fileOut.close();
            fileIn.close();
        } catch (Exception error) {
            throw new IOException("Failure exporting file");
        }
    }
}