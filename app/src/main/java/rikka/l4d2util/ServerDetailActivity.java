package rikka.l4d2util;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import rikka.l4d2util.common.L4D2Server;

public class ServerDetailActivity extends AppCompatActivity {
    private String hostname;
    private int port;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_detail);
        textView = (TextView) findViewById(R.id.text_server_detail) ;

        Intent intent = getIntent();
        this.hostname = intent.getStringExtra("hostname");
        this.port = intent.getIntExtra("port", 27015);

        setTitle(hostname);
        refreshDetail();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void refreshDetail() {
        textView.setText(R.string.toast_refreshing);
        (new RefreshServerDetailAsyncTask(this, hostname, port)).execute();
    }

    private static class RefreshServerDetailAsyncTask extends AsyncTask<Object, Object, Object[]> {
        private final ServerDetailActivity context;
        private final String hostname;
        private final int port;

        public RefreshServerDetailAsyncTask(ServerDetailActivity context, String hostname, int port) {
            this.context = context;
            this.hostname = hostname;
            this.port = port;
        }

        @Override
        protected Object[] doInBackground(Object... useless) {
            return new Object[] {
                    L4D2Server.QueryServerInfo(hostname, port),
                    L4D2Server.GetPlayerList(hostname, port)
            };
        }

        @Override
        protected void onPostExecute(Object[] results) {
            L4D2Server.Info info = (L4D2Server.Info) results[0];
            if (info == null) {
                context.textView.setText(R.string.subtext_unable_to_contact_server);
            } else {
                context.setTitle(info.serverName);
                String text =
                        context.getString(R.string.text_server_address) + ": \n" + hostname + ":" + String.valueOf(port) + "\n" +
                        context.getString(R.string.text_server_name) + ": \n" + info.serverName + "\n" +
                        context.getString(R.string.text_map_name) + ": \n" + info.mapName + "\n" +
                        context.getString(R.string.text_online_player) + " (" +
                        String.valueOf(info.playerCount) + "/" + String.valueOf(info.slotCount) + ") :";

                String[] players = (String[]) results[1];
                for (int i=0; i<players.length; i++) {
                    text += "\n" + String.valueOf(i+1) + "." + players[i];
                }
                context.textView.setText(text);
            }
        }
    }
}
