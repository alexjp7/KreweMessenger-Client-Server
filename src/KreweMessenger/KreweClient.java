
package KreweMessenger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ObservableValue;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;


public class KreweClient  extends Application implements Initializable 
{
    /**************************CLIENT COMPONENTS*****************************/
    private Thread feedInThread;      //Processes incoming messages from server
    private Thread feedOutThread; //Processes outgoing messages to server
    private String username; 
    private static CommunicationClient client;
    private static  FocusManager fManager;
    private InboundInterpreter feedIn;
    private boolean usernameFound;
    private BufferedWriter writer; 
    private BufferedReader reader;
    private static boolean isFocused;

    /****************************GUI-COMPONENTS********************************/
    private final  int styleableComponents = 2;
    public HashMap<String, String []> styleMap;
    private String bkgValue;
    private String mainFrameText;
    @FXML
    private Circle connectionStatus;
    @FXML
    private Button enterText;
    @FXML
    private Button saveButton;
    @FXML
    private TextArea lobbyArea;
    @FXML
    private TextArea chatFeed;
    @FXML
    private TextField talkSpace;
    @FXML
    private Label connectionInfoLabel;
    @FXML
    private Label appLabel;
    @FXML
    private Label systemPrompt;
    @FXML
    private Pane mainPane;
    @FXML
    private ColorPicker lobbyAreaBkgColorPicker;
    @FXML
    private ColorPicker lobbyTextColorPicker;
    @FXML
    private ColorPicker mainFrameFontPicker;
    @FXML
    private ColorPicker chatFeedBkgPicker;
    @FXML
    private ColorPicker talkSpaceFontPicker;
    @FXML
    private ColorPicker talkSpaceBkgPicker;
    @FXML
    private ColorPicker mainFrameBkgPicker;
    @FXML
    private ColorPicker chatFeedTextPicker;


