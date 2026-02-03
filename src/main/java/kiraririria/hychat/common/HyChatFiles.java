package kiraririria.hychat.common;

import com.hypixel.hytale.server.core.Constants;
import java.nio.file.Path;

public class HyChatFiles
{
    public static Path getGlobalFolder()
    {
        Path current = Constants.UNIVERSE_PATH;
        Path parent1 = current.getParent();
        if (parent1 == null) {
            return Path.of("HyChat");
        }

        Path parent2 = parent1.getParent();
        if (parent2 == null) {
            return parent1.resolve("HyChat");
        }
        Path parent3 = parent2.getParent();
        if (parent3 == null) {
            return parent2.resolve("HyChat");
        }

        return parent3.resolve("HyChat");
    }

    public static Path getModFolder()
    {
        return Constants.UNIVERSE_PATH.resolve("HyChat");
    }

    public static Path getCardsFolder()
    {
        return getModFolder().resolve("cards");
    }

    public static Path getSettingsFolder()
    {
        return getGlobalFolder().resolve("settings");
    }
}