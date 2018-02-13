package rikka.l4d2util.common;

/**
 * Created by Rikka0w0 on 2/13/2018.
 */

public class ServerObject{
    // Saved data
    public final String hostname;
    public final int port;

    // Sensed data
    public boolean contacted = false;

    // Display data
    public final String text;
    public String subText;

    public ServerObject(String url) {
        int pos = url.indexOf(':');
        if (pos >= 0) {
            this.hostname = url.substring(0, pos);
            String portString = url.substring(pos + 1);
            int port;
            try {
                port = Integer.parseInt(portString);
            } catch (Exception e) {
                port = 27015;
            }
            this.port = port;
        } else {
            this.hostname = url;
            this.port = 27015;
        }

        this.text = hostname + ":" + String.valueOf(port);
    }

    public ServerObject(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        this.text = hostname + ":" + String.valueOf(port);
    }

    @Override
    public String toString() {
        return hostname + ":" + String.valueOf(port);
    }

    @Override
    public boolean equals(Object in) {
        if (!(in instanceof ServerObject))
            return false;

        return this.toString().equals(in.toString());
    }
}
