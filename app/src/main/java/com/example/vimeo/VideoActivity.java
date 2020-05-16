package com.example.vimeo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.vimeo.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class VideoActivity extends AppCompatActivity {

    SimpleExoPlayerView playerView;
    SimpleExoPlayer exoPlayer;
    PlaybackStateCompat.Builder playbackStateCompactBuilder;
    MediaSessionCompat mediaSessionCompat;

    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        path = getIntent().getStringExtra("path");

        playerView = findViewById(R.id.simpleExoPlayerView);
        initializePlayer();



    }

    private void initializePlayer(){
        DefaultTrackSelector defaultTrackSelector=  new DefaultTrackSelector();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getBaseContext(),defaultTrackSelector);
        playerView.setPlayer(exoPlayer);

        Uri uri = Uri.parse(path);

        String userAgent = Util.getUserAgent(getBaseContext(),"Exo");
        MediaSource mediaSource = new ExtractorMediaSource(uri, new DefaultDataSourceFactory(getBaseContext(), userAgent),
                new DefaultExtractorsFactory(),
                null,
                null);

        exoPlayer.prepare(mediaSource);

        ComponentName componentName = new ComponentName(getBaseContext(), "Exo");
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(),"Exoplayer",componentName, null);
        playbackStateCompactBuilder = new PlaybackStateCompat.Builder();
        playbackStateCompactBuilder.setActions(PlaybackStateCompat.ACTION_PLAY);
        playbackStateCompactBuilder.setActions(PlaybackStateCompat.ACTION_PAUSE);
        playbackStateCompactBuilder.setActions(PlaybackStateCompat.ACTION_FAST_FORWARD);

        mediaSessionCompat.setPlaybackState(playbackStateCompactBuilder.build());
        mediaSessionCompat.setActive(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    private void releasePlayer(){
        if (exoPlayer!=null){
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

}
