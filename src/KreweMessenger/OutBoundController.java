package KreweMessenger;

import java.io.*;
import java.net.*;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


/*Implements runnable in order to interface with 
  java.lang.Thread  */

class OutBoundController implements Runnable
{
    //Runnable Flag
    private boolean  isClosing;
    //Server/Client endpoisRunningints
    private DatagramSocket clientSocket;
    private InetAddress serverAddress;
    private int serverPort;
    //Client username
    private String username;
    //GUI components
    private TextField talkSpace;
    private TextArea chatFeed;
    private Button enterText;
    

    public OutBoundController(DatagramSocket  clientSocket, InetAddress serverAddress, int serverPort, String username, TextField talkSpace,TextArea chatFeed, Button enterText)
    {
        this.clientSocket   = clientSocket;
        this.serverAddress  = serverAddress;
        this.serverPort     = serverPort;
        this.username       = username;
        this.talkSpace      = talkSpace;
        this.chatFeed       = chatFeed;
        this.enterText      = enterText;
        isClosing           = false;
    }
    //Invoked by Threads start() method
    @Override
    public void run() 
    {
         //Add Event listener to talkSpace 
        talkSpace.setOnKeyPressed(event -> {
        try 
        {
            enterText(event);
        } catch (IOException ex) {}});    //do some handling here
        
        enterText.setOnMouseClicked(event -> {
        try 
        {
            handleTextEntered(event);
        } catch (IOException ex) {}});
        
        while(!isClosing)
        {
            //Keep poling for outbound communication
        }
    }
    
    public void threadClose()
    {
        isClosing = true;
    }
    
    @FXML
    private void enterText(KeyEvent event) throws IOException
    {
        if(event.getCode().equals(KeyCode.ENTER))
        {
            handleTextEntered(event);
        }
    }
    
    @FXML
    private void handleTextEntered(Event event) throws IOException
    {
        String sentenceSent         = "m," +username+","+talkSpace.getText();         //Formated String data to go to server
        byte[] sendData             = sentenceSent.getBytes();                       //Outgoing data-array to server                                               
        DatagramPacket sendPacket   = new DatagramPacket(sendData, sendData.length, //Packet to be sent
                                                         serverAddress, serverPort);  
        chatFeed.appendText("\nYou: " + talkSpace.getText());
        talkSpace.setText("");//Clear chat
        //Send data to server
        clientSocket.send(sendPacket);
    }
}