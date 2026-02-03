package kiraririria.hychat.core.request;

import com.google.gson.JsonObject;

public class PromptMessage
{
    public Role role;
    public String content;

    public PromptMessage(String content, Role role)
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
        SYSTEM, USER, ASSISTANT
    }
}
