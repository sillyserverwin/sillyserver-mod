package sillyserver;

import com.google.gson.Gson;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Config {
    private static final String CONFIG_FILENAME = Sillyserver.MOD_ID + ".json";
    private static ConfigValues VALUES;

    public static ConfigValues getConfig() {
        if (VALUES == null) {
            Path dirpath = FabricLoader.getInstance().getConfigDir();
            Path configPath = dirpath.resolve(CONFIG_FILENAME);

            try {
                List<String> lines = Files.readAllLines(configPath);
                Gson gson = new Gson();
                VALUES = gson.fromJson(String.join("\n", lines), ConfigValues.class);
                Sillyserver.LOGGER.info("Loaded Config");
            } catch (IOException e) {
                if (!Files.exists(configPath)) {
                    Sillyserver.LOGGER.info("Config file not found, creating default config at {}", configPath);
                    VALUES = new ConfigValues("", "");
                    String json = new Gson().toJson(VALUES);
                    try {
                        Files.writeString(configPath, json);
                    } catch (IOException ex) {
                        Sillyserver.LOGGER.error("Failed to write default config: {}", ex.getLocalizedMessage());
                    }
                } else {
                    Sillyserver.LOGGER.error("Error reading config file: {}, using default values", e.getLocalizedMessage());
                }
            }
        }

        return VALUES;
    }

    public record ConfigValues(String api_user, String api_token) {}
}
