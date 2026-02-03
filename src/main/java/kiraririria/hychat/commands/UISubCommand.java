package kiraririria.hychat.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import kiraririria.hychat.ui.HyChatDashboardUI;

import javax.annotation.Nonnull;

public class UISubCommand extends AbstractPlayerCommand {

    public UISubCommand() {
        super("ui", "Open the plugin dashboard");
        this.addAliases("dashboard", "gui");
        this.setPermissionGroup(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }


    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        context.sendMessage(Message.raw("Opening HyChat Dashboard..."));

        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                context.sendMessage(Message.raw("Error: Could not get Player component."));
                return;
            }

            HyChatDashboardUI dashboardPage = new HyChatDashboardUI(playerRef);
            player.getPageManager().openCustomPage(ref, store, dashboardPage);
            context.sendMessage(Message.raw("Dashboard opened. Press ESC to close."));
        } catch (Exception e) {
            context.sendMessage(Message.raw("Error opening dashboard: " + e.getMessage()));
        }
    }
}