    /****************************INITIALZIATIONS********************************/
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        componentInit();
        try 
        {
            //Request username
            username = requestUsername();
            if(usernameFound) // if found, ping server
            {
                client.setUsername(username);
                connectionInfoLabel.setText(username);
                client.pingUserNameToServer();
            }
            //'Runnable' class types used for defining Thread pro
            feedIn   = new InboundInterpreter(client.getClientSocket(), chatFeed, lobbyArea, fManager);
            OutBoundController feedOut  = new OutBoundController(client.getClientSocket(), client.getServerAddress(), client.getServerPort(), username, talkSpace, chatFeed, enterText);
            //Thread invokation
            feedInThread    = new Thread(feedIn);  
            feedOutThread   = new Thread(feedOut);
            feedInThread.start(); 
            feedOutThread.start();
        } 
        catch (Exception ex) {Logger.getLogger(KreweClient.class.getName()).log(Level.SEVERE, null, ex);}
    }
    public void componentInit()
    {
        //Chat Feed
        chatFeed.setWrapText(true);
        chatFeed.setText("");
        styleMap = new HashMap<>();
        //mapping between styleable components and list of  styles.. [0] = font, [1] = background
        styleMap.put("chatFeed",  new String [styleableComponents]);
        styleMap.put("lobbyArea", new String [styleableComponents]);
        styleMap.put("talkSpace", new String [styleableComponents]);
        initStyles();
    }

    private void initStyles()
    {
        String line = "";
        String font;
        String bkg;
        String [] components;
        String [] styles;
        try 
        {
            reader = new BufferedReader(new FileReader("data/styles.txt"));
            try // Probably don't need to check for this now -
            {   //Rebuild stylemap
                line = reader.readLine(); 
                while(line != null) //checks for EoF
                {
                    components  = line.split(",");
                    styles   = components[1].split(";");
                    font     = styles[0];
                    bkg      = styles[1];
                    
                    if(components[0].equals("mainPane")) //set background color 
                    {
                        setBackground(bkg);
                       appLabel.setStyle("-fx-text-fill :#" + font+";");  
                     connectionInfoLabel.setStyle("-fx-text-fill: #"+ font+";");  
                    }
                    else // set style string values 
                    {
                        try
                        {   
                            if(!font.equals("null"))
                            {
                                setStyleString(components[0],font,0); 
                            } 
                            if(!bkg.equals("null"))
                            {      
                                setStyleString(components[0],bkg,1);
                            }
                            
                            initColorPickers(components[0], font, bkg);  
                        }
                        catch(ArrayIndexOutOfBoundsException e){}
                    }
                    line = reader.readLine();  
                }
            }
            catch(NullPointerException e) {}
            reader.close();
        } 
        catch (IOException e) {}
 
        
        HashMap<Region, String> regionMap = new HashMap<>();
        regionMap.put(talkSpace,"talkSpace");
        regionMap.put(lobbyArea,"lobbyArea");
        regionMap.put(chatFeed,"chatFeed");
 
        //Set the styles for each component
        for(Map.Entry<Region, String> entry: regionMap.entrySet())
        {   
            try
            {
                if(getStyleString(entry.getValue()) != null)
                {
                    entry.getKey().setStyle(getStyleString(entry.getValue()));
                }
            }catch(Exception e){}
        }  
    }
    

    private void initColorPickers(String component, String font, String bkg)
    {
        try
        {
            Color fColor = Color.web(font);  
            Color bkgColor = Color.web(bkg);  
            switch(component)
            {
                case "chatFeed":
                    chatFeedTextPicker.setValue(fColor);
                    chatFeedBkgPicker.setValue(bkgColor);
                    break;
                case "lobbyArea":
                    lobbyTextColorPicker.setValue(fColor);
                    lobbyAreaBkgColorPicker.setValue(bkgColor);
                    break;
                case "talkSpace":
                    talkSpaceFontPicker.setValue(fColor);
                    talkSpaceBkgPicker.setValue(bkgColor);
                    break;
                default:
                    break;
            }
       }
       catch(IllegalArgumentException e){System.out.println("ERROR"+e);}  
    }
    
    private void setBackground(String bkgColor) 
    {
       try
       { 
        bkgValue    = bkgColor;
        Color color = Color.web(bkgColor);
        Paint fill  = color;
        //Background fill components
        BackgroundFill bkgFill  = new BackgroundFill(fill,CornerRadii.EMPTY, Insets.EMPTY);
        Background bkg          = new Background(bkgFill);
        //set background values
        mainPane.setBackground(bkg);
        mainFrameBkgPicker.setValue(color);  
       }
       catch(IllegalArgumentException e){}
               
    }
    
    /******************************EVENT-HANDLERS**********************************/
    //Application Closing
    private void closeClient(WindowEvent e, Stage stage)
    {
        client.pingDisconnect();
        stage.close();
        System.exit(1);
    }

    /************Color Pickers*********/
    //Main Frame
    @FXML   
    private void handleMainFrameBkgPicker(Event event) 
    {  
        setBackground(mainFrameBkgPicker.getValue().toString());
    }
     @FXML
    private void handleMainFrameText(Event event) 
    {  
        String fontColor = colorToHex(mainFrameFontPicker.getValue());
        appLabel.setStyle("-fx-text-fill :#" + fontColor+";");  
        connectionInfoLabel.setStyle("-fx-text-fill: #"+ fontColor+";");  
        mainFrameText=fontColor;
        
    }
    

    //Lobby Area
    @FXML
    private void handleLobbyTextColorPicked(Event event) 
    {  
        String compName = "lobbyArea";
        String value; 
        //Set style string value
        value = colorToHex(lobbyTextColorPicker.getValue());
        setStyleString(compName,value ,0);
        //retrieve string concatination of styles and apply it to component
        lobbyArea.setStyle(getStyleString(compName));     
    }
    
    @FXML
    private void handleLobbyBkgColorPicked(Event event) 
    {   
        String compName = "lobbyArea";
        String value; 
        //Set style string value
        value = colorToHex(lobbyAreaBkgColorPicker.getValue());
        setStyleString(compName,value ,1);
        //retrieve string concatination of styles and apply it to component
        lobbyArea.setStyle(getStyleString(compName));   
    }
    
     //Talkspace
     @FXML
    private void handleTalkSpaceFont(Event event) 
    {
        String compName = "talkSpace";
        String value; 
        //Set style string value      
        value = colorToHex(talkSpaceFontPicker.getValue());
        setStyleString(compName,value ,0);
        //retrieve string concatination of styles and apply it to component
        talkSpace.setStyle(getStyleString(compName));   
        
    }

    @FXML
    private void handleTalkSpaceBkg(Event event) 
    {
        String compName = "talkSpace";
        String value; 

        //Set style string value
        value = colorToHex(talkSpaceBkgPicker.getValue());
        setStyleString(compName,value ,1);
        //retrieve string concatination of styles and apply it to component
        talkSpace.setStyle(getStyleString(compName));    
        
    }


    //ChatFeed
    
    @FXML
    private void handleChatFeedTextColor(Event event) 
    {
        String compName = "chatFeed";
        String value;
        
        //Set style string value
        value = colorToHex(chatFeedTextPicker.getValue());
        setStyleString(compName,value ,0);
        //retrieve string concatination of styles and apply it to component
        chatFeed.setStyle(getStyleString(compName));     
    }
    
    @FXML
    private void handleChatFeedBkg(Event event) 
    {
        String compName = "chatFeed";
        String value; 
   
        //Set style string value
        value = colorToHex(chatFeedBkgPicker.getValue());
        setStyleString(compName,value ,1);
        //retrieve string concatination of styles and apply it to component
        chatFeed.setStyle(getStyleString(compName)); 
    }
    

    
    @FXML
    private void handleSaveButton(MouseEvent event) 
    {
        String filePath = "data/styles.txt";
        try
        {   //Create file if it does not exist
            File  file  = new File(filePath); 
            if(file.exists())
            {
                throw new FileAlreadyExistsException("File aleady exists");
            }       
        }
        catch(FileAlreadyExistsException e)
        {
            System.out.println(e);
        } 
        try
        {//Write to styles file
            String cmpnt, fontColor,bkgColor;
            
            writer      = new BufferedWriter(new FileWriter(filePath));
            for(Map.Entry<String,String[]> s: styleMap.entrySet())
            {
                cmpnt       = s.getKey();
                fontColor   = getComponentColor(cmpnt,0);
                bkgColor    = getComponentColor(cmpnt,1);
                
                writer.write(cmpnt+","+fontColor+ ";"+bkgColor);
                writer.newLine();
            }
            writer.write("mainPane,"+mainFrameText+";"+bkgValue);
            writer.close();
        }
        catch(IOException e){}
    }

    
    /******************************HELPER-METHODS**********************************/
    private String requestUsername()
    {
        String  input   = "";
        boolean isFound = false;
        
        //Initial Request username from file 
        try
        {
            //Text file contains username, if new user the file wont exist and will be created here
            reader = new BufferedReader(new FileReader("data/userData.txt"));
            try
            {
                String line          = reader.readLine().trim();
                isFound       = true;
                usernameFound = true;
                input         = line;      
               
            }
            catch (NullPointerException e)
            {
                   isFound = false;
            }
            finally{reader.close();}
 
            //If no name is found, request new input
            if(!isFound)
            {
                do
                {
                  input = JOptionPane.showInputDialog(null,"What Would you like to be called?");
                  usernameFound = true;
                }
                while(input.isEmpty()||input.trim().equals(""));
                //Write username to userData file
                writer = new BufferedWriter(new FileWriter("data/userData.txt"));
                writer.write(input);
                writer.close();
            }
       }
      catch(IOException e)
      {
        usernameFound = false;
        systemPrompt.setStyle("-fx-text-fill:red;");
        connectionInfoLabel.setStyle("-fx-text-fill:red;");
        connectionStatus.setFill(Color.RED);
        connectionInfoLabel.setText("N/A");
        talkSpace.setEditable(false);
        talkSpace.setDisable(true);
        systemPrompt.setText("Error - unable to load userData file. Check for userData.txt into root");
      }
       return input; 
    }
    
    public static String colorToHex(Color color) 
    {
        return colorChanelToHex(color.getRed())
                + colorChanelToHex(color.getGreen())
                + colorChanelToHex(color.getBlue())
                + colorChanelToHex(color.getOpacity());
    }
    
     private static String colorChanelToHex(double chanelValue) 
     {
        String rtn = Integer.toHexString((int) Math.min(Math.round(chanelValue * 255), 255));
        if (rtn.length() == 1) {
            rtn = "0" + rtn;
        }
        return rtn;
    }
     
     
    public String getComponentColor(String component, int styleValue)
    { 
        return styleMap.get(component)[styleValue];
    }
    
    public String getStyleString(String component)
    { 
        String cssString;
        String textStyle = "-fx-text-fill: #" + getComponentColor(component,0) + ";";
        String bkgStyle  = "-fx-control-inner-background: #"+ getComponentColor(component,1) + ";";
        
        cssString  = getComponentColor(component,0) == null ? "" : textStyle;
        cssString += getComponentColor(component,1) == null ? "" : bkgStyle;
        
        return cssString;
    }
    
        public void setStyleString(String component, String value, int attribute) // should check for valid CSS syntax
    {
     styleMap.get(component)[attribute] = value;
    }
         
    private void focusClientHandler(boolean value)
    {
        fManager.setFocus(value);
    }

    /*****************************MAIN**************************************/
     @Override
    public void start(Stage stage) throws Exception 
    {
        
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        Scene scene = new Scene(root);
     
        stage.setScene(scene);
        stage.setTitle("KreweMessenger");
        stage.getIcons().add(new Image("file:assets/icon.png"));
        stage.setOnCloseRequest(e -> closeClient(e,stage)); // closing app -> disconects client, and notifies server
        stage.focusedProperty().addListener((ObservableValue<? extends Boolean> observable,Boolean oldValue, Boolean newValue) -> 
        { 
            focusClientHandler(newValue);
        }) ;
        stage.show();
       
    }
  
    public static void main(String[] args) 
    {   
        try {
            client      = new CommunicationClient();
            fManager    = new FocusManager();
            launch(args);
        } catch (Exception ex) {
            Logger.getLogger(KreweClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
}
