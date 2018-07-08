package client;

import java.util.ArrayList;
import java.util.Random;

public class Styles {
    private ArrayList<String> styles;

    public Styles() {
        styles = new ArrayList<>();
        styles.add((getClass().getResource("/client/css/style1.css")).toExternalForm());
        styles.add((getClass().getResource("/client/css/style2.css")).toExternalForm());
    }

    public String getRandomStyle(){
        return styles.get(new Random().nextInt(styles.size()));
    }
}
