package rikka.l4d2util.common;

/**
 * Created by Administrator on 2/13/2018.
 */

public class ServerObject {
    // Saved data
    public String hostname;
    public int port;

    // Sensed data
    public String serverName;
    public String mapName;
    public int onlinePlayer;
    public int availableSlots;

    // Display data
    public String text;
    public String subText;

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
