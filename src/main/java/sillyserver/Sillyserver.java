package sillyserver;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sillyserver.command.CommandManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Sillyserver implements ModInitializer {
	public static final String MOD_ID = "sillyserver";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Map<String, Rank> RANKS = new HashMap<>();
    public static final Map<UUID, Rank> CACHE =  new HashMap<>();
    private static long lastUpdate = 0;
    private static final HttpClient httpClient;
    private static final Timer UPDATE_TIMER =  new Timer();

    static {
        httpClient =  HttpClient.newHttpClient();

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:4000/ranks"))
                    .GET()
                    .build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString())
                    .body()
                    .lines()
                    .findFirst()
                    .ifPresent(line -> {
                        Gson gson = new Gson();
                        JsonArray arr =  gson.fromJson(line, JsonArray.class);
                        for (JsonElement el : arr) {
                            Rank rank = gson.fromJson(el, Rank.class);
                            RANKS.put(rank.id(), rank);
                        }
                    });
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error("Error fetching ranks: {}", e.getLocalizedMessage());
        }
    }

    public static Rank getPlayerRank(Player player) {
        if (CACHE.containsKey(player.getUUID())) {
            return CACHE.get(player.getUUID());
        }

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:4000/player/" + player.getStringUUID() + "/rank"))
                    .GET()
                    .build();
            AtomicReference<Rank> rank = new AtomicReference<>();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString())
                    .body()
                    .lines()
                    .findFirst()
                    .ifPresent(line -> {
                        Gson gson = new Gson();
                        rank.set(gson.fromJson(line, Rank.class));
                    });
            Rank r =  rank.get();
            CACHE.put(player.getUUID(), r);

            return r;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            LOGGER.error("Error fetching player rank: {}", e.getLocalizedMessage());
        }

        return null;
    }

    public static List<Rank> getUnlockedRanks(Player player) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:4000/player/" + player.getStringUUID() + "/ranks"))
                    .GET()
                    .build();
            AtomicReference<List<Rank>> ranks = new AtomicReference<>();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString())
                    .body()
                    .lines()
                    .findFirst()
                    .ifPresent(line -> {
                        Gson gson = new Gson();
                        JsonArray arr = gson.fromJson(line, JsonArray.class);
                        List<Rank> rankList = new ArrayList<>();
                        for (JsonElement el : arr) {
                            Rank rank = gson.fromJson(el, Rank.class);
                            rankList.add(rank);
                        }
                        ranks.set(rankList);
                    });
            return ranks.get();
        } catch (InterruptedException | URISyntaxException | IOException e) {
            LOGGER.error("Error fetching player ranks: {}", e.getLocalizedMessage());
        }

        return null;
    }

	@Override
	public void onInitialize() {
        ServerPlayerEvents.JOIN.register((player) -> {
            LOGGER.info("Player {} joined the server", player.getName().getString());
        });
        ServerPlayerEvents.LEAVE.register((player) -> {
            LOGGER.info("Player {} left the server", player.getName().getString());
        });
        CommandRegistrationCallback.EVENT.register(CommandManager::registerCommands);


        UPDATE_TIMER.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Updating ranks...");
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:4000/rankupdates?last_updated=" + lastUpdate))
                            .GET()
                            .build();
                    httpClient.send(req, HttpResponse.BodyHandlers.ofString())
                            .body()
                            .lines()
                            .findFirst()
                            .ifPresent(line -> {
                                System.out.println(line);
                                Gson gson = new Gson();
                                Map<String, JsonElement> map =  gson.fromJson(line, JsonObject.class).asMap();
                                System.out.println(map);
                                for (Map.Entry<String, JsonElement> entry : map.entrySet()) {
                                    Rank rank = gson.fromJson(entry.getValue(), Rank.class);
                                    CACHE.put(UUID.fromString(entry.getKey()), rank);
                                }
                            });
                    lastUpdate = System.currentTimeMillis();
                } catch (IOException | InterruptedException e) {
                    LOGGER.error("Error fetching ranks: {}", e.getLocalizedMessage());
                } catch (URISyntaxException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
        }, 0, 60000);
	}
}