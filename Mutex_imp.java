
import java.util.ArrayList;
import java.util.Arrays;

public class Mutex_imp {
    private Client client;
    private String file;
    public ArrayList<String> pending_reply;
    private boolean finished;
    private int haveFinished;
    private Message msg;

    public Mutex_imp(Client client) {
        this.client = client;
        this.file = "file.txt";
        this.finished = false;
        this.haveFinished = 0;
    }
    
    public Message getMsg() {
    	return this.msg;
    }
    
    public String getFile() {
        return this.file;
    }

    public boolean getFinished() {
        return this.finished;
    }

    public void setFinished(boolean b) {
        this.finished = b;
    }

    public int getHaveFinished() {
        return this.haveFinished;
    }

    public void setHaveFinished(int n) {
        this.haveFinished = 0;
    }

    
    // receive message from server
    public synchronized void receive_server(Message m) {
        if (m.getOpt().equals("ENQUIRY")) {
            this.client.receive_enquiry(m);
        } else if (m.getOpt().equals("GRANT")) {
        	this.pending_reply.remove(m.getID());
            System.out.println(" [RECEIVE-GRANT-FROM-SERVER]: Get GRANT from Server " + m.getID());
            this.client.msg_count_receive ++;
            //this.client.writeToClientFile(" [RECEIVE-GRANT-FROM-SERVER]: Get GRANT from Server " + m.getID());
        } else {
            System.err.println("[ERROR]: SERVER Type with wrong opt!");
            //this.client.writeToClientFile("[ERROR]: SERVER Type with wrong opt!");
        }
    }

    public synchronized void send_request(Message m, String [] select_servers) {
    	// when client is sending request
        
        this.msg = m;
        this.pending_reply = new ArrayList<>(Arrays.asList(select_servers));
        this.client.broadCast(m, select_servers);
        
    }
    
    public synchronized boolean isAllowedInCS() {
        if (this.pending_reply.isEmpty()) {
            return true;
        }
        return false;
    }
   
}

