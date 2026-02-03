package kiraririria.hychat.api;

import kiraririria.hychat.HyChatPlugin;
import kiraririria.hychat.core.GenerationHandler;
import kiraririria.hychat.core.auth.OpenRouterAuth;
import kiraririria.hychat.core.models.IModel;
import kiraririria.hychat.core.models.KoboldAI;
import kiraririria.hychat.core.models.OpenRouterModel;
import kiraririria.hychat.core.request.PromptMessage;
import kiraririria.hychat.core.utils.EmojiUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HyChatAPI
{
    private static final Map<UUID, ApiGenerationTask> activeTasks = new ConcurrentHashMap<>();
    private static final Map<UUID, String> results = new ConcurrentHashMap<>();

    public static UUID runGeneration(String prompt, Consumer<String> onUpdate, Consumer<String> onComplete)
    {
        AtomicReference<String> result = new AtomicReference<>("");
        Consumer<String> newUpdate = (s)->{
            onUpdate.accept(s);
            result.set(s);
        };
        Runnable newComplete = ()-> onComplete.accept(result.get());
        return runGeneration(prompt, newUpdate, newComplete);
    }


    public static UUID runGeneration(String prompt, Consumer<String> onUpdate, Runnable onComplete)
    {
        try
        {
            UUID taskId = UUID.randomUUID();
            GenerationHandler.runGeneration(
                    taskId,
                    prompt,
                    updatedText ->
                    {
                        String cleanText = cleanGeneratedText(updatedText);
                        results.put(taskId, cleanText);
                        if (onUpdate != null) onUpdate.accept(cleanText);
                    },
                    () ->
                    {
                        activeTasks.remove(taskId);
                        if (onComplete != null) onComplete.run();
                    }
            );
            activeTasks.put(taskId, new ApiGenerationTask(taskId, prompt));
            return taskId;

        }
        catch (Exception e)
        {
            System.out.println("Ошибка запуска простой генерации: " + e.getMessage());
            return null;
        }
    }

    public static UUID runGeneration(List<PromptMessage> messages, Consumer<String> onUpdate, Consumer<String> onComplete)
    {
        AtomicReference<String> result = new AtomicReference<>("");
        Consumer<String> newUpdate = (s)->{
            onUpdate.accept(s);
            result.set(s);
        };
        Runnable newComplete = ()-> onComplete.accept(result.get());
        return runGeneration(messages, newUpdate, newComplete);
    }


    public static UUID runGeneration(List<PromptMessage> messages, Consumer<String> onUpdate, Runnable onComplete)
    {
        try
        {
            UUID taskId = UUID.randomUUID();
            GenerationHandler.runGeneration(
                    taskId,
                    messages,
                    updatedText ->
                    {
                        String cleanText = cleanGeneratedText(updatedText);
                        results.put(taskId, cleanText);
                        if (onUpdate != null) onUpdate.accept(cleanText);
                    },
                    () ->
                    {
                        activeTasks.remove(taskId);
                        if (onComplete != null) onComplete.run();
                    }
            );
            activeTasks.put(taskId, new ApiGenerationTask(taskId, messages));
            return taskId;

        }
        catch (Exception e)
        {
            System.out.println("Ошибка запуска простой генерации: " + e.getMessage());
            return null;
        }
    }



    private static String cleanGeneratedText(String text)
    {
        if (text == null || text.isEmpty())
        {
            return "";
        }
        text = text.replaceAll("^\\[R\\]", "");
        text = text.replaceAll("\\{\\{js:[^}]*\\}\\}", "");
        text = text.replaceAll("\\{\\{emo:[^}]*\\}\\}", "");
        text = text.trim();
        text = EmojiUtil.cleanup(text);
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    public static IModel getModel()
    {
        if (HyChatPlugin.getInstance().getConfig().get().isOnlineMode())
        {
            return OpenRouterModel.getInstance();
        }
        else
        {
            return KoboldAI.getInstance();
        }
    }

    public static void authorizeWithOpenRouter()
    {
        new Thread(() ->
        {
            try
            {
                OpenRouterAuth.main(new String[] {});
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }).start();
    }
}
