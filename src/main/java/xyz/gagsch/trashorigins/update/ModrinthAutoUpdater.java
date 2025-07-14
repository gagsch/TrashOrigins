package xyz.gagsch.trashorigins.update;


import com.google.gson.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;

import static xyz.gagsch.trashorigins.TrashOrigins.LOGGER;
import static xyz.gagsch.trashorigins.TrashOrigins.MODID;

public class ModrinthAutoUpdater {
    public static final Path MODS = Paths.get("mods");
    public static String currentVersion;

    public static String downloadUrl;
    public static String latestVersion;

    public static boolean checkNewUpdate() {
        if (!(FMLLoader.getDist().isClient() || FMLLoader.getDist().isDedicatedServer())) {
            return false;
        }

        currentVersion = ModList.get().getModContainerById(MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");

        if (currentVersion.equals("unknown")) {
            LOGGER.warn("Could not determine current mod version.");
            return false;
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.modrinth.com/v2/project/" + MODID + "/version").openConnection();
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonArray versions = JsonParser.parseReader(reader).getAsJsonArray();
            reader.close();

            for (JsonElement versionElem : versions) {
                JsonObject version = versionElem.getAsJsonObject();

                downloadUrl = version.getAsJsonArray("files").get(0).getAsJsonObject().get("url").getAsString();
                latestVersion = version.get("version_number").getAsString();

                LOGGER.debug(downloadUrl);

                return !latestVersion.equals(currentVersion);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not check for updates: " + e.getMessage());
        }

        LOGGER.warn("Could not check for updates: Unknown");
        return false;
    }

    public static void updateAndRestart() {
        if (!(FMLLoader.getDist().isClient() || FMLLoader.getDist().isDedicatedServer())) {
            return;
        }

        Path updateJar = MODS.resolve(MODID + "-" + latestVersion + ".jar");

        try (InputStream in = new URL(downloadUrl).openStream()) {
            Files.copy(in, updateJar, StandardCopyOption.REPLACE_EXISTING);
            deleteCurrentVersion();

            LOGGER.error("\n==================================\n" +
                    "Downloaded update: " + latestVersion + "\n" +
                    "It will be applied on next launch.\n" +
                    "Deleting the old JAR may fail.\n" +
                    "==================================");

            System.exit(0);
        }
        catch (IOException ignored) {
        }
    }

    private static void deleteCurrentVersion() {
        File[] jars = MODS.toFile().listFiles((dir, name) ->
                name.equals(MODID + "-" + currentVersion + ".jar")
        );

        if (jars == null || jars.length == 0) {
            LOGGER.warn("Could not find current mod jar to delete.");
            return;
        }

        File jar = jars[0];

        if (jar.delete()) {
            LOGGER.info("Deleted old version: " + jar.getName());
        } else {
            LOGGER.warn("Failed to delete old version: " + jar.getName());
        }
    }
}