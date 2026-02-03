package kiraririria.hychat.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.api.HyChatAPI;

import javax.annotation.Nonnull;


public class HyChatDashboardUI extends InteractiveCustomUIPage<HyChatDashboardUI.UIEventData> {

    public static final String LAYOUT = "hychat/Dashboard.ui";

    private final PlayerRef playerRef;
    private int refreshCount = 0;

    public HyChatDashboardUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, UIEventData.CODEC);
        this.playerRef = playerRef;
        HyChatPlugin.playerRef = playerRef;
    }

    @Override
    public void build(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull UICommandBuilder cmd,
            @Nonnull UIEventBuilder evt,
            @Nonnull Store<EntityStore> store
    ) {
        cmd.append(LAYOUT);
        cmd.set("#StreamResponse #CheckBox.Value", HyChatPlugin.getInstance().getConfig().get().isStreamResponse());
        cmd.set("#OnlineModel #CheckBox.Value", HyChatPlugin.getInstance().getConfig().get().isOnlineMode());

        cmd.set("#NewField.Value", HyChatPlugin.getInstance().getConfig().get().getKoboldUrl());

        evt.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#NewField",
                EventData.of("@InputField", "#NewField.Value")         ,
                false
        );
        evt.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#OpenRouterAuth",
                new EventData().append("Action", "openRouterAuth"),
                false
        );

        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#RefreshButton",
            new EventData().append("Action", "refresh"),
            false
        );

        evt.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "close"),
            false
        );

        evt.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#StreamResponse #CheckBox",
                new EventData().append("Action", "stream"),
                false
        );

        evt.addEventBinding(
                CustomUIEventBindingType.ValueChanged,
                "#OnlineModel #CheckBox",
                new EventData().append("Action", "online"),
                false
        );


    }

    @Override
    public void handleDataEvent(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nonnull UIEventData data
    ) {
        if (data.inputField != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.raw("HyChat"),
                    Message.raw(data.inputField),
                    NotificationStyle.Success
            );
            HyChatPlugin.getInstance().getConfig().get().setKoboldUrl(data.inputField);
            HyChatPlugin.getInstance().getConfig().save();
        }
        if (data.action == null) return;

        switch (data.action) {
            case "refresh":
                refreshCount++;
                UICommandBuilder cmd = new UICommandBuilder();
                cmd.set("#StatusText.Text", "Refreshed " + refreshCount + " time(s)!");
                this.sendUpdate(cmd, false);

                NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.raw("HyChat"),
                    Message.raw("Dashboard refreshed!"),
                    NotificationStyle.Success
                );
                break;
            case "openRouterAuth":
                HyChatAPI.authorizeWithOpenRouter();
                NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(),
                        Message.raw("HyChat"),
                        Message.raw("Start OpenRouter Authorization!"),
                        NotificationStyle.Success
                );
                break;
            case "stream":
                boolean stream = !HyChatPlugin.getInstance().getConfig().get().isStreamResponse();
                HyChatPlugin.getInstance().getConfig().get().setStreamResponse(stream);
                HyChatPlugin.getInstance().getConfig().save();

                NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(),
                        Message.raw("HyChat stream set to " + stream),
                        NotificationStyle.Success
                );
                break;

            case "online":
                boolean online = !HyChatPlugin.getInstance().getConfig().get().isOnlineMode();
                HyChatPlugin.getInstance().getConfig().get().setOnlineMode(online);
                HyChatPlugin.getInstance().getConfig().save();

                NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(),
                        Message.raw("HyChat online set to " + online),
                        NotificationStyle.Success
                );
                break;

            case "close":
                this.close();
                break;
        }
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    public static class UIEventData {

        public static final BuilderCodec<UIEventData> CODEC = BuilderCodec.builder(
                UIEventData.class, UIEventData::new
        )
        .addField(new KeyedCodec<>("Action", Codec.STRING), (e, v) -> e.action = v, e -> e.action)
        .addField(new KeyedCodec<>("@InputField", Codec.STRING), (e, v) -> e.inputField = v, e -> e.inputField).build();

        private String action;
        private String inputField;
    }
}