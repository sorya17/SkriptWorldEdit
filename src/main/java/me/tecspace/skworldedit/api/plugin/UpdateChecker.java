package me.tecspace.skworldedit.api.plugin;

import ch.njol.skript.util.Version;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.tecspace.skworldedit.SkWorldEdit;
import me.tecspace.skworldedit.api.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UpdateChecker {

    private final String githubSource;
    private final SkWorldEdit plugin;

    public UpdateChecker(SkWorldEdit plugin, String author, String project) {
        this.plugin = plugin;
        this.githubSource = author + "/" + project;
    }

    public void checkForUpdates() {
        CompletableFuture
                .supplyAsync(this::fetchLatestVersion)
                .thenAccept(this::compareVersions)
                .exceptionally(e -> null);
    }

    private @Nullable Version fetchLatestVersion() {
        try {
            URL url = URL.of(URI.create("https://api.github.com/repos/" + githubSource + "/releases/latest"), null);
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
                    "A new update for SkriptWorldEdit is available (" + latest + ") You are running (" + pluginVersion + "). Download it at https://github.com/" + githubSource + "/releases");
        } else {
            Utils.log("You are running the latest version!");
        }
    }
}