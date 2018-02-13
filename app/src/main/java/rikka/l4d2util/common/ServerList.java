package rikka.l4d2util.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rikka.android.TextFile;

/**
 * Created by Administrator on 2/13/2018.
 */

public class ServerList {
    public final String cfgFilePath;
    private final IServerListHandler eventHandler;
    private final List<ServerObject> list;

    public interface IServerListHandler {
        void onListChanged();
    }

    public ServerList(String parentDir, IServerListHandler eventHandler) {
        this.cfgFilePath = parentDir + "/config.json";
        this.eventHandler = eventHandler;
        this.list = new ArrayList<>();
    }

    public void add(String url) {
        int pos = url.indexOf(':');
        if (pos >= 0) {
            String hostname = url.substring(0, pos);
            String portString = url.substring(pos + 1);
            int port;
            try {
                port = Integer.parseInt(portString);
            } catch (Exception e) {
                port = 27015;
            }
            add(hostname, port);
        } else {
            add(url, 27015);
        }
    }

    public void add(String hostname, int port) {
        addImpl(hostname, port);
        syncView();
    }

    private void addImpl(String hostname, int port) {
        ServerObject serverObject = new ServerObject();
        serverObject.hostname = hostname;
        serverObject.port = port;
        serverObject.text = hostname + ":" + String.valueOf(port);
        list.add(serverObject);
    }

    public ServerObject remove(int index) {
        ServerObject ret = list.remove(index);
        syncView();
        return ret;
    }

    public boolean remove(ServerObject object) {
        boolean ret = list.remove(object);
        syncView();
        return ret;
    }

    public boolean contains(ServerObject object) {
        return list.contains(object);
    }

    public List<ServerObject> getList(){
        return  this.list;
    }

    public void syncView() {
        eventHandler.onListChanged();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void load() {
        list.clear();
        try {
            String cfgJSONString = TextFile.read(cfgFilePath);
            JSONObject root = new JSONObject(cfgJSONString);
            JSONArray serverArray = root.getJSONArray("servers");

            for (int i = 0; i < serverArray.length(); i++) {

                JSONObject serverConfig = serverArray.getJSONObject(i);
                String hostname = serverConfig.getString("hostname");
                int port = serverConfig.getInt("port");
                addImpl(hostname, port);
            }

            syncView();
        } catch (Exception exception) {
            if (exception instanceof java.io.FileNotFoundException){
                //serverList.add("motherfvcker.261day.com", 27015);
                save();
            }
        }
    }

    public void save() {
        JSONObject root = new JSONObject();
        JSONArray serverArray = new JSONArray();

        try {
            for (int i = 0; i < list.size(); i++) {
                ServerObject serverObject = list.get(i);
                JSONObject serverConfig = new JSONObject();
                serverConfig.put("hostname", serverObject.hostname);
                serverConfig.put("port", serverObject.port);
                serverArray.put(serverConfig);
            }
            root.put("servers", serverArray);
            TextFile.save(cfgFilePath, root.toString());
        } catch (Exception exception) {
            return;
        }
    }
}
