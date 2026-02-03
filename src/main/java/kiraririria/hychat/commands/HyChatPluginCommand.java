package kiraririria.hychat.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;


public class HyChatPluginCommand extends AbstractCommandCollection {

    public HyChatPluginCommand() {
        super("hychat", "HyChat plugin commands");

        this.addSubCommand(new ReloadSubCommand());
        this.addSubCommand(new TestSubCommand());
        this.addSubCommand(new UISubCommand());
        this.addSubCommand(new ModelSubCommand());
        this.addSubCommand(new DialogueSubCommand());
        this.addSubCommand(new PromptSubCommand());
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}