package fr.maxlego08.koth;

import fr.maxlego08.koth.api.Koth;
import fr.maxlego08.koth.placeholder.LocalPlaceholder;
import fr.maxlego08.koth.save.Config;
import fr.maxlego08.koth.zcore.utils.ReturnConsumer;
import fr.maxlego08.koth.zcore.utils.builder.TimerBuilder;

import java.util.Optional;

public class KothPlaceholder {

    private final KothManager kothManager;

    public KothPlaceholder(KothManager kothManager) {
        this.kothManager = kothManager;
    }

    public void register() {

        this.register("name", Koth::getName);
        this.register("world", koth -> koth.getCenter().getWorld().getName());
        this.register("min_x", koth -> String.valueOf(koth.getMinLocation().getBlockX()));
        this.register("min_y", koth -> String.valueOf(koth.getMinLocation().getBlockY()));
        this.register("min_z", koth -> String.valueOf(koth.getMinLocation().getBlockZ()));
        this.register("max_x", koth -> String.valueOf(koth.getMaxLocation().getBlockX()));
        this.register("max_y", koth -> String.valueOf(koth.getMaxLocation().getBlockY()));
        this.register("max_z", koth -> String.valueOf(koth.getMaxLocation().getBlockZ()));
        this.register("center_x", koth -> String.valueOf(koth.getCenter().getBlockX()));
        this.register("center_y", koth -> String.valueOf(koth.getCenter().getBlockY()));
        this.register("center_z", koth -> String.valueOf(koth.getCenter().getBlockZ()));

        this.register("spawn_seconds", koth -> String.valueOf(koth.getRemainingSeconds() == null ? koth.getCaptureSeconds() : koth.getRemainingSeconds().get()));
        this.register("spawn_format", koth -> TimerBuilder.getStringTime(koth.getRemainingSeconds() == null ? koth.getCaptureSeconds() : koth.getRemainingSeconds().get()));
        this.register("capture_format", koth -> String.valueOf(koth.getRemainingSeconds() == null ? koth.getCaptureSeconds() : koth.getRemainingSeconds().get()));
        this.register("capture_seconds", koth -> TimerBuilder.getStringTime(koth.getRemainingSeconds() == null ? koth.getCaptureSeconds() : koth.getRemainingSeconds().get()));

        this.register("player_name", koth -> koth.getCurrentPlayer() != null ? koth.getCurrentPlayer().getName() : Config.noPlayer);
        this.register("team_name", koth -> koth.getCurrentPlayer() != null ? kothManager.getKothTeam().getFactionTag(koth.getCurrentPlayer()) : Config.noFaction);
    }

    private void register(String key, ReturnConsumer<Koth> consumer) {
        LocalPlaceholder placeholder = LocalPlaceholder.getInstance();
        placeholder.register(key, (a, b) -> onFirstKoth(consumer));
    }

    public String onFirstKoth(ReturnConsumer<Koth> consumer) {
        Optional<Koth> optional = this.kothManager.getActiveKoths().stream().findFirst();
        if (optional.isPresent()) {
            return consumer.accept(optional.get());
        } else return Config.noKoth;
    }

}
