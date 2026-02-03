package kiraririria.hychat;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import kiraririria.hychat.commands.HyChatPluginCommand;
import kiraririria.hychat.common.HyChatConfig;
import kiraririria.hychat.common.HyChatFiles;
import kiraririria.hychat.core.data.CharacterCard;
import kiraririria.hychat.interactions.HyChatInteraction;
import kiraririria.hychat.listeners.PlayerListener;
import com.hypixel.hytale.server.core.util.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import java.util.HashMap;


public class HyChatPlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<HyChatConfig> config;

    private static HyChatPlugin instance;

    public static PlayerRef playerRef;


    public static HashMap<String, CharacterCard> characterCards = new HashMap<>();

    public HyChatPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        config = this.withConfig("MyConfig", HyChatConfig.CODEC);
        instance = this;

    }

    public static HyChatPlugin getInstance() {
        return instance;
    }

    public Config<HyChatConfig> getConfig()
    {
        return config;
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("[HyChat] Setting up...");
        registerCommands();
        registerListeners();
        config.save();
        this.getCodecRegistry(Interaction.CODEC).register("hy_chat_interaction_id", HyChatInteraction.class, HyChatInteraction.CODEC);
        initFolders();
        try
        {
            loadCards();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        LOGGER.at(Level.INFO).log("[HyChat] Setup complete!");
    }

    private void initFolders()
    {
        HyChatFiles.getGlobalFolder().toFile().mkdirs();
        HyChatFiles.getModFolder().toFile().mkdirs();
        HyChatFiles.getCardsFolder().toFile().mkdirs();
        HyChatFiles.getSettingsFolder().toFile().mkdirs();
    }

    public static void loadCards() throws IOException
    {
        characterCards.clear();
            Files.list(HyChatFiles.getCardsFolder())
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".png"))
                    .forEach(path -> {
                        String name = path.getFileName().toString().replace(".png", "");
                        CharacterCard characterCard = CharacterCard.fromPng(name);
                        characterCards.put(name,characterCard);
                    });

    }


    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new HyChatPluginCommand());
            LOGGER.at(Level.INFO).log("[HyChat] Registered /hychat command");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[HyChat] Failed to register commands");
        }
    }


    private void registerListeners() {
        EventRegistry eventBus = getEventRegistry();

        try {
            new PlayerListener().register(eventBus);
            LOGGER.at(Level.INFO).log("[HyChat] Registered player event listeners");
        } catch (Exception e) {
            LOGGER.at(Level.WARNING).withCause(e).log("[HyChat] Failed to register listeners");
        }
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("[HyChat] Started!");
        LOGGER.at(Level.INFO).log("[HyChat] Use /hychat help for commands");
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("[HyChat] Shutting down...");
        instance = null;
    }


}