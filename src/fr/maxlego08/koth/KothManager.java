package fr.maxlego08.koth;

import fr.maxlego08.koth.api.Koth;
import fr.maxlego08.koth.api.KothStatus;
import fr.maxlego08.koth.api.KothTeam;
import fr.maxlego08.koth.api.KothType;
import fr.maxlego08.koth.api.events.KothCreateEvent;
import fr.maxlego08.koth.hook.TeamPlugin;
import fr.maxlego08.koth.hook.teams.NoneHook;
import fr.maxlego08.koth.loader.KothLoader;
import fr.maxlego08.koth.zcore.enums.Message;
import fr.maxlego08.koth.zcore.logger.Logger;
import fr.maxlego08.koth.zcore.utils.ZUtils;
import fr.maxlego08.koth.zcore.utils.builder.ItemBuilder;
import fr.maxlego08.koth.zcore.utils.loader.Loader;
import fr.maxlego08.koth.zcore.utils.storage.Persist;
import fr.maxlego08.koth.zcore.utils.storage.Savable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class KothManager extends ZUtils implements Savable {

    private final Loader<Koth> kothLoader;
    private final List<Koth> koths = new ArrayList<>();
    private final Map<UUID, Selection> selections = new HashMap<>();
    private final KothPlugin plugin;
    private final File folder;
    private KothTeam kothTeam = new NoneHook();

    public KothManager(KothPlugin plugin) {
        this.plugin = plugin;
        this.kothLoader = new KothLoader(plugin);
        this.folder = new File(plugin.getDataFolder(), "koths");
        if (!this.folder.exists()) this.folder.mkdir();

        for (TeamPlugin value : TeamPlugin.values()) {
            if (value.isEnable()) {
                kothTeam = value.init(plugin);
                Logger.info("Register " + value.getPluginName() + " team implementation.", Logger.LogType.INFO);
            }
        }
    }

    @Override
    public void save(Persist persist) {
        this.selections.values().forEach(Selection::clear);
    }

    @Override
    public void load(Persist persist) {

        File[] files = this.folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            Koth koth = this.kothLoader.load(configuration, "", file);
            this.koths.add(koth);
        }

        System.out.println(koths);
    }

    public Optional<Selection> getSelection(UUID uuid) {
        return Optional.ofNullable(this.selections.getOrDefault(uuid, null));
    }

    public ItemStack getKothAxe() {
        ItemBuilder builder = new ItemBuilder(Material.STONE_AXE, Message.AXE_NAME.getMessage());
        Message.AXE_DESCRIPTION.getMessages().forEach(builder::addLine);
        return builder.build();
    }

    public void createSelection(UUID uniqueId, Selection selection) {
        this.selections.put(uniqueId, selection);
    }

    public void saveKoth(Koth koth) {

        File file = new File(this.folder, koth.getFileName() + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        this.kothLoader.save(koth, configuration, "");
        try {
            configuration.save(file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }


    public void createKoth(Player player, String name, Location minLocation, Location maxLocation, int capture, KothType kothType) {

        String fileName = name.replace(" ", "_");
        Optional<Koth> optional = getKoth(fileName);
        if (optional.isPresent()) {
            message(player, Message.ALREADY_EXIST, "%name%", name);
            return;
        }

        int distance = Math.abs(minLocation.getBlockX() - maxLocation.getBlockY());
        if (distance <= 0) {
            message(player, Message.KOTH_SIZE);
            return;
        }

        Koth koth = new ZKoth(this.plugin, fileName, kothType, name, capture, minLocation, maxLocation);

        KothCreateEvent event = new KothCreateEvent(koth);
        event.call();

        if (event.isCancelled()) return;

        Optional<Selection> optionalSelection = getSelection(player.getUniqueId());
        optionalSelection.ifPresent(Selection::clear);

        this.koths.add(koth);
        message(player, Message.CREATE_SUCCESS, "%name%", name);

        this.saveKoth(koth);
    }

    private Optional<Koth> getKoth(String name) {
        return this.koths.stream().filter(koth -> name != null && koth.getFileName().equalsIgnoreCase(name)).findFirst();
    }

    public KothTeam getKothTeam() {
        return kothTeam;
    }

    public List<Koth> getActiveKoths() {
        return koths.stream().filter(koth -> koth.getStatus() == KothStatus.START).collect(Collectors.toList());
    }

    public List<Koth> getEnableKoths() {
        return koths.stream().filter(koth -> koth.getStatus() == KothStatus.COOLDOWN).collect(Collectors.toList());
    }

    public List<String> getNameKoths() {
        return this.koths.stream().map(Koth::getFileName).collect(Collectors.toList());
    }

    public void spawnKoth(CommandSender sender, String name, boolean now) {

        Optional<Koth> optional = getKoth(name);
        if (!optional.isPresent()) {
            message(sender, Message.DOESNT_EXIST, "%name%", name);
            return;
        }

        Koth koth = optional.get();
        koth.spawn(sender, now);

        /*if (Config.enableScoreboard) {

            this.manager.setLinesAndSchedule(koth.onScoreboard());
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.manager.createBoard(player, Config.scoreboardTitle);
            }
        }*/
    }
}
