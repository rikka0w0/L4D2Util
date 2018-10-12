package rikka.srcdsutil;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import rikka.srcdsutil.common.L4D2Rcon;

public class RconActivity extends AppCompatActivity {
    private String hostname;
    private int port;
    private String password;
    private volatile L4D2Rcon rcon;

    Button btnSend;
    EditText editTextRconCommand;
    TextView textViewRconResponce;
    ScrollView scroll;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rcon, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rcon);

        btnSend = (Button) findViewById(R.id.btnSend);
        editTextRconCommand = (EditText) findViewById(R.id.text_rconCommand);
        textViewRconResponce = (TextView) findViewById(R.id.text_rconResponce);
        scroll = (ScrollView) findViewById(R.id.scroll_rconResponce);

        Intent intent = getIntent();
        this.hostname = intent.getStringExtra("hostname");
        this.port = intent.getIntExtra("port", 27015);
        this.password = intent.getStringExtra("password");

        setTitle(R.string.action_rcon);

        btnSend.setEnabled(false);
        textViewRconResponce.append(hostname + ":" + port + "\n");
        textViewRconResponce.append(getString(R.string.rcon_status_authorizing));
        (new RConAuthAsyncTask(this, hostname, port, password)).execute();
    }

    private static class RConAuthAsyncTask extends AsyncTask<Object, Object, L4D2Rcon> {
        private final String hostname;
        private final int port;
        private final String password;

        private final RconActivity rconActivity;

        public RConAuthAsyncTask(RconActivity rconActivity, String hostname, int port, String password) {
            this.hostname = hostname;
            this.port = port;
            this.password = password;

            this.rconActivity = rconActivity;
        }

        @Override
        protected L4D2Rcon doInBackground(Object... objects) {
            try {
                L4D2Rcon rcon = new L4D2Rcon(hostname, port);
                return rcon.authorize(password) ? rcon : null;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(L4D2Rcon rcon) {
            if (rcon == null) {
                rconActivity.textViewRconResponce.append(rconActivity.getString(R.string.rcon_status_authorize_failed));
            } else {
                rconActivity.rcon = rcon;
                rconActivity.textViewRconResponce.append(rconActivity.getString(R.string.rcon_status_authorize_succeeded) + "\n");
                rconActivity.btnSend.setEnabled(true);
            }
        }
    }

    public void sendButtonClicked(View view) {
        String command = editTextRconCommand.getText().toString();
        if (command.trim().length() == 0)
            return;

        editTextRconCommand.setText("");

        (new RConCommandAsyncTask(this, rcon, command)).execute();
    }

    private static class RConCommandAsyncTask extends AsyncTask<Object, Object, String> {
        private final L4D2Rcon rcon;
        private final String command;

        private final RconActivity rconActivity;
        public RConCommandAsyncTask(RconActivity rconActivity, L4D2Rcon rcon, String command) {
            this.rcon = rcon;
            this.command = command;

            this.rconActivity = rconActivity;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                return rcon.command(command);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            rconActivity.textViewRconResponce.append("> " + command + "\n");
            if (result == null){
                rconActivity.textViewRconResponce.append(rconActivity.getString(R.string.rcon_status_command_failed) + "\n");
            } else {
                rconActivity.textViewRconResponce.append(result + "\n");

                rconActivity.scroll.post(new Runnable() {
                    public void run() {
                            rconActivity.scroll.scrollTo(0, rconActivity.textViewRconResponce.getBottom());
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (rcon != null) {
            RConDisconnectAsyncTask task = new RConDisconnectAsyncTask(rcon);
            this.rcon = null;
            task.execute();
        }
        super.onDestroy();
    }

    private static class RConDisconnectAsyncTask extends AsyncTask<Object, Object, Object> {
        private final L4D2Rcon rcon;
        public RConDisconnectAsyncTask(L4D2Rcon rcon) {
            this.rcon = rcon;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                rcon.disconnect();
            } catch (Exception e) {

            }
            return null;
        }
    }

    public void onClearLogClick(MenuItem item) {
        textViewRconResponce.setText("");
    }
}
