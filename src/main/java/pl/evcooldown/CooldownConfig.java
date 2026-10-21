package pl.evcooldown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CooldownConfig {
    public static class Entry {
        public String name = "";
        public int seconds = 30;

        public Entry() {}

        public Entry(String name, int seconds) {
            this.name = name;
            this.seconds = seconds;
        }
    }

    public List<Entry> entries = new ArrayList<>();
    public boolean hudVisible = true;
    public boolean onlyActive = false;
    public int hudX = 5;
    public int hudY = 5;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("evcooldown.json");
    private static CooldownConfig instance = new CooldownConfig();

    public static CooldownConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    CooldownConfig c = GSON.fromJson(r, CooldownConfig.class);
                    if (c != null) {
                        if (c.entries == null) c.entries = new ArrayList<>();
                        instance = c;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = new CooldownConfig();
        instance.entries.add(new Entry("Boski Topor", 30));
        instance.entries.add(new Entry("Siekiera Grincha", 60));
        save();
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(instance, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
