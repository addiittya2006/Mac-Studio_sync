package se.popcorn_time.base.config;

public class VPN {

    public String host;
    public int port;
    public String user;
    public String pass;

    public VPN() {
        this("", 0, "", "");
    }

    public VPN(String host, int port, String user, String pass) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }
}