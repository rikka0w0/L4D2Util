package rikka.l4d2util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import rikka.l4d2util.common.ServerObject;
import rikka.android.TextFile;

/**
 * Created by Rikka0w0 on 2/13/2018.
 */

public class Configuration {
    public static  String cfgFilePath;

    public static void setCfgFilePath(String parentDir) {
        cfgFilePath = parentDir + "/config.json";
    }


}
