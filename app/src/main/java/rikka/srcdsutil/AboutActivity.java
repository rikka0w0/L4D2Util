package rikka.srcdsutil;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void onRikkaClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://github.com/rikka0w0/L4D2Util")));
    }
}
