
package com.example.alex.demoexoplayer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = new Intent(this, VetvStreamActivity.class);
        intent.setData(Uri.parse("https://edge-120-2.vetv.vn/mobile/lolvntv1.m3u8"));
        startActivity(intent);
    }
}
