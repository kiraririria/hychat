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
import kiraririria.hychat.ui.HyChatDialogueUI;
import kiraririria.hychat.ui.ModelSelectorUI;

import javax.annotation.Nonnull;

public class DialogueSubCommand extends AbstractPlayerCommand {

    public DialogueSubCommand() {
        super("dialogue", "Open the Dialogue");
        this.setPermissionGroup(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    /**
     * Called on the world thread with proper player context.
     */
    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World world
    ) {
        context.sendMessage(Message.raw("Opening Dialogue..."));

        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                context.sendMessage(Message.raw("Error: Could not get Player component."));
                return;
            }
            HyChatDialogueUI modelSelectorPage = new HyChatDialogueUI(playerRef, null);
            player.getPageManager().openCustomPage(ref, store, modelSelectorPage);
            context.sendMessage(Message.raw("Dialogue opened. Press ESC to close."));
        } catch (Exception e) {
            context.sendMessage(Message.raw("Error opening Dialogue: " + e.getMessage()));
        }
    }
}