import java.net.*;

public class Client
{
    private int id;
    private int port;
    private InetAddress ipAddress;
    private String name;

    public Client(int port, InetAddress ipAddress, String name, int id)
    {
        this.port       = port;
        this.ipAddress  = ipAddress;
        this.name       = name;
        this.id         = id;
    }
    public InetAddress getAddress()
    {
        return ipAddress;
    }
    public int getPort()
    {
        return port;
    }
    public String getName()
    {
        return name;
    }
    public int getId()
    {
        return id;
    }

    public int getIdByIpAndPort(int checkPort, InetAddress checkAddress)
    {
  
     return this.ipAddress.equals(checkAddress)  && this.port == checkPort ? this.id : 0;
    }
    @Override
    public String toString()
    {
        return "ID: "+id+" Name:"+name + " IP: " + ipAddress + " Port: " + port;
    }





}