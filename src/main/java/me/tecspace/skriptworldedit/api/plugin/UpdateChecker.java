package me.tecspace.skriptworldedit.api.plugin;

import ch.njol.skript.util.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.tecspace.skriptworldedit.SkriptWorldEdit;
import me.tecspace.skriptworldedit.api.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    //private static final String GITHUB_SOURCE = "tecspace/SkriptWorldEdit";
    private final String GITHUB_SOURCE;
    private final SkriptWorldEdit plugin;

    public UpdateChecker(SkriptWorldEdit plugin, String author, String project) {
        this.plugin = plugin;
        this.GITHUB_SOURCE = author + "/" + project;
    }

    public void checkForUpdates() {
        CompletableFuture
                .supplyAsync(this::fetchLatestVersion)
                .thenAccept(this::compareVersions)
                .exceptionally(e -> null);
    }

    private @Nullable Version fetchLatestVersion() {
        try {
            URL url = URL.of(URI.create("https://api.github.com/repos/" + GITHUB_SOURCE + "/releases/latest"), null);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                String tag = json.get("tag_name").getAsString();
                return new Version(tag);
            }
        } catch (IOException | IllegalArgumentException ignored) {}
        return null;
    }

    private void compareVersions(@Nullable Version latest) {
        Version pluginVersion = new Version(plugin.getPluginMeta().getVersion());
        if (latest == null) {
            Utils.log("Failed to check for updates.");
        } else if (pluginVersion.isSmallerThan(latest)) {
            Utils.log(
                    "A new update for SkriptWorldEdit is available (" + pluginVersion + ") You are running (" + latest + "). Download it at https://github.com/" + GITHUB_SOURCE + "/releases");
        } else {
            Utils.log("You are running the latest version!");
        }
    }
}