package com.redfox.ai_story_generator;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class AIStoryGenerator {
    public static void prevMain(String[] args) {
        if (args.length != 2) { System.out.println("2 input parameters are required"); return; }

        Scanner scanner = new Scanner(System.in);

        ArrayList<String> generatedStories = new ArrayList<>();

        String apiKeyKey = args[0] + "_API_KEY";
        String apiKey = Dotenv.load().get(apiKeyKey);

        AIClient.AIModel aiModel = AIClient.AIModel.fromAIModel(args[0]);
        AIClient.AIVersion aiVersion = AIClient.AIVersion.fromModelName(args[1]);

        System.out.println("\tModel: " + aiModel.toString());
        System.out.println("\tVersion: " + aiVersion.toString());
        System.out.println("\tSelected API Key Key: " + apiKeyKey);

        System.out.println("How many stories are you gonna write: ");
        int storyAmount = new Scanner(System.in).nextInt();
        System.out.println("\tStory amount: " + storyAmount);
        
        System.out.println("Input story type (e.g. kortverhaal) prompt: ");
        String storyType = scanner.nextLine();
        System.out.println("\tStory type: " + storyType);
        if (storyType.equalsIgnoreCase("none")) { storyType = "storie"; }

        System.out.println("Input the word count: ");
        String wordCount = scanner.nextLine();
        System.out.println("\tWord count: " + wordCount);
        if (wordCount.equalsIgnoreCase("none")) { wordCount = null; }

        System.out.println("Input the age: ");
        String age = scanner.nextLine();
        System.out.println("\tAge: " + age);
        if (age.equalsIgnoreCase("none")) { age = null; }

        System.out.println("Input the achievement level (e.g. 40% vir Afr kry): ");
        String achievementLevel = scanner.nextLine();
        System.out.println("\tAchievement level: " + achievementLevel);
        if (achievementLevel.equalsIgnoreCase("none")) { achievementLevel = null; }

        System.out.println("Input the topic: ");
        String topic = scanner.nextLine();
        System.out.println("\tTopic: " + topic);
        if (topic.equalsIgnoreCase("none")) { topic = null; }

        System.out.println("Do you want a title? (type 'Y' or 'N'): ");
        boolean hasTitle = new Scanner(System.in).next().equalsIgnoreCase("y");

        //Make middle sentence
        String wordClause;
        if (wordCount != null) { wordClause = wordCount + " woorde he"; } else wordClause = "";
        String ageAchievementClause;
        if (age != null) {
            String ageClause = ", geskryf word soos wat iemand wat " + age + " is ";
            if (achievementLevel != null) {
                ageAchievementClause = ageClause + "wat " + achievementLevel + ", sou";
            } else ageAchievementClause = ageClause + "sou";
        } else if (age == null && achievementLevel != null) {
            ageAchievementClause = ", geskryf word soos wat iemand wat " + achievementLevel + ", sou";
        } else ageAchievementClause = "";
        String topicClause;
        if (topic != null && ageAchievementClause.isEmpty()) {
            topicClause = ", oor " + topic + " gaan";
        } else if (topic != null && (!ageAchievementClause.isEmpty())) {
            topicClause = ". Dit moet oor " + topic + " gaan";
        } else topicClause = "";
        String titleSentence;
        if (hasTitle) { titleSentence = ". Sit 'n titel aan die bokant"; } else titleSentence = "";

        String middleSentence;
        if ((!wordClause.isEmpty()) || (!ageAchievementClause.isEmpty()) || (!topicClause.isEmpty()) || (!titleSentence.isEmpty()))  {
            middleSentence = " Dit moet " + wordClause
                    + ageAchievementClause
                    + topicClause
                    + titleSentence + ".";
        } else middleSentence = "";

        //Make prompt
        String prompt = "Skryf 'n " + storyType + " in Afr."
                + middleSentence
                + " Gee net die storie in die output, niks anders nie.";

        System.out.println("\nPrompt: " + prompt);

        //Generate output
        System.out.println("Is this correct (type 'Y' or 'N'): ");
        if (new Scanner(System.in).next().equalsIgnoreCase("y")) {
            for (int i = 0; i < storyAmount; i++) {
                String story = generateStory(prompt, apiKey, aiModel, aiVersion);
                System.out.println("\n" + story);
                generatedStories.add(story);
            }
        } else {
            System.out.println("Enter new prompt: ");
            prompt = scanner.nextLine() + " Gee net die storie in die output, niks anders nie.";
            System.out.println("New prompt: " + prompt);

            for (int i = 0; i < storyAmount; i++) {
                String story = generateStory(prompt, apiKey, aiModel, aiVersion);
                System.out.println("\n" + story);
                generatedStories.add(story);
            }
        }

        for (String generatedStory : generatedStories) {
            try {
                LocalDateTime now = LocalDateTime.now();

                int mins = Math.round(now.getMinute() / 5.0f) * 5;
                if (mins == 60) {
                    now = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                } else now = now.withMinute(mins).withSecond(0).withNano(0);
                String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

                Path genPath = Paths.get("generated_stories_" + aiModel.toString().toLowerCase() + "/" + time + "/" + prompt + "_" + generatedStories.indexOf(generatedStory) + ".txt");
                if (Files.notExists(genPath.getParent())) {
                    Files.createDirectories(genPath.getParent());
                }

                Files.writeString(genPath, generatedStory, StandardCharsets.UTF_8);

                System.out.println("Story written to: " + genPath.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* output:
        Write a short story from 40 - 60 words about a fairy:
{
  "id": "chatcmpl-Bm11FSR5t1xr6Mc6sSlM8jjYw3e46",
  "object": "chat.completion",
  "created": 1750783361,
  "model": "gpt-4o-mini-2024-07-18",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "In a forgotten garden, a tiny fairy named Lira danced among the wildflowers. With each flutter of her iridescent wings, she painted the petals with colors unseen. One evening, a lonely child discovered her secret. Together they laughed, and Lira's magic blossomed, bringing joy to both their worlds, intertwining their hearts forever.",
        "refusal": null,
        "annotations": []
      },
      "logprobs": null,
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 21,
    "completion_tokens": 70,
    "total_tokens": 91,
    "prompt_tokens_details": {
      "cached_tokens": 0,
      "audio_tokens": 0
    },
    "completion_tokens_details": {
      "reasoning_tokens": 0,
      "audio_tokens": 0,
      "accepted_prediction_tokens": 0,
      "rejected_prediction_tokens": 0
    }
  },
  "service_tier": "default",
  "system_fingerprint": "fp_34a54ae93c"
}*/
    }
    static String generateStory(String prompt, String apiKey, AIClient.AIModel aiModel, AIClient.AIVersion aiVersion) {
        try {
            String response = switch (aiModel) {
                case CHAT_GPT -> AIClient.extractChatGPTContent(AIClient.askAI(prompt, apiKey, aiModel, aiVersion));
                case CLAUDE -> null;
                case GEMINI -> AIClient.extractGeminiContent(AIClient.askAI(prompt, apiKey, aiModel, aiVersion));
            };
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
