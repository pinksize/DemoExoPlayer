package com.example.alex.demoexoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VetvStreamActivity extends AppCompatActivity {

    public static final String EXTRA_DATA_URI = "data_uri";

    private SimpleExoPlayer player;
    private VetvStreamPlayer playerView;
    private Uri dataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent != null){
            dataUri = intent.getData();
            if (dataUri != null){
                // 1. create a default TrackSelector
                Handler mainHandler = new Handler();
                DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                AdaptiveVideoTrackSelection.Factory factory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector = new DefaultTrackSelector(mainHandler, factory);

                // 2. create default LoadControl
                LoadControl loadControl = new DefaultLoadControl();

                // 3. create a player
                player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

                playerView = (VetvStreamPlayer) findViewById(R.id.playerView);
                playerView.setPlayer(player);
                playerView.setUseController(true);

                // create dataUri sourcefactory
                String userAgent = Util.getUserAgent(this, "DemoExoPlayer");
                DefaultHttpDataSourceFactory httpDataSourceFactory =
                        new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter, 3000, 3000, true);
                DefaultDataSourceFactory sourceFactory =
                        new DefaultDataSourceFactory(this, bandwidthMeter, httpDataSourceFactory);

                // create extractor to extract media samples
                DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                // create media from extractor
                MediaSource hlsMediaSource = new HlsMediaSource(dataUri, sourceFactory, mainHandler, null);

                player.prepare(hlsMediaSource);

                // start play ASAP
                player.setPlayWhenReady(true);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerView.release();
    }
}
