package kiraririria.hychat.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.api.HyChatAPI;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;


public class PromptSubCommand extends CommandBase {

    public PromptSubCommand() {
        super("prompt", "Prompt");
        this.setAllowsExtraArguments(true);

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
        String inputString = context.getInputString();
        String rawArgs = CommandUtil.stripCommandName(inputString).trim();
        if (rawArgs.isEmpty()) {
            context.sendMessage(Message.translation("server.commands.parsing.error.wrongNumberRequiredParameters").param("expected", 1).param("actual", 0));
        } else {
            AtomicReference<String> result = new AtomicReference<>("");
            HyChatAPI.runGeneration(rawArgs, result::set,()->{
                context.sendMessage(Message.raw(result.get()));
            });
        }


    }
}