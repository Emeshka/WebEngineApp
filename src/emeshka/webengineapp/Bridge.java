package emeshka.webengineapp;
import javafx.scene.web.PromptData;

/**
 * Created by Alexandra on 31.05.2018.
 */

public interface Bridge {
    Application mainApp = null;

    void setMainApp(Application mainApp);
    void alert(String s);
    boolean confirm(String s);
    String prompt(String s);
    String prompt(PromptData pd);
    void exit();
}