


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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Client
implements Runnable {
    private LamportClock localClock;
    private String clientID;
    private String clientIP;
    private int clientPort;
    private HashMap<String, String> servers;
    private ArrayList<String> serverIDs;
    private int finish_CS = 0;
    private ArrayList<String> pending_servers;
    private Mutex_imp mutex;
    private int[][] quorum;
    private String file;
    public boolean stopflag;
    private int time_unit;
    public int msg_count_send;
    public int msg_count_receive;
    public int time_start;
    public int time_stop;

    public Client(String clientID, String configFile_server, String configFile_client, String time_unit) {
        this.clientID = clientID;
        this.servers = new HashMap<String, String>();
        this.serverIDs = new ArrayList<String>();
        this.configServer(configFile_server);
        this.configClient(configFile_client);
        this.pending_servers = new ArrayList<String>(this.serverIDs);
        this.mutex = new Mutex_imp(this);
        this.file = "file.txt";
        this.stopflag = false;
        this.time_unit = Integer.parseInt(time_unit.replaceAll("\\s+",""));
        this.msg_count_receive = 0;
        this.msg_count_send = 0;
        
        this.quorum = new int[][] { {0, 1, 3},
									{0, 1, 4},
									{0, 3, 4},
									{0, 2, 5},
									{0, 2, 6},
									{0, 5, 6},
									{1, 3, 2, 5},
									{1, 3, 2, 6},
									{1, 3, 5, 6},
									{1, 4, 2, 5},
									{1, 4, 2, 6},
									{1, 4, 5, 6},
									{3, 4, 2, 5},
									{3, 4, 2, 6},
									{3, 4, 5, 6}};

    }
    
    public String getIP() {
        return this.clientIP;
    }

    public int getPort() {
        return this.clientPort;
    }

    public String getID() {
        return this.clientID;
    }

    public LamportClock getClock() {
        return this.localClock;
    }

    // Configuration all the servers in the system
    private void configServer(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
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
    
 // Configuration for client
    private void configClient(String file) {
        try {
            BufferedReader r = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(" ");
                if (this.clientID.equals(parts[0])) {
                    this.localClock = new LamportClock(Integer.parseInt(parts[3]));                    
                    this.clientIP = InetAddress.getByName(parts[1]).getHostAddress();
                    this.clientPort = Integer.parseInt(parts[2]);
                    this.createFile();
                    break;
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    // add server into hashmap
    private void addServer(String serverID, String ip, String port) {
        this.servers.put(serverID, String.valueOf(ip) + ":" + port);
        this.serverIDs.add(serverID);
    }

    public int sizeOfServers() {
        return this.serverIDs.size();
    }

    // create client log file
    public void createFile() {
        File ClientFile = new File(String.valueOf(this.clientID) + ".txt");
        if (!ClientFile.exists()) {
            try {
                ClientFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                System.err.println("[ERROR]: Cannot create ClientFile!");
            }
        }
    }
    // write log function
    public void writeToClientFile(String record) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(String.valueOf(this.clientID) + ".txt", true)));
            out.println(record);
            out.close();
        }
        catch (IOException out) {
        	out.printStackTrace();
            System.err.println("[ERROR]: Cannot write Client File!");
        }
    }

    @Override
    public void run() {
    	// system configure
        System.out.println("client-" + this.clientID + " is started!");
        //this.writeToClientFile("client-" + this.clientID + " is started!");
        this.run_listenning();
        this.initial_connection();
        while (!this.pending_servers.isEmpty()) {
            System.out.println("Still need replies of filenames from " + this.pending_servers);
        }
        System.out.println("finished initial connection!");
        //this.writeToClientFile("finished initial connection!");
        
        System.out.println("  There are " + this.sizeOfServers() + " server processes!");
        //this.writeToClientFile("  There are " + this.sizeOfServers() + " server processes!");
        System.out.println("  " + this.serverIDs);
        //this.writeToClientFile("  " + this.serverIDs);
        System.out.println("Type 's' if preparing finished.");
        // wait for start
        Scanner keyboard = new Scanner(System.in);
        while (!(keyboard.next()).equals("s")) {
            System.out.println("Not match, please type in again.");
        }
        keyboard.close();
        System.out.println("[START]: Client start generating operations! (total-20)");
        //this.writeToClientFile("[START]: Client start generating operations! (total-20)");
        // start CS for 20 times
        while (this.finish_CS < 20) {
        	int msg_before_send = this.msg_count_send;
        	int msg_before_receive = this.msg_count_receive;
            System.out.println("[" + this.finish_CS + "] " + this.clientID + " Finished Critical Section is " + this.finish_CS);
            //this.writeToClientFile("[" + this.finish_CS + "] " + this.clientID + " Finished Critical Section is " + this.finish_CS);
            
            // select a random quorum to for entering the cs
            String[] select_servers = this.generate_opt();
            // keep running while for entering the CS
            long t1 = System.currentTimeMillis();
            while (!this.mutex.isAllowedInCS()) {
            }
            long t2 = System.currentTimeMillis();
            
            this.execute_CS();
            
            System.out.println(" [Finished-CS]: Finish Writing the file");
            //this.writeToClientFile(" [Finished-CS]: Finish Writing the file");
            
            this.send_release(select_servers);
            this.finish_CS++;
            this.writeToClientFile("CS [" + this.finish_CS + "] Waiting time in milisecond: " + Long.toString(t2 - t1));
            this.writeToClientFile("CS [" + this.finish_CS + "] Messages sent: " + Integer.toString(this.msg_count_send - msg_before_send));
            this.writeToClientFile("CS [" + this.finish_CS + "] Messages received: " + Integer.toString(this.msg_count_receive - msg_before_receive));
        }
        // sending message to server1 that it have finished its all CS
        Message m = new Message("FINISH", this.clientID, "FINISH", this.getClock().get_time(), "Server-01", this.clientIP, this.clientPort);
        String addr = this.servers.get("Server-01");
        this.msg_count_send ++;
        this.send_message(addr, m);
        
        
        System.out.println("[Over]: All Operations Finished!");
        //this.writeToClientFile("[Over]: All Operations Finished!");
        this.writeToClientFile("Total messages sent: " + Integer.toString(this.msg_count_send));
        this.writeToClientFile("Total messages received: " + Integer.toString(this.msg_count_receive));
        this.stopflag = true;
    }

    private void initial_connection() {
        for (String serverID : this.serverIDs) {
            Message m = new Message("ENQUIRY", this.clientID, "ENQUIRY", this.getClock().get_time(), serverID, this.clientIP, this.clientPort);
            String addr = this.servers.get(serverID);
            this.send_message(addr, m);
        }
    }

    private void run_listenning() {
        ClientListener listener = new ClientListener(this);
        listener.start();
    }

    private synchronized void execute_CS() {
    	try {
    		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.file, true)));
	        String command = "Entering " + this.clientID + " " + new Date().toString();
	        out.println(command);
	        try {
	            Thread.sleep(3 * this.time_unit);
	        }
	        catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        out.close();
    	}catch (IOException e){
    		e.printStackTrace();    		
    	}
        
    }
    // randomly generate operation on random server
    private synchronized String[] generate_opt() {
        Message m;
        Random rand = new Random();
        this.localClock.event();
        int delayTime = rand.nextInt(5 * this.time_unit) + 5 * this.time_unit;
        try {
            Thread.sleep(delayTime);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] select_quorum = this.quorum[rand.nextInt(15)];
        String[] select_servers = new String[select_quorum.length];
        for(int i = 0; i < select_servers.length; i ++) {
        	select_servers[i] = this.serverIDs.get(select_quorum[i]);
        }
        int ts = this.localClock.get_time();
        
        m = new Message("REQUEST", this.clientID, "REQUEST", ts, null, this.clientIP, this.clientPort);
        
        mutex.send_request(m, select_servers);
        return select_servers;
    }
    
    // Broadcast both request and release.
    public synchronized void broadCast(Message m, String [] select_servers) {
        System.out.println(" [broadCast]: " + m);
        //this.writeToClientFile(" [broadCast]: " + m);
        this.localClock.event();
        for (String sID : select_servers) {
            String target = this.servers.get(sID);
            System.out.println(" target address is " + target);
            
            //this.writeToClientFile(" target address is " + target);
            this.msg_count_send ++;
            this.send_message(target, m);
        }
        
    }
    // function for sending the message to server.
    public synchronized void send_message(String targetAddr, Message m) {
        System.out.println(" [Send] Sends message to " + targetAddr + " with Message - " + m);
        
        //this.writeToClientFile(" [Send] Sends message to " + targetAddr + " with Message - " + m);
        String[] addr = targetAddr.split(":");
        try {
            Socket sock = new Socket(addr[0], Integer.parseInt(addr[1]));
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
    
    // function for the delivered message
    public synchronized void deliver_message(Message m) {
    	this.localClock.message(m.getTs());
        this.mutex.receive_server(m);
    }
    
    // this is just for checking at the start, to check whether all the server are on
    public synchronized void receive_enquiry(Message m) {
        this.pending_servers.remove(m.getID());
        System.out.println("[Filenames-Get]: Removed " + m.getID() + " from pending_servers");
        //this.writeToClientFile("[Filenames-Get]: Removed " + m.getID() + " from pending_servers");
    }
    
    // send release message
    public synchronized void send_release(String[] select_servers) {
    	Message m;
        this.localClock.event();
        
        int ts = this.localClock.get_time();
        
        m = new Message("RELEASE", this.clientID, "RELEASE", ts, null, this.clientIP, this.clientPort);
        
        this.broadCast(m, select_servers);
    }

    public static void main(String[] args) {
        Client c = new Client(args[0], args[1], args[2], args[3]);
        Thread t = new Thread(c);
        t.start();
    }
}

