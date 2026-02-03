package kiraririria.hychat.api;

import kiraririria.hychat.core.request.PromptMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ApiGenerationTask
{
    private final UUID taskId;
    private String prompt = "";
    private List<PromptMessage> promptMessages = new ArrayList<>();

    private final long startTime;

    public ApiGenerationTask(UUID taskId, String prompt)
    {
        this.taskId = taskId;
        this.prompt = prompt;
        this.startTime = System.currentTimeMillis();
    }

    public ApiGenerationTask(UUID taskId, List<PromptMessage> promptMessages)
    {
        this.taskId = taskId;
        this.promptMessages = promptMessages;
        this.startTime = System.currentTimeMillis();
    }
    public UUID getTaskId()
    {
        return taskId;
    }

    public String getPrompt()
    {
        return prompt;
    }

    public List<PromptMessage> getPromptMessage()
    {
        return promptMessages;
    }


    public long getStartTime()
    {
        return startTime;
    }
}