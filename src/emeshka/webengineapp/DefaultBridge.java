package emeshka.webengineapp;

import javafx.scene.web.PromptData;

/**
 * Created by Alexandra on 31.05.2018.
 */

public class DefaultBridge implements Bridge {
    private Application mainApp = null;

    public void setMainApp(Application mainApp) {
        this.mainApp = mainApp;
    }

    public String prompt(String s) {
        return mainApp.dialogs().prompt(s);
    }

    public String prompt(PromptData pd) {
        String answer = mainApp.dialogs().prompt(pd.getMessage());
        if (answer.isEmpty()) return pd.getDefaultValue();
        else return answer;
    }

    public boolean confirm(String s) {
        return mainApp.dialogs().ask(s);
    }

    public void alert(String s) {
        mainApp.dialogs().alert(s);
    }

    public void exit() {//close by option within program or by system features
        if (mainApp.getAskOnExit()) {
            boolean yes = mainApp.dialogs().ask(mainApp.getExitQuestionText());
            if (yes) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
}