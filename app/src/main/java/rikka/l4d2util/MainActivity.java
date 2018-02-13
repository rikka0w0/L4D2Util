package rikka.l4d2util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;


import java.util.ArrayList;

import rikka.android.InputBox;
import rikka.l4d2util.common.L4D2Server;
import rikka.l4d2util.common.ServerList;
import rikka.l4d2util.common.ServerObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener{
    protected ServerList serverList;
    protected ServerListAdapter serverListAapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // ServerList
        serverList = new ServerList(this.getFilesDir().getAbsolutePath(), new ServerList.IServerListHandler() {
            @Override
            public void onListChanged() {
                MainActivity.this.serverListAapter.notifyDataSetChanged();
            }
        });
        serverList.load();

        serverListAapter = new ServerListAdapter(this, serverList.getList());
        ListView listviewServerList = (ListView) findViewById(R.id.serverlist);
        listviewServerList.setAdapter(serverListAapter);
        listviewServerList.setOnItemLongClickListener(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ServerObject server: serverList.getList()) {
                    RefreshServerInfoAsync(server);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddServerClick(MenuItem item) {
        InputBox.show(this, getString(R.string.message_add_server), getString(R.string.action_add_server), "", new InputBox.IInputBoxHandler() {
            @Override
            public void onClose(String text) {
                if (text != null) {
                    serverList.add(text);
                    serverList.save();
                }
            }
        });
    }

    public void RefreshServerInfoAsync(final ServerObject... serverObjects) {
        for (ServerObject server: serverObjects) {
            server.subText = getString(R.string.subtext_querying);
        }
        serverList.syncView();

        (new AsyncTask<Object, Integer, ArrayList<L4D2Server.Info>>() {
            @Override
            protected ArrayList<L4D2Server.Info> doInBackground(Object... useless) {
                ArrayList<L4D2Server.Info> results = new ArrayList<>(serverObjects.length);
                for (int i=0; i<serverObjects.length; i++) {
                    ServerObject server = serverObjects[i];
                    results.add(i, L4D2Server.QueryServerInfo(server.hostname, server.port));
                }

                return results;
            }

            @Override
            protected void onPostExecute(ArrayList<L4D2Server.Info> result) {
                for (int i=0; i<serverObjects.length; i++) {
                    ServerObject server = serverObjects[i];
                    L4D2Server.Info info = result.get(i);

                    if (info == null) {
                        server.subText = getString(R.string.subtext_unable_to_contact_server);
                    } else {
                        server.subText =
                                "(" + String.valueOf(info.playerCount) + "/" +
                                        String.valueOf(info.slotCount) + ") " + info.mapName;
                    }
                }

                serverList.syncView();
            }
        }).execute(serverObjects);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        final ServerObject server = (ServerObject ) parent.getAdapter().getItem(position);

        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.menu_serverlist, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_delete_server:
                        serverList.remove(server);
                        serverList.save();
                        break;
                    case R.id.action_refresh_server:
                        RefreshServerInfoAsync(server);
                        break;
                }
                return false;
            }
        });

        popup.show();


        return true;
    }
}
