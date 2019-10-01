
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



//Bit iffy about enums. might user it later
enum ClientInstructionSet 
{
    CONNECT('c'),
    DISCONNECT('d'),
    MESSAGE('m');
    
    private char id;

    private ClientInstructionSet(char id)
    {
        this.id = id;
    }

    public char getId()
    {
        return id;
    }
};


class KreweServer
{
    private String formatedUserList;
    private HashMap<Integer,Client> clientMap;
    private ArrayList<String> connectedUsernameList;
    private DatagramSocket serverSocket;
    private byte[] sendData;

                        // exception handling needed to be done
    KreweServer(int portNum) throws Exception
    {
        clientMap               = new HashMap<Integer,Client>();    
        connectedUsernameList   = new ArrayList<String>();
        serverSocket            = new DatagramSocket(portNum);
    }

    //Serving data between various clients
    public void serve()throws Exception
    {
        String               sentenceRecieved = "";
        DatagramPacket       recievedPacket;  //Incoming data to server
        byte[] recieveData;  recieveData     = new byte[1024];  
        //Get  recieved data
        recievedPacket = new DatagramPacket(recieveData, recieveData.length);
        serverSocket.receive(recievedPacket);
        //Capture data into string
        sentenceRecieved = new String (recievedPacket.getData());
        //Process Packet substrings to determine additional instructions
        processPacket(recievedPacket, sentenceRecieved);  
    }

    public void processPacket(DatagramPacket recievedPacket, String messageRecieved ) throws IOException
    {
        int sendPort            = recievedPacket.getPort();
        InetAddress sendAddress = recievedPacket.getAddress();
        //Message Components 
        String [] components = messageRecieved.split(",");
        String instructionID = components[0];
        String username      = components[1];
        String message       = "m,"+username+", "+components[2];
        int id = 0;
        int probe;
        //try to assign client ID
        try
        {
            for(int i: clientMap.keySet())
            {
                 probe = clientMap.get(i).getIdByIpAndPort(sendPort, sendAddress);
                if(probe != 0)
                {
                    id = probe;
                    break;
                }
            }

        }   //if nullpointer, the client does not exist
        catch(NullPointerException e){}
     
        
              //Determine instruction sent
              switch(instructionID)
              {
                    case "c":
                        userConnected(sendPort, sendAddress,username);
                        break;
                  
                    case "d":  
                        userDisconnected(sendPort, sendAddress, username, id);
                        break;
      
                    case "m":
                        userSentMessage(sendPort, sendAddress, message);
                        break;
      
                  default:
                  //do som error handling
                    break;
      
              }
    }
    public void userConnected(int clientPort, InetAddress clientAddress, String username) throws IOException
    {  //send  welcome message  back to clients, and user name to append to lobby text area 
        String welcomeMessage = "c,"+username + ", has joined the chat!";
        mapClients(clientPort, clientAddress, username);               //adds Clients to clientmap
        requestAndSendUserList(clientPort, clientAddress, username);  // replys to connected client with list of conencted users
        userSentMessage(clientPort, clientAddress, welcomeMessage);  //  sends conenction message to other connected clients 
    }

    public void userDisconnected(int clientPort, InetAddress clientAddress, String username, int clientId) throws IOException
    { //send  disconnect message  back to clients, and username to remove it from lobby text area 
        //Remove client from Map and notify remaining client
        String disconnectMessage = "d,"+username + ", has left the chat";
        clientMap.remove(clientId);
        
        System.out.println("BEFORE");
        for(String s: connectedUsernameList)
        {
            System.out.println(s);
        }
        connectedUsernameList.removeIf(string -> string.equals(username));

        System.out.println("AFTER");
        for(String s: connectedUsernameList)
        {
            System.out.println(s);
        }
        userSentMessage(clientPort, clientAddress, disconnectMessage);
            
    }

    public void userSentMessage(int sendPort, InetAddress sendAddress, String messageToBeSent) throws IOException
    {
        sendData = messageToBeSent.getBytes();
        DatagramPacket sendPacket;     // outgoing data to clients

        for(int i: clientMap.keySet())
        {//Send message to all clients except the one that send it
            if(sendPort != clientMap.get(i).getPort() && sendAddress != clientMap.get(i).getAddress())
            {   
                sendPacket  = new DatagramPacket(sendData, sendData.length,clientMap.get(i).getAddress(), clientMap.get(i).getPort());
                serverSocket.send(sendPacket);
            }
        }

    }
 
    public void requestAndSendUserList(int clientPort, InetAddress clientAddress, String username) throws IOException
    {//send list of online users to newly connected client
        String messageToBeSent = "u,"+username+","+formatedUserList;
        sendData = messageToBeSent.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        serverSocket.send(sendPacket);
    }

    public void mapClients(int port, InetAddress address, String username)
    {
        int id = generateClientID();
        clientMap.put(id, new Client(port,address, username, id));
        connectedUsernameList.add(username);
        updateUserDisplay();
    }

    private void updateUserDisplay()
    {
        formatedUserList = "";

        for(String s: connectedUsernameList)
        {
            formatedUserList+=s+";";
        }
    }

    private int generateClientID()
    {
        //(int)(Math.random()*100)+1;
        int randID = 0;
        do
        {
            randID = (int) (Math.random()*1000)+1;

        }while(clientMap.containsKey(randID));

        System.out.println(randID);
        return randID;
    }
    
    public static void main(String [] args) throws Exception
    {
        KreweServer server = new KreweServer(2525);
        System.out.println("Establishing connection...");
        while(true)
        {
            server.serve();

        }
    }

}