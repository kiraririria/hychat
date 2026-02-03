package kiraririria.hychat.interactions;



import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.OpenCustomUIInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.ui.HyChatDialogueUI;

import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCDialogInteractionSupplier implements OpenCustomUIInteraction.CustomPageSupplier {
    private final HyChatPlugin plugin;

    public NPCDialogInteractionSupplier(HyChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Nullable
    public CustomUIPage tryCreate(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor, @Nonnull PlayerRef playerRef, @Nonnull InteractionContext context) {
        Ref<EntityStore> targetRef = context.getTargetEntity();
        if (targetRef == null || !targetRef.isValid()) {
            InteractionSyncData chainData = context.getClientState();
            if (chainData != null && chainData.entityId >= 0) {
                CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
                if (commandBuffer != null) {
                    targetRef = commandBuffer.getStore().getExternalData().getRefFromNetworkId(chainData.entityId);
                }
            }
        }

        if (targetRef != null && targetRef.isValid()) {
            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
            if (commandBuffer == null) {
                return null;
            } else {
                UUIDComponent uuidComponent = commandBuffer.getComponent(targetRef, UUIDComponent.getComponentType());
                if (uuidComponent == null) {
                    return null;
                } else {
                    UUID entityUuid = uuidComponent.getUuid();
                    NPCEntity npc = targetRef.getStore().getComponent(targetRef, NPCEntity.getComponentType());
                    return new HyChatDialogueUI(playerRef, npc);
                }
            }
        } else {
            return null;
        }
    }
}
