package ru.highloadcup.travels.util;

import com.jsoniter.output.JsonStream;
import ru.highloadcup.travels.service.StorageService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class WarmingUtil {
    private static StorageService storageService = StorageService.INSTANCE;

    private WarmingUtil(){}

    public static void start() {
        try {
            for (int i = 1; i < 10; i++) {
                executeAndPrint("./wrk -c 32 -t 1 -d 1 http://localhost:80/users/" + i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 http://localhost:80/visits/" + i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 http://localhost:80/locations/" + i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 http://localhost:80/users/" + i + "/visits");
                executeAndPrint("./wrk -c 32 -t 1 -d 1 http://localhost:80/locations/" + i + "/avg");
                createPostLua("user", i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 -s post.lua http://localhost:80/users/" + i);
                createPostLua("visit", i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 -s post.lua http://localhost:80/visits/" + i);
                createPostLua("location", i);
                executeAndPrint("./wrk -c 32 -t 1 -d 1 -s post.lua http://localhost:80/locations/" + i);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void createPostLua(String entity, int id) throws IOException {
        File file = new File("post.lua");
        String json = null;
        switch (entity) {
        case "user":
            json = JsonStream.serialize(storageService.findUserById(id));
            break;
        case "location":
            json = JsonStream.serialize(storageService.findLocationById(id));
            break;
        case "visit":
            json = JsonStream.serialize(storageService.findVisitById(id));
        }
        try{
            FileWriter fstream = new FileWriter(file,false);
            fstream.write("wrk.method = \"POST\"\n" +
                    "wrk.body   = \"" + json + "\"\n" +
                    "wrk.headers[\"Content-Type\"] = \"application/json\"");
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void executeAndPrint(String cmd) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
    }
}
