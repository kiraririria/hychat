package kiraririria.hychat.core.utils;

import java.awt.Desktop;
import java.net.URI;

public class WebLinkUtil {
    public static void openWebLink(String address) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(address));
                return;
            }

            openWebLinkFallback(address);
        } catch (Exception e) {
            System.out.println("Desktop browse failed: " + e.getMessage());
            openWebLinkFallback(address);
        }
    }

    private static void openWebLinkFallback(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                String cmd = String.format("cmd /c start \"\" \"%s\"", url);
                Runtime.getRuntime().exec(cmd);
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", url).start();
            } else {
                System.err.println("[HC] Unsupported OS: " + os);
            }
        } catch (Exception e) {
            System.err.println("[HC] ERROR opening link: " + e.getMessage());
            System.err.println("[HC] URL was: " + url);
        }
    }
}