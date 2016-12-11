package com.example.alex.demoexoplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VetvStreamPlayer extends FrameLayout {

    private final String TAG = this.getClass().getSimpleName();
    private Uri hlsUri;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private AspectRatioFrameLayout videoFrame;
    private VetvStreamPlayerController controller;
    private boolean useTextureView;
    private View surfaceView;
    private ComponentListener componentListener;
    private boolean useController;

    public VetvStreamPlayer(Context context) {
        this(context, null);
    }

    public VetvStreamPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VetvStreamPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.stream_player, this);

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame);
        videoFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        controller = (VetvStreamPlayerController) findViewById(R.id.control);

        componentListener = new ComponentListener();

        View view = useTextureView ? new TextureView(context) : new SurfaceView(context);
        LayoutParams layoutParams =
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        videoFrame.addView(view, 0);
        view.setId(R.id.id_surface_view);
        surfaceView = view;
    }

    public void setUseTextureView(boolean useTextureView) {
        this.useTextureView = useTextureView;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(SimpleExoPlayer player) {
        if (this.player == player)
            return;

        if (this.player != null) {
            // remove listeners of previous player
            this.player.setVideoListener(null);
            this.player.setVideoSurface(null);
        }

        this.player = player;
        if (this.player != null) {
            if (useController) {
                controller.setPlayer(player);
            }

            if (surfaceView instanceof SurfaceView) {
                player.setVideoSurfaceView((SurfaceView) surfaceView);
            } else if (surfaceView instanceof TextureView) {
                player.setVideoTextureView((TextureView) surfaceView);
            }

            this.player.setVideoListener(componentListener);
        } else {
            controller.hideControls();
        }
    }

    public void setUseController(boolean useController) {
        if (this.useController == useController)
            return;
        this.useController = useController;
        if (useController) {
            controller.setPlayer(player);
        } else {
            controller.hideControls();
            controller.setPlayer(null);
        }
    }

    public void setControllerVisibilityListener(VetvStreamPlayerController.VisibilityListener listener) {
        controller.setVisibilityListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!useController || player == null || event.getActionMasked() != MotionEvent.ACTION_DOWN)
            return false;

        // ACTION _DOWN
        if (controller.isVisible())
            controller.hideControls();
        else controller.showControls();
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return useController ? controller.dispatchKeyEvent(event) : super.dispatchKeyEvent(event);
    }

    protected void release() {
        if (this.player != null) {
            // remove listeners of previous player
            this.player.setVideoListener(null);
            this.player.setVideoSurface(null);
            this.player.release();
        }

    }

    private class ComponentListener implements SimpleExoPlayer.VideoListener {

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            Log.d(TAG, "onVideoSizeChanged() called with: width = [" + width + "], height = [" + height +
                    "], unappliedRotationDegrees = [" + unappliedRotationDegrees + "], pixelWidthHeightRatio = [" +
                    pixelWidthHeightRatio + "]");
            videoFrame.setAspectRatio(height == 0 ? 1 : width * pixelWidthHeightRatio / height);
        }

        @Override
        public void onRenderedFirstFrame() {

        }

        @Override
        public void onVideoTracksDisabled() {

        }
    }
}
