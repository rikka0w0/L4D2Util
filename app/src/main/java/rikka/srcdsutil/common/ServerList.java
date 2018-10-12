package rikka.srcdsutil.common;

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

    public ServerObject add(String url) {
        ServerObject serverObject = new ServerObject(url);
        list.add(serverObject);
        syncView();
        return serverObject;
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

    public ServerObject replace(int index, String url) {
        ServerObject ret = new ServerObject(url);
        list.set(index, ret);
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
                String password = "";
                if (serverConfig.has("password")) {
                    password = serverConfig.getString("password");
                }
                ServerObject server = new ServerObject(hostname, port);
                server.password = password;
                list.add(server);
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
                serverConfig.putOpt("password", serverObject.password);
                serverArray.put(serverConfig);
            }
            root.put("servers", serverArray);
            TextFile.save(cfgFilePath, root.toString());
        } catch (Exception exception) {
            return;
        }
    }
}
