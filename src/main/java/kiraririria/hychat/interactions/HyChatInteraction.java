package kiraririria.hychat.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.FormattedMessage;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import kiraririria.hychat.api.HyChatAPI;
import kiraririria.hychat.ui.HyChatDialogueUI;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

public class HyChatInteraction extends SimpleInstantInteraction
{
    public static final BuilderCodec<HyChatInteraction> CODEC = BuilderCodec.builder(
            HyChatInteraction.class, HyChatInteraction::new, SimpleInstantInteraction.CODEC
    ).build();
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
        Ref<EntityStore> playerRef = interactionContext.getEntity();
        Ref<EntityStore> npcRef = interactionContext.getTargetEntity();

        if (npcRef == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("Target entity (NPC) is null");
            return;
        }

        try {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) {
                LOGGER.atWarning().log("Player component not found");
                return;
            }

            PlayerRef playerRefComponent = store.getComponent(playerRef, PlayerRef.getComponentType());
            if  (playerRefComponent == null) {
                LOGGER.atWarning().log("PlayerRef is null");
                return;
            }

            Store<EntityStore> npcstore = npcRef.getStore();
            NPCEntity npc = npcstore.getComponent(npcRef, NPCEntity.getComponentType());
            HyChatDialogueUI modelSelectorPage = new HyChatDialogueUI(playerRefComponent, npc);
            player.getPageManager().openCustomPage(playerRef, store, modelSelectorPage);

        } catch (Exception e) {
            LOGGER.atWarning().log("Error opening chat UI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}