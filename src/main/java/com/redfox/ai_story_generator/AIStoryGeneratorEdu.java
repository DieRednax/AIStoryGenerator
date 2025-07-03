package com.redfox.ai_story_generator;

import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import static com.redfox.ai_story_generator.AIStoryGenerator.generateStory;

public class AIStoryGeneratorEdu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, String> generatedStories = new HashMap<>();

        //model
        System.out.println("Nuwer of Ouer model: ");
        String modelAge = scanner.nextLine().toLowerCase();

        AIClient.AIModel aiModel = AIClient.AIModel.CHAT_GPT;
        AIClient.AIVersion aiVersion;
        if (modelAge.contains("ou")) {
            aiVersion = AIClient.AIVersion.GPT_3_5_TURBO;
            modelAge = "ou model";
        } else {
            aiVersion = AIClient.AIVersion.GPT_4_1;
            modelAge = "nuwe model";
        }

        String apiKey = Dotenv.load().get(aiModel.toString() + "_API_KEY");

        System.out.println("\tAI Model: " + aiModel.toString());
        System.out.println("\tAI Version: " + aiVersion.toString());
        System.out.println("\tAPI Key Key: " + aiModel + "_API_KEY");

        //story count
        System.out.println("Hoeveel stories wil jy genereer: ");
        int storyCount = new Scanner(System.in).nextInt();
        System.out.println("\tStory count: " + storyCount);

        //prompt (asking)

        System.out.println("Gaan hierdie 'n eenvoudige of komplekse storie wees: ");
        String storyType = new Scanner(System.in).next().toLowerCase();
        System.out.println("\tStorie tipe: " + storyType);

        System.out.println("Watse graad groep is dit in geskryf (4 - 6, 7 - 9, 10 - 12): ");
        String grade = scanner.nextLine().replace("gr", "").replace("graad", "");
        System.out.println("\tGraad groep: " + grade);

        //asking ai
        String achievement = "";
        for (int i = 0; i < storyCount; i++) {
            //promt (building)
            String prompt = "";
            String theme = "none";
            if (storyType.contains("eenvoudig")) {
                prompt = "Skryf 'n storie in Afrikaans van ten minste 500 woorde. Skyf dit soos wat 'n graad "
                        + grade + " sou.";
            } else {
                ArrayList<String> themes;
                InputStream themesFile = AIStoryGeneratorEdu.class.getClassLoader().getResourceAsStream("ai_story_generator/themes.json");
                if (themesFile != null) {
                    Gson gson = new Gson();
                    InputStreamReader ir = new InputStreamReader(themesFile, StandardCharsets.UTF_8);
                    themes = gson.fromJson(ir, ArrayList.class);
                } else {
                    System.out.println("returning");
                    return;
                }

                int theme1Index = new Random().nextInt(themes.size());
                int theme2Index = new Random().nextInt(themes.size());
                while (theme1Index == theme2Index) {
                    theme2Index = new Random().nextInt(themes.size());
                }
                String theme1 = themes.get(theme1Index);
                String theme2 = themes.get(theme2Index);
                theme = theme1 + " en " + theme2;

                if (achievement.isEmpty()) {
                    System.out.println("Wil jy die prestasie van 70% vir Afr na iets anders verander (tik 'N' of 'J'): ");
                    boolean bAchievementChange = new Scanner(System.in).next().equalsIgnoreCase("j");
                    achievement = "70%";
                    if (bAchievementChange) {
                        System.out.println("Ander prestasie (tik net die persent bv 60%): ");
                        achievement = new Scanner(System.in).next();
                    }
                }

                prompt = "Skryf 'n storie in Afrikaans van ten minste 500 woorde oor " + theme1 + " en " + theme2 + "."
                        + " Ek kry " + achievement + " vir Afr en is in graad " + grade + ", "
                        + "so pas my storie daarvolgens in woordspraak, spelling, sinskonstruksie en eenvoudigheid aan.";
            }
            prompt = prompt + " Gee net die storie in die uitset, so geen titel nie.";

            System.out.println("Prompt: " + prompt);

            String story = generateStory(prompt, apiKey, aiModel, aiVersion);
            System.out.println("\n" + story);
            generatedStories.put(story, theme);
        }
        if (!achievement.isEmpty()) achievement = ", " + achievement;

        int i = 0;
        for (String story : generatedStories.keySet()) {
            try {
                LocalDateTime now = LocalDateTime.now();

                int mins = Math.round(now.getMinute() / 5.0f) * 5;
                if (mins == 60) {
                    now = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
                } else now = now.withMinute(mins).withSecond(0).withNano(0);
                String time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));

                Path genPath = Paths.get("edu_generated_stories" + "/" + time + "/" + storyType + ", Gr " + grade + achievement + ", " + modelAge + ", " + generatedStories.get(story) + "_" + i + ".txt");
                if (Files.notExists(genPath.getParent())) {
                    Files.createDirectories(genPath.getParent());
                }

                Files.writeString(genPath, story, StandardCharsets.UTF_8);

                System.out.println("Story written to: " + genPath.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }
    }
}
