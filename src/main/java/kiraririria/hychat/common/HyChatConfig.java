package kiraririria.hychat.common;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class HyChatConfig
{
    public static final BuilderCodec<HyChatConfig> CODEC = BuilderCodec.builder(HyChatConfig.class, HyChatConfig::new)
            .append(new KeyedCodec<String>("OpenrouterKey", Codec.STRING),
                    (config, value) -> config.openrouterKey = value,
                    (config) -> config.openrouterKey).add()
            .append(new KeyedCodec<String>("OpenrouterModel", Codec.STRING),
                    (config, value) -> config.openrouterModel = value,
                    (config) -> config.openrouterModel).add()
            .append(new KeyedCodec<Boolean>("OnlineMode", Codec.BOOLEAN),
                    (config, value) -> config.onlineMode = value,
                    (config) -> config.onlineMode).add()
            .append(new KeyedCodec<Boolean>("StreamResponse", Codec.BOOLEAN),
                    (config, value) -> config.streamResponse = value,
                    (config) -> config.streamResponse).add()
            .append(new KeyedCodec<Integer>("ModelProvider", Codec.INTEGER),
                    (config, value) -> config.modelProvider = value,
                    (config) -> config.modelProvider).add()
            .append(new KeyedCodec<Integer>("ChatLen", Codec.INTEGER),
                    (config, value) -> config.chatLen = value,
                    (config) -> config.chatLen).add()
            .build();

    private String openrouterKey = "";
    private String openrouterModel = "openrouter/auto";
    private String koboldUrl = "http://localhost:5001";

    private int modelProvider = 0;


    private boolean playground = true;
    private boolean streamResponse = true;
    private boolean onlineMode = true;

    private int chatLen = 30;

    public String getOpenrouterModel()
    {
        return openrouterModel;
    }

    public void setOpenrouterModel(String openrouterModel)
    {
        this.openrouterModel = openrouterModel;
    }

    public boolean isOnlineMode()
    {
        return onlineMode;
    }

    public void setOnlineMode(boolean onlineMode)
    {
        this.onlineMode = onlineMode;
    }

    public boolean isStreamResponse()
    {
        return streamResponse;
    }

    public void setStreamResponse(boolean streamResponse)
    {
        this.streamResponse = streamResponse;
    }

    public String getOpenrouterKey()
    {
        return openrouterKey;
    }


    public HyChatConfig()
    {
    }

    public void setOpenrouterKey(String openrouterKey)
    {
        this.openrouterKey = openrouterKey;
    }

    public String getKoboldUrl()
    {
        return koboldUrl;
    }

    public void setKoboldUrl(String koboldUrl)
    {
        this.koboldUrl = koboldUrl;
    }

    public boolean isPlayground()
    {
        return playground;
    }

    public void setPlayground(boolean playground)
    {
        this.playground = playground;
    }

    public int getModelProvider()
    {
        return modelProvider;
    }

    public void setModelProvider(int modelProvider)
    {
        this.modelProvider = modelProvider;
    }

    public int getChatLen()
    {
        return chatLen;
    }

    public void setChatLen(int chatLen)
    {
        this.chatLen = chatLen;
    }
}