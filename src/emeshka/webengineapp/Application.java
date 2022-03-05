package emeshka.webengineapp;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by Alexandra on 28.02.2018.
 */
public class Application {
    private JFrame window = null;
    private WebView wv = null;
    private WebEngine engine = null;//js engine
    private Bridge bridge = null;//js engine
    private Accessory acc = null;//module that shows different modal dialogs
    private HashMap<String, Function<String, Integer>> handlers = new HashMap<>();

    private boolean askOnExit = true;
    private String page = "";//gui. Lies nearby executive file
    private String parameter = "";
    private boolean pageLoaded = false;
    private int width = 800;
    private int height = 700;
    private String title = "";
    private String exitQuestionText = "Sure to exit?";
    private String modalDialogTitleText = "Warning!";
    private String detailsCommentText = "You can copy following error text using Ctrl+C.";
    private String jsLogPrefix = "-- js log: ";

    public JFrame getWindow() {
        return window;
    }

    public WebView getWv() {
        return wv;
    }

    public WebEngine getEngine() {
        return engine;
    }

    public Bridge getBridge() {
        return bridge;
    }

    public String getPage() {
        return page;
    }
    //setPage() not usable, because we have loadPage()

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        window.setTitle(title);
    }

    public String getExitQuestionText() {
        return exitQuestionText;
    }

    public void setExitQuestionText(String exitQuestionText) {
        this.exitQuestionText = exitQuestionText;
    }

    public String getModalDialogTitleText() {
        return modalDialogTitleText;
    }

    public void setModalDialogTitleText(String modalDialogTitleText) {
        this.modalDialogTitleText = modalDialogTitleText;
    }

    public String getDetailsCommentText() {
        return detailsCommentText;
    }

    public void setDetailsCommentText(String detailsCommentText) {
        this.detailsCommentText = detailsCommentText;
    }

    public Accessory dialogs() {
        return acc;
    }

    public String getJsLogPrefix() {
        return jsLogPrefix;
    }

    public void setJsLogPrefix(String jsLogPrefix) {
        this.jsLogPrefix = jsLogPrefix;
    }

    public boolean getPageLoaded() {
        return pageLoaded;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int newWidth, int newHeight) {
        width = newWidth;
        height = newHeight;
        window.setSize(width, height);
    }

    public void addAlertHandler(String key, Function<String, Integer> handler) {
        handlers.put(key, handler);
    }

    public Function<String, Integer> removeAlertHandler(String key) {
        return handlers.remove(key);
    }

    public boolean getAskOnExit() {
        return askOnExit;
    }

    public void setAskOnExit(boolean askOnExit) {
        this.askOnExit = askOnExit;
    }

    private int getRuntimeJavaVersion() {
        String javaVer = System.getProperty("java.specification.version");
        int ver = 0;
        switch (javaVer) {
            case "1.8": ver = 8; break;
            case "9": ver = 9; break;
            case "10": ver = 10; break;
            case "11": ver = 11; break;
            case "12": ver = 12; break;
            case "13": ver = 13; break;
        }
        return ver;
    }
    /*
    executeScript() with js function call in it until Java 9 is buggy and can cause stackoverflow.
    This method sets a global js variable. If you use Java 8, you can use this as callback replacer
    (because real callbacks can crash your app). Js code, if it waits a signal from Java machine, should check repeatedly
    whether the appointed variable is set.
    It is safer, though complex and dirty.
    Hint: you can define setter for a property in your js code.
     */
    public void setGlobal(Object what, String variableName) {
        String json;
        if (what == null) json = "null";
        else json = new Gson().toJson(what, what.getClass());
        JSObject jsWindow = (JSObject) execute("window");
        jsWindow.setMember(variableName, json);
    }

    /* If you use Java 9 or higher, this is better solution for callbacks*/
    public Object execute(String jsCode) {
        return engine.executeScript(jsCode);
    }

    /* Combination on both methods above. Use if you want to support Java 8, but use more effective
    way of callbacks if possible. */
    public void callback(Object what, String callbackName) {
        int jv = getRuntimeJavaVersion();
        if (jv <= 8) {
            setGlobal(what, callbackName+"_argument");
        } else {
            String json;
            if (what == null) json = "null";
            else json = new Gson().toJson(what, what.getClass());
            System.out.println(json);
            execute(callbackName+"(\'"+json+"\');");
        }
    }

    //load html page from file
    public void loadPage(File f, String parameter) {
        try {
            if (!f.exists()) throw new FileNotFoundException("WebEngineApp.Application.loadPage(): No such file: "+f.getAbsolutePath());
            if (f.isDirectory()) throw new IOException("WebEngineApp.Application.loadPage(): Is a directory: "+f.getAbsolutePath());
            /*Scanner sc1 = new Scanner(f);
            String webStr = sc1.useDelimiter("\\Z").next();
            if (enableDebugger) {
                int index = webStr.indexOf("</head>");
                if (index < 0) index = webStr.indexOf("<body>");

                if (index < 0) webStr = "<script src=\""+debuggerPath+"\"></script>\n" + webStr;
                else {
                    String first = webStr.substring(0, index);
                    String second = webStr.substring(index);
                    webStr = first + "<script src=\""+debuggerPath+"\"></script>\n" + second;
                }
            }
            System.out.println(webStr.substring(0, 400));
            engine.loadContent(webStr);*/
            pageLoaded = false;
            engine.load(f.toURI().toURL().toString()); //not equivalent to getAbsolutePath. Result is like file:/C:/test%20a.xml
            page = f.getAbsolutePath();
            this.parameter = parameter;
        } catch (MalformedURLException e) {
            acc.alert(e, "WebEngineApp.Application.loadPage(): Error when loading page '" + f.getAbsolutePath() + "': ");
            System.exit(-1);
        } catch (IOException e) {
            acc.alert(e, "Problems when loading html file.");
            System.exit(-1);
        }
    }

    //load html page from file by path
    public void loadPage(String path, String parameter) {
        File f = new File(path);
        loadPage(f, parameter);
    }

    //optional simple function to test if there is js-java contact
    //you should use it after Application.run() and wait a little til page is loaded
    /*public void test() {
        //System.out.println("test started.");
        acc.alert("Application.test() will try to change document.body.innerHTML. If no changes applied, then test failed.");
        execute("document.body.innerHTML = 'Application.test(): success<br>'");
        //engine.executeScript("window.initialize()");
        JSObject global = (JSObject) execute("window");
        JSObject parseInt = (JSObject) global.getMember("parseInt");
        String name = (String) parseInt.getMember("name");
        int len = (int) parseInt.getMember("length");
        acc.alert("Application.test(): global function:\n" +
                "function name: " + name + ", length: " + len);
        String javaVar = global.getMember("java").toString();
        acc.alert("Application.test(): variable java is '" + javaVar+"'");
        acc.alert("Application.test() will try to call window.confirm() from within js");
        execute("confirm('Application.test() called this confirm');");
        //System.out.println("test finished.");
    }*/

    public void center() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle dim = env.getMaximumWindowBounds();
        window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
    }

    public void run() {
        int jv = getRuntimeJavaVersion();
        if (jv < 9) System.out.println("WebEngineApp: WARNING! App is running under Java "+jv
                +".\n Avoid to call javascript functions with Application.execute('foo()').\n" +
                "It can cause unexpected stackoverflow error! Use Application.setGlobal('globalVariableName', 'value') instead!\n" +
                "For details see https://bugs.openjdk.java.net/browse/JDK-8089681");
        window = new JFrame(title);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//prevent default
        window.addWindowListener(new WindowAdapter() {//close by system facilities
            public void windowClosing(WindowEvent e) {
                bridge.exit();
            }
        });

        final JFXPanel jfxPanel = new JFXPanel();
        window.add(jfxPanel);
        Platform.runLater(() -> {
            wv = new WebView();
            engine = wv.getEngine();//engine and bridge are not changed after loading new page
            engine.setConfirmHandler(bridge::confirm);
            engine.setPromptHandler(bridge::prompt);
            engine.setOnError(weberrorevent -> acc.alert(weberrorevent, "during load of '" + page + "'"));
            handlers.put("java::onload=true", (data) -> {
                pageLoaded = true;
                if (!window.isVisible()) window.setVisible(true);
                JSObject jsobj = (JSObject) execute("window");
                //System.out.println("set bridge on load");
                jsobj.setMember("java", bridge);
                jsobj.setMember("parameter", parameter);
                return 0;
            });
            handlers.put("java::log()", (data) -> {
                if (data.length() <= 11)
                    System.out.println("Warning! js tried to output to java console" +
                            " using 'java::log()' prefix in alert, but no data to output was given");
                System.out.println(jsLogPrefix + data.substring(11));
                return 0;
            });
            engine.setOnAlert(event -> {
                String data = event.getData();
                Function<String, Integer> handler = null;
                Set<String> handlersKeys = handlers.keySet();
                for (String key : handlersKeys) {
                    if (data.startsWith(key)) handler = handlers.get(key);
                }
                if (handler == null) {
                    acc.alert(data);
                } else {
                    handler.apply(data);
                }
            });
            jfxPanel.setScene(new Scene(wv));
            loadPage(page, parameter);
        });
        if (width > 0 && height > 0) window.setSize(width, height);//width and height includes borders
        else if (width < 0 && height < 0) {
            window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            window.setUndecorated(true);
        } else {
            if (width == 0 && height == 0) {
                window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            } else if (width == 0 && height > 0) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle bounds = env.getMaximumWindowBounds();
                width = bounds.width;
                window.setSize(width, height);
            } else if (width > 0 && height == 0) {
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle bounds = env.getMaximumWindowBounds();
                height = bounds.height;
                window.setSize(width, height);
            } else {
                System.out.println("WebEngineApp: Warning: invalid combination of size parameters. Size reset");
                System.out.println("WebEngineApp: Info: accepted combinations:\n" +
                        "w > 0, h > 0: sets specified size\n" +
                        "w = 0, h > 0: sets specified height and maximized width\n" +
                        "w > 0, h = 0: sets specified width and maximized height\n" +
                        "w = 0, h = 0: sets maximized both\n" +
                        "w < 0, h < 0: sets maximized both undecorated (no system panels are visible, neither close/minimize/etc buttons)");
                window.setSize(1200, 500);
            }
        }
    }

    public Application(String title, String firstPage, int width, int height, Bridge a) {
        if (title.isEmpty()) this.title = "Application";
        else this.title = title;
        page = firstPage;//if empty, it will alert on loadPage
        this.width = width;
        this.height = height;
        bridge = a;
        acc = new Accessory(this);
        bridge.setMainApp(this);
    }

    public Application(String title, String firstPage, String initParam, int width, int height, Bridge a) {
        if (title.isEmpty()) this.title = "Application";
        else this.title = title;
        page = firstPage;//if empty, it will alert on loadPage
        this.width = width;
        this.height = height;
        parameter = initParam;
        bridge = a;
        acc = new Accessory(this);
        bridge.setMainApp(this);
    }

    protected Application() { }
}