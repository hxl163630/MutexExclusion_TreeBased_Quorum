
import java.io.Serializable;

// message class for transfer messages between client and server
public class Message implements Comparable<Message>, Serializable {
    private String type;
    private String ID;
    private String opt;
    private int timeStamp;
    private String serverID;
    private String IP;
    private int port;

    public Message(String type, String ID, String opt, int timeStamp, String serverID, String IP, int port) {
        this.type = type;
        this.ID = ID;
        this.opt = opt;
        this.timeStamp = timeStamp;
        this.serverID = serverID;
        this.IP = IP;
        this.port = port;
    }

    @Override
    public int compareTo(Message m) {
        if (this.timeStamp > m.timeStamp) {
            return 1;
        }
        if (this.timeStamp < m.timeStamp) {
            return -1;
        }
        return (int)(this.ID.charAt(8) - m.getID().charAt(8));
    }

    public String getType() {
        return this.type;
    }

    public String getID() {
        return this.ID;
    }

    public String getOpt() {
        return this.opt;
    }

    public int getTs() {
        return this.timeStamp;
    }

    public String getTarget() {
        return this.serverID;
    }


    public String getIP() {
        return this.IP;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return "Message from " + this.ID + " " + this.type + " " + this.opt + " timeStamp:" + this.timeStamp + " " + this.serverID + " " + this.IP + ":" + this.port;
    }
}

