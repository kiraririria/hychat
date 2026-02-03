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
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.core.models.OpenRouterModel;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModelSelectorUI extends InteractiveCustomUIPage<ModelSelectorUI.SearchGuiData> {

    private String searchQuery = "";
    private final List<OpenRouterModel.ModelDetail> visibleItems;
    private final List<OpenRouterModel.ModelDetail> players;


    public ModelSelectorUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, SearchGuiData.CODEC);
        this.searchQuery = "";
        this.visibleItems = new ArrayList<>();
        try
        {
            this.players = OpenRouterModel.getInstance().fetchModelsWithDetails();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("hychat/ModelSelector.ui");
        uiCommandBuilder.set("#SearchInput.Value", this.searchQuery);

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
        this.buildList(ref, uiCommandBuilder, uiEventBuilder, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull SearchGuiData data) {
        super.handleDataEvent(ref, store, data);
        var playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        var player = store.getComponent(ref, Player.getComponentType());

        if (data.button != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.raw("HyChat Model Update"),
                    Message.raw(data.uuid),
                    NotificationStyle.Success
            );
            HyChatPlugin.getInstance().getConfig().get().setOpenrouterModel(data.uuid);
            HyChatPlugin.getInstance().getConfig().save();
        }

        if (data.searchQuery != null) {
            this.searchQuery = data.searchQuery.trim().toLowerCase();
        }
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        this.buildList(ref, commandBuilder, eventBuilder, store);
        this.sendUpdate(commandBuilder, eventBuilder, false);
    }

    private void buildList(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder,
                           @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {

        String currentModel = HyChatPlugin.getInstance().getConfig().get().getOpenrouterModel();

        if (this.searchQuery.isEmpty()) {
            visibleItems.clear();
            visibleItems.addAll(players);
            visibleItems.sort((a, b) -> {
                boolean aIsCurrent = a.id.equals(currentModel);
                boolean bIsCurrent = b.id.equals(currentModel);

                if (aIsCurrent && !bIsCurrent) return -1;
                if (!aIsCurrent && bIsCurrent) return 1;
                return a.id.compareToIgnoreCase(b.id); // остальные по алфавиту
            });
        } else {
            visibleItems.clear();
            for (OpenRouterModel.ModelDetail entry : players) {
                if (entry.id.toLowerCase().contains(this.searchQuery.toLowerCase())||formatPricingInfo(entry).contains(this.searchQuery.toLowerCase())) {
                    visibleItems.add(entry);
                }
            }
            visibleItems.sort((a, b) -> {
                boolean aIsCurrent = a.id.equals(currentModel);
                boolean bIsCurrent = b.id.equals(currentModel);

                if (aIsCurrent && !bIsCurrent) return -1;
                if (!aIsCurrent && bIsCurrent) return 1;
                return a.id.compareToIgnoreCase(b.id);
            });
        }

        this.buildButtons(visibleItems, commandBuilder, eventBuilder, store);
    }

    @Override
    protected void close() {
        super.close();
    }

    private void buildButtons(List<OpenRouterModel.ModelDetail> items, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.clear("#IndexElements");
        var cur = HyChatPlugin.getInstance().getConfig().get().getOpenrouterModel();
        uiCommandBuilder.set("#CurrentModel.Text", cur);
        uiCommandBuilder.appendInline("#Main #IndexList", "Group #IndexElements { LayoutMode: Left; }");
        var i = 0;
        for (OpenRouterModel.ModelDetail name : items) {

            uiCommandBuilder.append("#IndexElements", "hychat/ModelEntry.ui");
            eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#IndexElements[" + i + "]", EventData.of("Button", "ToggleExpanded").append("UUID", name.id), false);
            uiCommandBuilder.set("#IndexElements[" + i + "] #MemberName.Text", name.id);
            uiCommandBuilder.set("#IndexElements[" + i + "] #PromptPrice.Text",formatPricingInfo(name));
            uiCommandBuilder.set("#IndexElements[" + i + "] #ContextLength.Text", formatContextLength(name));

            if (Objects.equals(name.id, cur))
            {
                uiCommandBuilder.set("#IndexElements[" + i + "] #MemberName.Style.TextColor","#00FF14");
            }

            ++i;
        }
    }
    private String formatPricingInfo(OpenRouterModel.ModelDetail detail)
    {
        if (detail.promptPrice > 0 || detail.completionPrice > 0)
        {
            double promptPerThousand = detail.promptPrice * 1000;
            double completionPerThousand = detail.completionPrice * 1000;

            if (promptPerThousand < 0.01 && completionPerThousand < 0.01)
            {
                return String.format("$%.5f/$%.5f per 1K", promptPerThousand, completionPerThousand);
            }
            else
            {
                return String.format("$%.3f/$%.3f per 1K", promptPerThousand, completionPerThousand);
            }
        }
        else
        {
            return "Free";
        }
    }
    private String formatContextLength(OpenRouterModel.ModelDetail detail)
    {
        if (detail.contextLength > 0)
        {
            if (detail.contextLength >= 1000000)
            {
                return String.format("%.1fM tokens", detail.contextLength / 1000000.0);
            }
            else if (detail.contextLength >= 1000)
            {
                return String.format("%.0fK tokens", detail.contextLength / 1000.0);
            }
            else
            {
                return detail.contextLength + " tokens";
            }
        }
        else
        {
            return "Context not available";
        }
    }
    public static class SearchGuiData {
        static final String KEY_BUTTON = "Button";
        static final String KEY_NAVBAR = "NavBar";
        static final String KEY_UUID = "UUID";
        static final String KEY_SEARCH_QUERY = "@SearchQuery";
        static final String KEY_DROPDOWN_VALUE_QUERY = "@DropdownValue";

        public static final BuilderCodec<SearchGuiData> CODEC = BuilderCodec.builder(SearchGuiData.class, SearchGuiData::new)
                .addField(new KeyedCodec<>(KEY_SEARCH_QUERY, Codec.STRING), (searchGuiData, s) -> searchGuiData.searchQuery = s, searchGuiData -> searchGuiData.searchQuery)
                .addField(new KeyedCodec<>(KEY_UUID, Codec.STRING), (searchGuiData, s) -> searchGuiData.uuid = s, searchGuiData -> searchGuiData.uuid)
                .addField(new KeyedCodec<>(KEY_BUTTON, Codec.STRING), (searchGuiData, s) -> searchGuiData.button = s, searchGuiData -> searchGuiData.button)
                .addField(new KeyedCodec<>(KEY_DROPDOWN_VALUE_QUERY, Codec.STRING), (searchGuiData, s) -> searchGuiData.dropdownValue = s, searchGuiData -> searchGuiData.dropdownValue)
                .addField(new KeyedCodec<>(KEY_NAVBAR, Codec.STRING), (searchGuiData, s) -> searchGuiData.navbar = s, searchGuiData -> searchGuiData.navbar)
                .build();

        private String button;
        private String searchQuery;
        private String uuid;
        private String dropdownValue;
        private String navbar;

    }
}
