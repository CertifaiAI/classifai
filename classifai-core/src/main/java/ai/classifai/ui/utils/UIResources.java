package ai.classifai.ui.utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.Objects;

@Slf4j
public class UIResources {
    private static final String BUTTON_PATH = "/console/";
    private static final String ICON_PATH = "/icon/";
    private static final String FAVICON_FILE_NAME = "Classifai_Favicon_Light_BG.jpg";
    private static final String FAVICON_DARK_FILE_NAME = "Classifai_Favicon_Dark_32px.png";
    private static final String BACKGROUND_FILE_NAME = "Classifai_Welcome_Handler.jpg";
    private static final String OPEN_BUTTON_FILE_NAME = "Open_Button.png";
    private static final String CONFIG_BUTTON_FILE_NAME = "Config_Button.png";
    private static final String LOG_BUTTON_FILE_NAME = "Log_Button.png";
    private static final String RED_LIGHT_FILE_NAME = "RedLight.png";
    private static final String GREEN_LIGHT_FILE_NAME = "GreenLight.png";


    public static Image getClassifaiIcon() { return getButton(FAVICON_FILE_NAME); }
    public static Image getDarkClassifaiIcon() { return getIcon(FAVICON_DARK_FILE_NAME); }
    public static Image getBackground() { return getButton(BACKGROUND_FILE_NAME); }
    public static Image getOpenButton() { return getButton(OPEN_BUTTON_FILE_NAME); }
    public static Image getConfigButton() { return getButton(CONFIG_BUTTON_FILE_NAME); }
    public static Image getLogButton() { return getButton(LOG_BUTTON_FILE_NAME); }
    public static Image getRedLight() { return getButton(RED_LIGHT_FILE_NAME); }
    public static Image getGreenLight() { return getButton(GREEN_LIGHT_FILE_NAME); }

    @SneakyThrows
    private static Image getButton(String imgName){
        return ImageIO.read(Objects.requireNonNull(UIResources.class.getResource(BUTTON_PATH+ imgName)));
    }

    @SneakyThrows
    private static Image getIcon(String imgName){
        return ImageIO.read(Objects.requireNonNull(UIResources.class.getResource(ICON_PATH+ imgName)));
    }
}
