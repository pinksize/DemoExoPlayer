package com.example.alex.demoexoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;

public class VetvStreamPlayerController extends FrameLayout {

    private final String TAG = this.getClass().getSimpleName();
    public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
    private boolean isAttachedToWindow;
    private ComponentListener componentListener;
    private Runnable hideAction = new Runnable() {
        @Override
        public void run() {
            hideControls();
        }
    };
    private long showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;

    /**
     * hide control buttons
     */
    public void hideControls() {
        if (isVisible()) {
            setVisibility(GONE);

            if (visibilityListener != null)
                visibilityListener.onVisibilityChange(getVisibility());

            removeCallbacks(hideAction);
        }
    }

    /**
     * show control buttons and hide it after period of time
     */
    public void showControls() {
        if (!isVisible()) {
            setVisibility(VISIBLE);

            if (visibilityListener != null)
                visibilityListener.onVisibilityChange(getVisibility());

            updateUI();
        }

        hideControllerAfterTimeout();
    }

    /**
     * Listener to be notified about changes of the visibility of the UI control.
     */
    public interface VisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either {@link View#VISIBLE} or {@link View#GONE}.
         */
        void onVisibilityChange(int visibility);
    }

    private ExoPlayer player;
    private VisibilityListener visibilityListener;

    private ImageButton playBtn;

    public VetvStreamPlayerController(Context context) {
        this(context, null);
    }

    public VetvStreamPlayerController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VetvStreamPlayerController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.stream_player_controller, this);

        componentListener = new ComponentListener();
        playBtn = (ImageButton) findViewById(R.id.play);
        playBtn.setOnClickListener(componentListener);
    }

    public void setVisibilityListener(VisibilityListener visibilityListener) {
        this.visibilityListener = visibilityListener;
    }

    public ExoPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ExoPlayer player) {
        if (this.player == player)
            return;

        if (this.player != null)
            this.player.removeListener(componentListener);

        this.player = player;
        if (player != null)
            player.addListener(componentListener);

        updateUI();
    }

    private void updateUI() {
        updatePlayPauseButton();
    }

    private final class ComponentListener implements ExoPlayer.EventListener, OnClickListener {

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updatePlayPauseButton();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            // do nothing
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.d(TAG, "Cannot play video");
            error.printStackTrace();
        }

        @Override
        public void onPositionDiscontinuity() {
            // do nothing
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.play:
                if (player != null)
                    player.setPlayWhenReady(!player.getPlayWhenReady());
                break;
            }

            hideControllerAfterTimeout();
        }
    }

    private void hideControllerAfterTimeout() {
        // remove previously action
        removeCallbacks(hideAction);

        if (isAttachedToWindow) {
            postDelayed(hideAction, showTimeoutMs);
        }
    }

    private void updatePlayPauseButton() {
        if (!isVisible() || !isAttachedToWindow) {
            return;
        }

        boolean isPlaying = player != null && player.getPlayWhenReady();
        playBtn.setImageResource(isPlaying ? R.drawable.exo_controls_pause : R.drawable.exo_controls_play);
    }

    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;

        postDelayed(hideAction, showTimeoutMs);

        updateUI();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        isAttachedToWindow = false;

        removeCallbacks(hideAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player == null || event.getAction() != KeyEvent.ACTION_DOWN)
            return super.dispatchKeyEvent(event);

        switch (event.getAction()) {
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            if (player != null)
                player.setPlayWhenReady(!player.getPlayWhenReady());
            break;
        case KeyEvent.KEYCODE_MEDIA_PLAY:
            if (player != null)
                player.setPlayWhenReady(true);
            break;
        case KeyEvent.KEYCODE_MEDIA_PAUSE:
            if (player != null)
                player.setPlayWhenReady(false);
            break;
        default:
            return super.dispatchKeyEvent(event);
        }

        showControls();
        return true;
    }
}
