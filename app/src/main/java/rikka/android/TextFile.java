package rikka.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Rikka0w0 on 2/13/2018.
 */

public class TextFile {
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String read(String filePath) throws Exception {
        FileInputStream fin = new FileInputStream(filePath);
        String ret = convertStreamToString(fin);
        fin.close();
        return ret;
    }

    public static void save(String filePath, String content) throws Exception {
        FileWriter fout = new FileWriter(new File(filePath));
        fout.write(content);
        fout.close();
    }
}
