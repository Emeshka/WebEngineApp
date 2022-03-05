# WebEngineApp
An Electron JS but in Java. Construct your client interface using HTML&Javascript and enjoy the mighty Java in the same time.
Are you frustrated when you need to construct a desktop GUI application in Java? Did you hear about the WebEngine class, but found it kind of cumbersome to set up every single time? Well, that's why I started WebEngineApp.
Please note that the library is still version 0, multipage applications may be unavailable.

The library consists of several classes:

class *Application*

private static JFrame window
		- the window frame
public static WebView wv
		- the WebView component, in which HTML-page content is stored.
public static WebEngine engine
    - the JS engine.
public static Bridge bridge
		- the Java-JS bridge.
private static final String PAGE = "gui.html";
		- first page GUI.
private HashMap<String, Function<String, Integer>> handlers = new HashMap<>();
		- template messages handlers, transfered with builtin JS alert() function.
private boolean askOnExit = true;
		- to prompt on closing.
private int WIDTH = 800;
		- the window width.
private int HEIGHT = 700;
		- the window height.
private String TITLE = "";
		- the window title.

public JFrame getWindow()
    - getter for window.
public WebView getWv()
    - getter for WebView object.
public WebEngine getEngine()
    - getter for Javascript engine.
public Bridge getBridge()
    - getter for the bridge.
public String getPage()
    - getter for the current web-page URL.
public String getTitle()
    - getter for the current window title.

public void setTitle(String title)
    - setter for the window title
public void setSize(int width, int height)
    - setter for the window size.
public void addAlertHandler(String key, Function<String, Integer> handler)
    - add a template message handler (messages that are transfered with builtin JS alert() function)
public Function<String, Integer> removeAlertHandler(String key)
    - remove a template message handler by its key.
public boolean getAskOnExit()
    - getter for askOnExit
public void setAskOnExit(boolean askOnExit)
    - setter for askOnExit
public void setGlobal(Object what, String variableName)
    - convert `what` to JSON and set as a global variable in the current Engine with a name `variableName`. One of the possible applications of this method is an `executeScript()` bug avoidance (see below)
public Object call(String function, Object ... args)
    - call a globally available JS function `function`() with given set of arguments. The arguments should be ordered in the same way as JS `function`() expects. All arguments dependless of their respective types, will be converted to JSON. Can cause a crash due to `executeScript()` bug (see below)
(!) public void debug()
    - a method was intended to be an inbuilt debugging help, an integration of Firebug lite in the library itself, because the Java WebEngine WebKit has its unique set of peculiarities when it comes to layout & CSS displaying, but I couldn't make it working. I suppose because Firebug Lite project is suspended (my question on Stack Overflow https://stackoverflow.com/questions/50669070/javafx-webengine-fail-to-load-firebug-lite-js-that-is-stored-locally)
public void loadPage(File f)
    - load a page from a file
public void test()
    - a set of basic tests to make sure that JS-Java bridge is working. Apply it only in the early beginning of your project, when you cannot be sure of this still, because it modifies the page.
public void run()
    - run the Application object: create a window with WebView and set up a listener on window closing, set listeners for `alert`, `confirm`, `prompt`, on file loading error, set window size and load HTML from `page`.
public Application(@NotNull String title, @NotNull String firstPage, int width, int height, @NotNull Bridge a)
    - the constructor: window title, the starting GUI page URL, window width&height and a Bridge object. Does not run the Application, only creates it.

To transfer data from JS to Java, JSON is used, because the JS can pass all simple types (numbers, booleans) and strings. Since we are working with an array, we need to convert it to a string according to the JSON format.
JS has built-in functions JSON.stringify(Object o) and JSON.parse(String str) to work with this format. In Java, I used the Gson library (com.google.gson.Gson) (copywrite Google LLC). With this format, Java and JS exchange messages, see.
For interaction between the two, an object of a special class is created - a bridge. Generally speaking, the bridge is defined directly by the programmer and can contain any methods, however, I found it necessary to oblige the programmer to implement the interface specified in the Bridge file (see below). For convenience, you can use an instance of DefaultBridge (see below), inherit from it, or implement Bridge directly. The bridge instance must then be passed to the constructor. There are two subtleties in the run method that cost me a lot of time to figure out:
1) The bridge must be saved as a global variable, otherwise, after the run method is completed, it will be deleted and then there will be a crash when JS tries to access this address.
2) The bridge cannot be assigned before the page has loaded, because in this case, after downloading it will be overwritten. Therefore, the bridge assignment is put into the "java::onload=true" template message handler.
Template message handlers are functions that should be called if js passed some service message in its alert. You can set this message and the handler itself using the public void addAlertHandler(String key, Function<String, Integer> handler) method, where key is what this message starts with, and handler is a function that performs some actions based on the full message passed to it . The function must return an exit code, however, while it is ignored. Handler example: standard logging handler: in order to send a message from js to the console of a java application, you need to call the alert("java::log() your message of any content and length"); function in js. In this case, the template is "java::log()": if the alert() message starts with these characters, control is passed to the handler. Such a logging implementation is due to the desire that we be able to see the message, even if the bridge is not connected (you never know), therefore, through alert (it always has a listener). The JavaFX WebEngineAPI does not have a standard way to assign a logging handler to console.log(), so alert is used.
The setGlobal(Object what, String variableName) method, as already mentioned, can be used by the developer to call back to the client, without fear of the application crashing. Passing parameters to a function in such a perverted way may seem like a strange implementation, especially considering that we can directly call a JS function from Java using the engine.executeScript() method, however there is a bug regarding the native code of this method in jdk7 versions, jdk8 and early builds of jdk9, causing a memory leak or stack overflow sometimes when trying to call a JS function, even if it is absolutely syntactically correct. This will either freeze or crash the application.
The bug is fixed in the new version of JDK9 with the latest fixes.

class *Accessory*
A set of static methods to display all kinds of messages.

public static int alert(String text)
    - a simple popup window with 'OK' button only.
public static boolean ask(String text)
    - a popup window with 'Yes' and 'No' options (JS `confirm`). Returns the user's choice.
public static String prompt(String text)
    - a popup that prompts user for some input. The popup contains the message, an input field and 2 buttons: 'OK' and 'Cancel'. .
public static int alert(Exception e, String comment)
    - alert user about a Java Exception.
public static int alert(WebErrorEvent e, String comment)
    - same, but with WebErrorEvent class.
protected static String text(WebErrorEvent e, String comment)
    - create a text message to display on the page from WebErrorEvent and a string message.
protected static String text(Exception e, String comment)
    - same, but with Java Exception class.
protected static String style(String text, String css, String tag)
    - a method to style messages. I haven't got a need to make it public, but it can be made.

interface *Bridge*
Defines a basic Bridge interface. Custom Bridges can be made based on it.

void alert(String s);
boolean confirm(String s);
String prompt(String s);
String prompt(PromptData pd);
void exit();

As you can see, these basic functions `alert`, `confirm`, `prompt` (a pair of them, because `prompt` can be called with an additional parameter `defaultValue`, in this case Java converts it into PromptData and calls the second overload), and a close app function.

class *DefaultBridge*
Gives you a default and in most cases the most convenient implementation of Bridge interface. If you don't need any special handling of these functions, you can just extend the DefaultBridge class and add your own methods.
