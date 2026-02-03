package kiraririria.hychat.api;

import com.google.gson.JsonObject;
import kiraririria.hychat.core.request.PromptMessage;

import javax.annotation.Nullable;

public class DialogueMessage
{
    public DialogueMessage.Role role;
    public String content;
    @Nullable
    private PromptMessage prompt;



    public DialogueMessage(String content, DialogueMessage.Role role)
    {
        this.content = content;
        this.role = role;
    }

    public JsonObject to() {
        JsonObject message = new JsonObject();
        message.addProperty("role", role.name().toLowerCase());
        message.addProperty("content", content);
        return message;
    }

    public enum Role {
        USER, ASSISTANT
    }
}
