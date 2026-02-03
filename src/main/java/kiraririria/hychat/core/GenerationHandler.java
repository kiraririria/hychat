package kiraririria.hychat.core;


import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.core.request.PromptMessage;
import kiraririria.hychat.core.request.RequestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class GenerationHandler
{
    private static final Map<UUID, GenerationTask> activeGenerations = new ConcurrentHashMap<>();
    public static boolean hasRunningTasks()
    {
        return !activeGenerations.isEmpty();
    }

    public static void runGeneration(UUID dialogueId, List<PromptMessage> messages, Consumer<String> onUpdate, Runnable onComplete, boolean stream)
    {
        GenerationTask task = new GenerationTask(RequestBuilder.build(messages,stream), onUpdate, onComplete,stream);
        activeGenerations.put(dialogueId, task);
        task.start();
    }
    public static void runGeneration(UUID dialogueId, String prompt, Consumer<String> onUpdate, Runnable onComplete, boolean stream)
    {
        List<PromptMessage> messages = Collections.singletonList(new PromptMessage(prompt, PromptMessage.Role.SYSTEM));
        runGeneration(dialogueId, messages, onUpdate, onComplete, stream);
    }

    public static void runGeneration(UUID dialogueId, String prompt, Consumer<String> onUpdate, Runnable onComplete)
    {
        runGeneration(dialogueId, prompt, onUpdate, onComplete, HyChatPlugin.getInstance().getConfig().get().isStreamResponse());
    }
    public static void runGeneration(UUID dialogueId, List<PromptMessage> messages, Consumer<String> onUpdate, Runnable onComplete)
    {
        runGeneration(dialogueId, messages, onUpdate, onComplete, HyChatPlugin.getInstance().getConfig().get().isStreamResponse());
    }
}
