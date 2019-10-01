 package KreweMessenger;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


/*Implements runnable in order to interface with 
  java.lang.Thread  */
class InboundInterpreter implements Runnable
{
    
    //Lobby--
    private static int chatLobbyCounter; 
    private HashMap<String, Integer> clientMap;
    //Connection
    private DatagramSocket  clientSocket;
    private byte[] recieveData;
    private String message;
    private TextArea chatFeed,lobbyArea;
    //Alert Message
    private File alert;
    private Clip alertClip;
    private  FocusManager fManager;

    public InboundInterpreter(DatagramSocket  clientSocket, TextArea chatFeed, TextArea lobbyArea, FocusManager fManager)
    {

        this.clientSocket   = clientSocket;
        this.chatFeed       = chatFeed;
        this.lobbyArea      = lobbyArea;
        this.fManager       = fManager;
        alert               = new File("assets/alert.wav");
        
        
 
        //Keep track of active users.
        clientMap           = new HashMap<>();
        chatLobbyCounter    = 0;
    }   

    //Called by threads start() method,
    @Override
    public void run()
    {
      
            System.out.println("\t * Chat Thread Started");
        while(true)
        {
            try 
            {
                recieve();
            } 
            catch (IOException ex) {}
        }
           
   
    }

    public void recieve() throws IOException
    {
        String sentenceRecieved = "";    // Text to be displayed in chat feed
        DatagramPacket recievePacket;   // incoming data-packet from server

        recieveData   = new  byte[1024];//Data is recieved/sent via byte packets

        //recieve any new data from server
        recievePacket = new DatagramPacket(recieveData, recieveData.length);//construct a packet from data buffer
        clientSocket.receive(recievePacket); //recieves the incoming message to the client
        //Capture data into string
        sentenceRecieved = new String (recievePacket.getData());
        processPacket(sentenceRecieved);
    }
    
    
     public void processPacket(String messageRecieved ) throws IOException
    {

        String [] components = messageRecieved.split(",");
        String instructionID = components[0];
        String username      = components[1];
        String message       = components[2];
   
        
            //Determine instruction sent
            switch(instructionID)
            {
                case "c": //A User has connected
                    userConnected(username, message);
                    break;

                case "d": //A User has disconnected
                    userDisconnected(username, message);
                    break;

                case "m":// A user has sent a message
                    userSentMessage(username,message);
                    break;
                case"u": // The server has provided the active set of users
                    appendUserList(message);
                    break;
                    
                default:
                //do som error handling
                  break;
                  
            }
    }
     
     public void userConnected(String username, String message)
     {
        chatLobbyCounter++; 
        clientMap.put(username,chatLobbyCounter);
        updateLobbyArea();
        userSentMessage("SERVER",username+message);
     }
     public void userDisconnected(String username, String message)
     {
        clientMap.remove(username);
        updateLobbyArea();
        userSentMessage("SERVER",username+message);
     }
     
     public void userSentMessage(String username, String message)
     {
        
        String textStr = "\n"+username+": "+message;
        chatFeed.appendText(textStr);
        playAlertMessage();
     }
     private void appendUserList(String message)
     {
         String [] uList = message.split(";");
         for(String s: uList)
         {
             chatLobbyCounter++;
             clientMap.put(s, chatLobbyCounter);
         }
         updateLobbyArea();
         
     }
     private void updateLobbyArea()
     {
        lobbyArea.setText("");
        
        clientMap.forEach((key ,value) -> 
        {
            lobbyArea.appendText(key);
            lobbyArea.appendText("\n");
         });

     }

     
    private void playAlertMessage()
    {
        if(!fManager.getFocus())
        {
            try 
            {   
                alertClip           = AudioSystem.getClip();
                alertClip.open(AudioSystem.getAudioInputStream(alert));
                alertClip.start();
            } 
            catch (Exception ex) 
            {
                Logger.getLogger(InboundInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
    
        

    }
}
