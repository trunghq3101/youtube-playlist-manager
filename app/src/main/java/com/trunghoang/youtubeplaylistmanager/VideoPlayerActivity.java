package com.trunghoang.youtubeplaylistmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

public class VideoPlayerActivity extends AppCompatActivity implements YouTubePlayer.OnInitializedListener {
    VideoPlayerFragment videoPlayerFragment;
    String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_activity);
        Intent intent = getIntent();
        videoId = intent.getStringExtra(MainActivity.EXTRA_VIDEOID);
        videoPlayerFragment = (VideoPlayerFragment) getFragmentManager()
                .findFragmentById(R.id.videoPlayerFragment);
        videoPlayerFragment.initialize("AIzaSyDTO434OJxLmsPiPb1tq2C4FoKE2q-97sg", this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            youTubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
            youTubePlayer.loadVideo(videoId);
        } else {
            youTubePlayer.play();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

    }
}
