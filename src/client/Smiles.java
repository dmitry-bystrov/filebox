package client;

import javafx.scene.image.Image;

import java.util.TreeMap;

public class Smiles {
    private TreeMap<String, Image> smileMap;

    public Smiles() {
        smileMap = new TreeMap<>();
        smileMap.put(":)", new Image(getClass().getResourceAsStream("/client/assets/emoji/Slightly_Smiling_Face_Emoji_Icon_42x42.png")));
        smileMap.put(":(", new Image(getClass().getResourceAsStream("/client/assets/emoji/Very_sad_emoji_icon_png_42x42.png")));
        smileMap.put(";)", new Image(getClass().getResourceAsStream("/client/assets/emoji/Wink_Emoji_42x42.png")));
        smileMap.put(":D", new Image(getClass().getResourceAsStream("/client/assets/emoji/Smiling_Face_Emoji_Icon_42x42.png")));
        smileMap.put("XD", new Image(getClass().getResourceAsStream("/client/assets/emoji/Smiling_Face_with_Tightly_Closed_eyes_Icon_42x42.png")));
        smileMap.put(":anguished:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Anguished_Face_Emoji_42x42.png")));
        smileMap.put(":flushed:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Flushed_Face_Emoji_42x42.png")));
        smileMap.put(":heart_eyes:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Heart_Eyes_Emoji_42x42.png")));
        smileMap.put(":loudly_crying:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Loudly_Crying_Face_Emoji_42x42.png")));
        smileMap.put(":nerd:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Nerd_with_Glasses_Emoji_42x42.png")));
        smileMap.put(":|", new Image(getClass().getResourceAsStream("/client/assets/emoji/Neutral_Face_Emoji_42x42.png")));
        smileMap.put(":omg:", new Image(getClass().getResourceAsStream("/client/assets/emoji/OMG_Face_Emoji_42x42.png")));
        smileMap.put(":shyly:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Shyly_Smiling_Face_Emoji_42x42.png")));
        smileMap.put(":sleeping:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Sleeping_Emoji_42x42.png")));
        smileMap.put(":devil:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Smiling_Devil_Emoji_42x42.png")));
        smileMap.put(":halo:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Smiling_Face_with_Halo_42x42.png")));
        smileMap.put(":sweat:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Smiling_with_Sweat_Emoji_Icon_42x42.png")));
        smileMap.put("B)", new Image(getClass().getResourceAsStream("/client/assets/emoji/Sunglasses_Emoji_42x42.png")));
        smileMap.put(":joy:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Tears_of_Joy_Emoji_Icon_42x42.png")));
        smileMap.put(":thinking:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Thinking_Face_Emoji_42x42.png")));
        smileMap.put(":updown:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Upside-Down_Face_Emoji_Icon_42x42.png")));
        smileMap.put(":angry:", new Image(getClass().getResourceAsStream("/client/assets/emoji/Very_Angry_Emoji_42x42.png")));
   }

    public TreeMap<String, Image> getSmiles() {
        return smileMap;
    }
}
