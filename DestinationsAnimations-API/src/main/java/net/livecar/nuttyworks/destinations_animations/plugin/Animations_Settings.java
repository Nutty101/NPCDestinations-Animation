package net.livecar.nuttyworks.destinations_animations.plugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.livecar.nuttyworks.npc_destinations.citizens.NPCDestinationsTrait;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Animations_Settings {

    public int npcID;
    public Map<UUID, Animations_Location> locations;
    public Location currentDestination;
    public NPCDestinationsTrait destinationsTrait;
    public Date lastAction;
    public Date nextAnimationTime;

    public FishHook fishHook;
    public boolean isSleeping;
    public Date castTime;
    public Location hookDestination;

    public Animations_Settings() {
        locations = new HashMap<>();
        lastAction = new Date();
        nextAnimationTime = new Date();
    }

    public void setNPC(Integer npcId) {
        this.npcID = npcId;
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
        destinationsTrait = npc.getTrait(NPCDestinationsTrait.class);
        locations = new HashMap<>();
    }

    public Integer getNPCID() {
        return npcID;
    }

    public enum enAction {
        NONE, SLEEP, CHEST, CHEST_FILL, CHEST_EMPTY, FISH_ADD, FISH, SIT, SWING, COMPOST, ARMSBOUNCE;

        public enAction GetValue(String value) {
            for (enAction enumVal : enAction.values()) {
                if (enumVal.name().equals(value)) {
                    return enumVal;
                }
            }
            return null;
        }
    }
}
