package ru.highloadcup.travels;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.DecodingMode;
import com.jsoniter.spi.JsoniterSpi;
import org.rapidoid.net.Server;
import ru.highloadcup.travels.entity.UsersVisit;
import ru.highloadcup.travels.json.LocationDecoder;
import ru.highloadcup.travels.json.LocationEncoder;
import ru.highloadcup.travels.json.UserDecoder;
import ru.highloadcup.travels.json.UserEncoder;
import ru.highloadcup.travels.json.UsersVisitEncoder;
import ru.highloadcup.travels.json.VisitDecoder;
import ru.highloadcup.travels.entity.Location;
import ru.highloadcup.travels.entity.User;
import ru.highloadcup.travels.entity.Visit;
import ru.highloadcup.travels.json.VisitEncoder;
import ru.highloadcup.travels.server.TravelsServer;
import ru.highloadcup.travels.service.StorageService;
import ru.highloadcup.travels.util.WarmingUtil;
import java.io.File;

public class TravelsApplication {
    private static final String LOCAL_PATH = new File("src/main/resources/data").getAbsolutePath();
    private static final String PROD_PATH = "/data/";

    private static StorageService storageService = StorageService.INSTANCE;

    public static void main(String[] args) {
        Server server = new TravelsServer().listen(80);

        JsonIterator.setMode(DecodingMode.DYNAMIC_MODE_AND_MATCH_FIELD_WITH_HASH);
        JsonStream.setMode(EncodingMode.DYNAMIC_MODE);
        JsoniterSpi.registerTypeDecoder(User.class, new UserDecoder());
        JsoniterSpi.registerTypeEncoder(User.class, new UserEncoder());
        JsoniterSpi.registerTypeDecoder(Location.class, new LocationDecoder());
        JsoniterSpi.registerTypeEncoder(Location.class, new LocationEncoder());
        JsoniterSpi.registerTypeDecoder(Visit.class, new VisitDecoder());
        JsoniterSpi.registerTypeEncoder(Visit.class, new VisitEncoder());
        JsoniterSpi.registerTypeEncoder(UsersVisit.class, new UsersVisitEncoder());

        //Read initial data
        System.out.println("Reading initial data...");
        long start = System.nanoTime();
        if (args[0].equals("local")) storageService.readInitData(LOCAL_PATH);
        else if (args[0].equals("prod")) storageService.readInitData(PROD_PATH);
        long end = System.nanoTime();
        long microseconds = (end - start) / 1000000;
        System.out.println("Data read in: " + microseconds + " ms");

        //Warming up
        if (args[0].equals("prod")) {
            System.out.println("Starting warming up...");
            start = System.nanoTime();
            WarmingUtil.start();
            end = System.nanoTime();
            microseconds = (end - start) / 1000000;
            System.out.println("Warming up finished in: " + microseconds + " ms");
            System.gc();
        }
    }


}