package main.java.Utils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class CardImageLoader {
    private static final String BASE_PATH = "C:\\Users\\Effendi Jabid Kamal\\eclipse-workspace\\UnoCardGameSimulationDesignAndDevelopment\\src\\main\\java\\DataFiles\\UNO Cards\\";
    private static final Map<String, String> cardImagePaths = new HashMap<>();

    static {
        // Load action card images
        cardImagePaths.put("Blue Draw Two", BASE_PATH + "Action\\Blue draw 2.jpg");
        cardImagePaths.put("Blue Reverse", BASE_PATH + "Action\\Blue reverse.jpg");
        cardImagePaths.put("Blue Skip", BASE_PATH + "Action\\Blue skip.jpg");
        cardImagePaths.put("Green Draw Two", BASE_PATH + "Action\\Green draw 2.jpg");
        cardImagePaths.put("Green Reverse", BASE_PATH + "Action\\Green reverse.jpg");
        cardImagePaths.put("Green Skip", BASE_PATH + "Action\\Green skip.jpg");
        cardImagePaths.put("Red Draw Two", BASE_PATH + "Action\\Red draw 2.jpg");
        cardImagePaths.put("Red Reverse", BASE_PATH + "Action\\Red reverse.jpg");
        cardImagePaths.put("Red Skip", BASE_PATH + "Action\\Red skip.jpg");
        cardImagePaths.put("Yellow Draw Two", BASE_PATH + "Action\\Yellow draw 2.jpg");
        cardImagePaths.put("Yellow Reverse", BASE_PATH + "Action\\Yellow reverse.jpg");
        cardImagePaths.put("Yellow Skip", BASE_PATH + "Action\\Yellow skip.jpg");

        // Load number card images
        cardImagePaths.put("Blue 0", BASE_PATH + "Number\\Blue 0.jpg");
        cardImagePaths.put("Blue 1", BASE_PATH + "Number\\Blue 1.jpg");
        cardImagePaths.put("Blue 2", BASE_PATH + "Number\\Blue 2.jpg");
        cardImagePaths.put("Blue 3", BASE_PATH + "Number\\Blue 3.jpg");
        cardImagePaths.put("Blue 4", BASE_PATH + "Number\\Blue 4.jpg");
        cardImagePaths.put("Blue 5", BASE_PATH + "Number\\Blue 5.jpg");
        cardImagePaths.put("Blue 6", BASE_PATH + "Number\\Blue 6.jpg");
        cardImagePaths.put("Blue 7", BASE_PATH + "Number\\Blue 7.jpg");
        cardImagePaths.put("Blue 8", BASE_PATH + "Number\\Blue 8.jpg");
        cardImagePaths.put("Blue 9", BASE_PATH + "Number\\Blue 9.jpg");
        cardImagePaths.put("Green 0", BASE_PATH + "Number\\Green 0.jpg");
        cardImagePaths.put("Green 1", BASE_PATH + "Number\\Green 1.jpg");
        cardImagePaths.put("Green 2", BASE_PATH + "Number\\Green 2.jpg");
        cardImagePaths.put("Green 3", BASE_PATH + "Number\\Green 3.jpg");
        cardImagePaths.put("Green 4", BASE_PATH + "Number\\Green 4.jpg");
        cardImagePaths.put("Green 5", BASE_PATH + "Number\\Green 5.jpg");
        cardImagePaths.put("Green 6", BASE_PATH + "Number\\Green 6.jpg");
        cardImagePaths.put("Green 7", BASE_PATH + "Number\\Green 7.jpg");
        cardImagePaths.put("Green 8", BASE_PATH + "Number\\Green 8.jpg");
        cardImagePaths.put("Green 9", BASE_PATH + "Number\\Green 9.jpg");
        cardImagePaths.put("Red 0", BASE_PATH + "Number\\Red 0.jpg");
        cardImagePaths.put("Red 1", BASE_PATH + "Number\\Red 1.jpg");
        cardImagePaths.put("Red 2", BASE_PATH + "Number\\Red 2.jpg");
        cardImagePaths.put("Red 3", BASE_PATH + "Number\\Red 3.jpg");
        cardImagePaths.put("Red 4", BASE_PATH + "Number\\Red 4.jpg");
        cardImagePaths.put("Red 5", BASE_PATH + "Number\\Red 5.jpg");
        cardImagePaths.put("Red 6", BASE_PATH + "Number\\Red 6.jpg");
        cardImagePaths.put("Red 7", BASE_PATH + "Number\\Red 7.jpg");
        cardImagePaths.put("Red 8", BASE_PATH + "Number\\Red 8.jpg");
        cardImagePaths.put("Red 9", BASE_PATH + "Number\\Red 9.jpg");
        cardImagePaths.put("Yellow 0", BASE_PATH + "Number\\Yellow 0.jpg");
        cardImagePaths.put("Yellow 1", BASE_PATH + "Number\\Yellow 1.jpg");
        cardImagePaths.put("Yellow 2", BASE_PATH + "Number\\Yellow 2.jpg");
        cardImagePaths.put("Yellow 3", BASE_PATH + "Number\\Yellow 3.jpg");
        cardImagePaths.put("Yellow 4", BASE_PATH + "Number\\Yellow 4.jpg");
        cardImagePaths.put("Yellow 5", BASE_PATH + "Number\\Yellow 5.jpg");
        cardImagePaths.put("Yellow 6", BASE_PATH + "Number\\Yellow 6.jpg");
        cardImagePaths.put("Yellow 7", BASE_PATH + "Number\\Yellow 7.jpg");
        cardImagePaths.put("Yellow 8", BASE_PATH + "Number\\Yellow 8.jpg");
        cardImagePaths.put("Yellow 9", BASE_PATH + "Number\\Yellow 9.jpg");

        // Load wild card images
        cardImagePaths.put("Wild", BASE_PATH + "Wild\\Wild.jpg");
        cardImagePaths.put("Wild Draw Four", BASE_PATH + "Wild\\Wild draw 4.jpg");
    }

    public static ImageIcon getCardImage(String cardName) {
        String path = cardImagePaths.get(cardName);
        if (path != null) {
            return new ImageIcon(path);
        }
        return null;
    }
}
