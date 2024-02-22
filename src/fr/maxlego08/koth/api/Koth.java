package fr.maxlego08.koth.api;

import fr.maxlego08.koth.zcore.utils.Cuboid;
import org.bukkit.Location;

import java.util.List;

public interface Koth {

    String getName();

    Location getMinLocation();

    Location getMaxLocation();

    Cuboid getCuboid();

    Location getCenter();

    List<String> getStartCommands();

    List<String> getEndCommands();

    void move(Location minLocation, Location maxLocation);

}