package rikka.srcdsutil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import rikka.android.InputBox;
import rikka.srcdsutil.common.L4D2Server;
import rikka.srcdsutil.common.ServerList;
import rikka.srcdsutil.common.ServerObject;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    protected Handler uiUpdateHandler;
    protected ServerList serverList;
    protected ServerListAdapter serverListAapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiUpdateHandler = new Handler();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        ListView listviewServerList = findViewById(R.id.serverlist);
        listviewServerList.setAdapter(serverListAapter);
        listviewServerList.setOnItemClickListener(this);
        listviewServerList.setOnItemLongClickListener(this);

        // Refresh immediately
        RefreshAllAsync();

        FloatingActionButton fab = findViewById(R.id.fab);
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
        InputBox.show(this, getString(R.string.message_add_server),
                getString(R.string.action_add_server), "",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                new InputBox.IInputBoxHandler() {
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
                                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
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
                    case R.id.action_rcon:
                        if (server.password.trim().length() > 0) {
                            startRcon(server);
                        } else {
                            showEditRconPasswordBox(server, false);
                        }

                        break;
                    case R.id.action_edit_rcon_password:
                        showEditRconPasswordBox(server, true);

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
    private static class RefreshServerInfoThread extends Thread {
        private final MainActivity context;
        private final ServerObject server;

        private RefreshServerInfoThread(MainActivity context, ServerObject server) {
            this.context = context;
            this.server = server;
        }

        @Override
        public void run() {
            final L4D2Server.Info info = L4D2Server.QueryServerInfo(server.hostname, server.port);
            context.uiUpdateHandler.post(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }

    public void RefreshServerInfoAsync(ServerObject server) {
        server.subText = getString(R.string.subtext_querying);
        serverList.syncView();
        (new RefreshServerInfoThread(this, server)).start();
    }

    public void RefreshAllAsync() {
        for (ServerObject server: serverList.getList()) {
            RefreshServerInfoAsync(server);
        }
    }

    public void startRcon(ServerObject server) {
        Intent intent = new Intent(this, RconActivity.class);
        intent.putExtra("hostname", server.hostname);
        intent.putExtra("port", server.port);
        intent.putExtra("password", server.password);
        this.startActivity(intent);
    }

    private void showEditRconPasswordBox(final ServerObject server, final boolean editOnly) {
        InputBox.show(MainActivity.this,
                getString(R.string.rcon_password_hint),
                getString(R.string.rcon_password),
                server.password,
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                new InputBox.IInputBoxHandler() {
                    @Override
                    public void onClose(String text) {
                        if (editOnly) {
                            if (text != null){
                                server.password = text;
                                MainActivity.this.serverList.save();
                            }
                        } else {
                            if (text != null && text.trim().length() > 0) {
                                server.password = text;
                                MainActivity.this.serverList.save();
                                startRcon(server);
                            }
                        }
                    }
                });
    }
}
