
package KreweMessenger;

import java.io.*;
import java.net.*;


class CommunicationClient 
{
    private BufferedReader userInput;
    //Server Conenction 
    private DatagramPacket sendPacket; //Outgoing data to server
    private InetAddress serverAddress;
    private int serverPort;

    //Client Socket
    private DatagramSocket  clientSocket;
    private String username;

    public CommunicationClient() throws Exception
    {
        //Client Initiailization
        clientSocket    = new DatagramSocket();
        //Server connection point
        serverAddress   = InetAddress.getByName("142.93.58.123");
        serverPort      = 2525;
    }
    
    public void setUsername(String username)
    {
        this.username = username;
    }
    public void pingUserNameToServer() 
    {
        String sentenceSent = "c," + username + ", has joined the chat!";
        byte[] sendData     = sentenceSent.getBytes();
     
        try
        {  //send packet to server
            sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            clientSocket.send(sendPacket);
        }
        catch(IOException e)
        {
            System.out.println("Error "+e);
        }
    }

    public void pingDisconnect()
    {
        DatagramPacket sendPacket; //Outgoing data to server
        String sentenceSent = "d," + username + ", has disconnected from the chat.";
        byte[] sendData     = sentenceSent.getBytes();
        
        try
        {  //send packet to server
            sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            clientSocket.send(sendPacket);
        }
        catch(IOException e)
        {
            System.out.println("Error "+e);
        }
        
    }
    
    public DatagramSocket getClientSocket() {return clientSocket;}
    public InetAddress getServerAddress() {return serverAddress;}
    public int getServerPort() {return serverPort;}


}