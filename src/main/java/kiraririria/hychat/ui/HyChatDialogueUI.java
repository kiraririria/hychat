package kiraririria.hychat.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import io.sentry.util.StringUtils;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.api.DialogueMessage;
import kiraririria.hychat.api.HyChatAPI;
import kiraririria.hychat.core.data.CharacterCard;
import kiraririria.hychat.core.request.PromptMessage;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class HyChatDialogueUI extends InteractiveCustomUIPage<HyChatDialogueUI.DiaalogueGuiData> {

    private final List<DialogueMessage> messages;
    public CharacterCard characterCard;
    public String userInput = "";
    public boolean isRunningGen = false;
    private String printSt = "";
    private String chatName = "";


    Ref<EntityStore> ref;
    Store<EntityStore> store;

    public HyChatDialogueUI(@Nonnull PlayerRef playerRef, NPCEntity entity) {
        super(playerRef, CustomPageLifetime.CanDismiss, DiaalogueGuiData.CODEC);
        this.messages = new ArrayList<>();
        if (entity ==  null)
        {
            chatName = "Chat " + playerRef.getUsername() + " with NPC";

        }else
        {
            chatName = "Chat " + playerRef.getUsername() + " with " + entity.getRoleName();
        }
        characterCard = CharacterCard.empty();
        this.messages.add(new DialogueMessage(characterCard.firstMes, DialogueMessage.Role.ASSISTANT));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("hychat/Dialogue.ui");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#UserField",
                EventData.of("@InputField", "#UserField.Value")         ,
                false
        );
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SendMessage",
                new EventData().append("Action", "send"),
                false
        );

        this.buildList(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull DiaalogueGuiData data) {
        super.handleDataEvent(ref, store, data);
        this.ref = ref;
        this.store = store;
        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        var player = store.getComponent(ref, Player.getComponentType());

        if (data.inputField != null) {
            this.userInput = data.inputField;
        }
        if (data.action != null) {
            if (data.action.equals("send")) {
                sendUserMessage();
            }
        }

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        this.buildList(ref, commandBuilder, eventBuilder, store);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }
    public void updt()
    {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        this.buildList(ref, commandBuilder, eventBuilder, store);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }
    public void sendUserMessage()
    {
        formatInput();
        if (!userInput.isEmpty() && !isRunningGen)
        {
            this.messages.add(new DialogueMessage(userInput, DialogueMessage.Role.USER));
            this.userInput =  "";
            this.isRunningGen = true;
            this.printSt = "Responds...";
            runAnswer();
        }
    }

    private void formatInput()
    {

        this.userInput = this.userInput.trim();
        this.userInput = StringUtils.capitalize(userInput);
    }

    public void runAnswer()
    {
        AtomicReference<DialogueMessage> result = new AtomicReference<>(new DialogueMessage("", DialogueMessage.Role.ASSISTANT));
        this.messages.add(result.get());
        List<PromptMessage> promptMessages = dialogueToPrompt();
        promptMessages.addFirst(new PromptMessage("You are in a virtual role-playing chat. You must respond to the user by playing your character", PromptMessage.Role.SYSTEM));
        HyChatAPI.runGeneration(promptMessages, (r)-> {result.get().content = r;
            this.printSt = "";
            updt();
            },()-> {isRunningGen=false;
            updt();
        });
    }

    public List<PromptMessage> dialogueToPrompt() {
        List<PromptMessage> promptMessages = new ArrayList<>();
        int totalMessages = this.messages.size();
        int startIndex = Math.max(0, totalMessages - HyChatPlugin.getInstance().getConfig().get().getChatLen());

        List<DialogueMessage> lastMessages = this.messages.subList(startIndex, totalMessages);
        for (DialogueMessage dialogueMessage : lastMessages) {
            promptMessages.add(new PromptMessage(
                    dialogueMessage.content,
                    PromptMessage.Role.valueOf(dialogueMessage.role.name())
            ));
        }
        return promptMessages;
    }
    private void buildList(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                           @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        commandBuilder.set("#UserField.Value", this.userInput);
        commandBuilder.set("#Print.Text", this.printSt);
        commandBuilder.set("#CurrentNpc.Text", this.chatName);

        this.buildButtons(messages, commandBuilder, eventBuilder, store);
    }

    @Override
    protected void close() {
        super.close();
    }

    private void buildButtons(List<DialogueMessage> items, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.clear("#IndexElements");
        uiCommandBuilder.appendInline("#Main #IndexList", "Group #IndexElements { LayoutMode: Left; }");
        var i = 0;
        for (DialogueMessage name : items) {

            uiCommandBuilder.append("#IndexElements", "hychat/MessageEntry.ui");
            uiCommandBuilder.set("#IndexElements[" + i + "] #Message.Text", name.content);


            if (Objects.equals(name.role, DialogueMessage.Role.USER))
            {
                uiCommandBuilder.set("#IndexElements[" + i + "] #Message.Style.HorizontalAlignment","Center");

                uiCommandBuilder.set("#IndexElements[" + i + "] #Message.Style.TextColor","#00FF14");
            }

            ++i;
        }
    }

    public static class DiaalogueGuiData
    {
        static final String ACTION = "Action";

        public static final BuilderCodec<DiaalogueGuiData> CODEC = BuilderCodec.builder(DiaalogueGuiData.class, DiaalogueGuiData::new)
                .addField(new KeyedCodec<>(ACTION, Codec.STRING), (diaalogueGuiData, s) -> diaalogueGuiData.action = s, diaalogueGuiData -> diaalogueGuiData.action)
                .addField(new KeyedCodec<>("@InputField", Codec.STRING), (e, v) -> e.inputField = v, e -> e.inputField).build();

        private String action;
        private String inputField;
    }
}
