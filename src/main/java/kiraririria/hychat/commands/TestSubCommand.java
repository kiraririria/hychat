package kiraririria.hychat.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.api.HyChatAPI;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;


public class TestSubCommand extends CommandBase {

    public TestSubCommand() {
        super("test", "Test prompt");
        this.setPermissionGroup(null);
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        HyChatPlugin plugin = HyChatPlugin.getInstance();

        if (plugin == null) {
            context.sendMessage(Message.raw("Error: Plugin not loaded"));
            return;
        }

        context.sendMessage(Message.raw("Testing HyChat..."));
        AtomicReference<String> result = new AtomicReference<>("");
        HyChatAPI.runGeneration("Hi, are you ready?", result::set,()->{
            context.sendMessage(Message.raw("[HyChat]: "+result.get()));
        });

    }
}