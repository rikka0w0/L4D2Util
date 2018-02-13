package rikka.l4d2util;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import rikka.android.InputBox;
import rikka.l4d2util.common.L4D2Server;
import rikka.l4d2util.common.ServerList;
import rikka.l4d2util.common.ServerObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
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
        listviewServerList.setOnItemClickListener(this);
        listviewServerList.setOnItemLongClickListener(this);

        // Refresh immediately
        RefreshAllAsync();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshAllAsync();
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
                    ServerObject server = serverList.add(text);
                    serverList.save();
                    RefreshServerInfoAsync(server);
                }
            }
        });
    }

    public void onShowAboutClick(MenuItem item) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
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
                    case R.id.action_edit_server:
                        InputBox.show(
                                MainActivity.this,
                                getString(R.string.message_edit_server),
                                getString(R.string.action_edit_server),
                                server.hostname+":"+String.valueOf(server.port),
                                new InputBox.IInputBoxHandler() {
                                    @Override
                                    public void onClose(String text) {
                                        if (text != null) {
                                            ServerObject newServer = serverList.replace(position, text);
                                            serverList.save();
                                            RefreshServerInfoAsync(newServer);
                                        }
                                    }
                        });
                        break;
                }
                return false;
            }
        });

        popup.show();

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ServerObject server = (ServerObject ) parent.getAdapter().getItem(position);

        Intent intent = new Intent(this, ServerDetailActivity.class);
        intent.putExtra("hostname", server.hostname);
        intent.putExtra("port", server.port);
        startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    private static class RefreshServerInfoAsyncTask extends AsyncTask<Object, Object, L4D2Server.Info> {
        private final MainActivity context;
        private final ServerObject server;

        public RefreshServerInfoAsyncTask(MainActivity context, ServerObject server) {
            this.context = context;
            this.server = server;
        }

        @Override
        protected L4D2Server.Info doInBackground(Object... useless) {
            return L4D2Server.QueryServerInfo(server.hostname, server.port);
        }

        @Override
        protected void onPostExecute(L4D2Server.Info info) {
            if (info == null) {
                server.subText = context.getString(R.string.subtext_unable_to_contact_server);
                server.contacted = false;
            } else {
                server.subText =
                        " (" + String.valueOf(info.playerCount) + "/" +
                        String.valueOf(info.slotCount) + ") " + info.serverName +"\n" + info.mapName;
                server.contacted = true;
            }

            context.serverList.syncView();
        }
    }

    public void RefreshServerInfoAsync(ServerObject server) {
        server.subText = getString(R.string.subtext_querying);
        serverList.syncView();
        (new RefreshServerInfoAsyncTask(this, server)).execute();
    }

    public void RefreshAllAsync() {
        for (ServerObject server: serverList.getList()) {
            RefreshServerInfoAsync(server);
        }
    }
}
