
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Server
implements Runnable {
    private LamportClock localClock;
    private String serverIP;
    private int serverPort;
    private String serverID;
    private String file;
    private boolean locked;
    private HashMap<String, String[]> servers;
    private PriorityQueue<Message> queue;
    private int clientFinished;
    public boolean stopflag;
    public int msg_count_send;
    public int msg_count_receive;
    public int time_start;
    public int time_stop;

    public Server(String serverID, String configFile_server) {
        this.serverID = serverID;
        this.file = "file.txt";
        this.servers = new HashMap<>();
        this.configServer(configFile_server);
        this.configFile();
        this.locked = false;
        this.queue = new PriorityQueue<>();
        this.stopflag = false;
        
    }

    private void configServer(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
                if (this.serverID.equals(parts[0])) {
                	this.localClock = new LamportClock(Integer.parseInt(parts[3]));
	                this.serverIP = InetAddress.getByName(parts[1]).getHostAddress();
	                this.serverPort = Integer.parseInt(parts[2]);
	                this.createFile();
	                continue;
                }
                this.addServer(parts[0], InetAddress.getByName(parts[1]).getHostAddress(), parts[2]);
            }
            r.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void addServer(String serverID, String ip, String port) {
        this.servers.put(serverID, new String[] {String.valueOf(ip),  port});
    }
    
 // create Server log file
    public void createFile() {
        File ServerFile = new File(String.valueOf(this.serverID) + ".txt");
        if (!ServerFile.exists()) {
            try {
            	ServerFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("[ERROR]: Cannot create ServerFile!");
            }
        }
    }
    
 // write log function
    public void writeToServerFile(String record) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(String.valueOf(this.serverID) + ".txt", true)));
            out.println(record);
            out.close();
        }
        catch (IOException out) {
            // empty catch block
        	out.printStackTrace();
            System.err.println("[ERROR]: Cannot write Server File!");
        }
    }

    private void configFile() {
    	// the file is created by server 1.
    	if(this.serverID.equals("Server-01")){
    		File newFile = new File(this.file);
    		try {
                newFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("Cannot creat this file");
            }
    	}
    }

    public String getID() {
        return this.serverID;
    }

    public int getPort() {
        return this.serverPort;
    }

    public LamportClock getClock() {
        return this.localClock;
    }

    @Override
    public void run() {
        this.run_listenning();
    }

    public void run_listenning() {
        ServerListener listener = new ServerListener(this);
        listener.start();
    }
    // send message to clients
    public synchronized void deliver_message(Message m) {
        this.localClock.message(m.getTs());
        String opt = m.getOpt();
        String r2IP = m.getIP();
        int r2Port = m.getPort();
        if (opt.equals("RELEASE")) {
        	this.msg_count_receive ++;
        	if(!this.queue.isEmpty()) {
        		m = this.queue.poll();
        		Message rm = new Message("SERVER", this.serverID, "GRANT", this.localClock.get_time(), this.serverID, this.serverIP, this.serverPort);
        		send_message(rm, m.getIP(), m.getPort());
        	} else {
        		this.locked = false;
        	}
        } else if (opt.equals("ENQUIRY")) {
            Message rm = new Message("SERVER", this.serverID, "ENQUIRY", this.localClock.get_time(), this.serverID, this.serverIP, this.serverPort);
            this.send_message(rm, r2IP, r2Port);
            System.out.println("has reply to client for an enquiry");
        } else if (opt.equals("REQUEST")){
        	this.msg_count_receive ++;
        	System.err.println("receive a request " + opt);
        	if (this.locked) {
        		this.queue.add(m);
        	} else {
        		this.locked = true;
        		this.msg_count_send ++;
        		Message rm = new Message("SERVER", this.serverID, "GRANT", this.localClock.get_time(), this.serverID, this.serverIP, this.serverPort);
        		send_message(rm, m.getIP(), m.getPort());
        	}
        	// this part is just for server1, it will count the finished clients number
        } else if (opt.equals("FINISH")) {
        	this.msg_count_receive ++;
        	this.clientFinished ++;
        	if(this.clientFinished == 5) {
        		Message rm = new Message("SERVER", this.serverID, "STOP", this.localClock.get_time(), this.serverID, this.serverIP, this.serverPort);
        		for(String[] value : this.servers.values()) {
        			this.msg_count_send ++;
        			send_message(rm, value[0], Integer.parseInt(value[1]));
        		}
        		this.stopflag = true;
        	}
        	// if received the stop message from server1, the server will stop
        } else if (opt.equals("STOP")){
        	this.msg_count_receive ++;
        	this.stopflag = true;
        	
        }else {
        	System.err.println("ERROR: Wrong operation type - " + opt);
        }
    }
    // function for sending the messages.
    public synchronized void send_message(Message m, String IP, int Port) {
        try {
            Socket sock = new Socket(IP, Port);
            OutputStream out = sock.getOutputStream();
            ObjectOutputStream outStream = new ObjectOutputStream(out);
            outStream.writeObject(m);
            outStream.close();
            out.close();
            sock.close();
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server s = new Server(args[0], args[1]);
        Thread t = new Thread(s);
        t.start();
    }
}

