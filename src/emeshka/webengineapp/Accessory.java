package emeshka.webengineapp;

import javafx.scene.web.WebErrorEvent;
import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

/**
 * Created by Alexandra on 31.05.2018.
 */
public class Accessory {
    ////////////////////////////////////вспомогательная фигня//////////////////////////////
    private Application mainApp = null;

    // show message with the only button 'OK'
    public void alert(String text) {
        JOptionPane.showMessageDialog(mainApp.getWindow(), text, mainApp.getModalDialogTitleText(), JOptionPane.PLAIN_MESSAGE);
    }

    // ask something with options 'YES' or 'NO'
    public boolean ask(String text) {
        int dialogButton = JOptionPane.YES_NO_OPTION;
        int res = JOptionPane.showConfirmDialog(mainApp.getWindow(), text, mainApp.getModalDialogTitleText(), dialogButton);
        return res == JOptionPane.YES_OPTION;
    }

    public String prompt(String text) {
        String answer = JOptionPane.showInputDialog(text);
        if (answer == null) return "";
        else return answer;
    }

    public void alert(String text, String details) {
        JDialog dialog = new JDialog(mainApp.getWindow(), mainApp.getModalDialogTitleText(), true);
        dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        int w = 500;
        int h = 400;
        dialog.setSize(w, h);

        String html = "<html><font style=\"font-family: Arial, Sans, OpenSans, Segoe UI, Ubuntu, sans-serif; font-weight: normal;\">"+text.replaceAll("[\n\r]", "<br>")+
                "<br>----------------------------------------------------------------------------------------------------------<br>"+
                mainApp.getDetailsCommentText()+
                "<br><textarea wrap=\"soft\" rows=15 cols=100>"+details+"</textarea></html>";
        dialog.add(new JLabel(html));
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle dim = env.getMaximumWindowBounds();
        dialog.setLocation(dim.width/2-w/2, dim.height/2-h/2);
        dialog.setVisible(true);
    }

    public void alert(Exception e, String comment) {//сокращение записи
        alert(comment, text(e));
    }

    public void alert(WebErrorEvent e, String comment) {//сокращение записи
        alert(comment, text(e.getException()));
    }

    public void alert(Exception e) {//сокращение записи
        alert("", text(e));
    }

    public void alert(WebErrorEvent e) {//сокращение записи
        alert("", text(e.getException()));
    }

    public void alertStyled(String text, String css, String wrapperTag) {
        alert(style(text, css, wrapperTag));
    }

    protected String text(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();
        return "\n\nStack trace:\n" + stackTrace;
    }

    protected String style(String text, String css, String tag) {
        css = css.replaceAll("[\n\r]", "");
        text = text.replaceAll("[\n\r]", "<br>");
        if (tag == null || tag.isEmpty()) text = "<html><font style=\"" + css + "\">" + text + "</font></html>";
        else text = "<html><font style=\"" + css + "\"><" + tag + ">" + text + "</" + tag + "></font></html>";
        return text;
    }

    public Accessory(Application app) {
        mainApp = app;
    }//класс не имеет ентитей, а только статические методы
}
