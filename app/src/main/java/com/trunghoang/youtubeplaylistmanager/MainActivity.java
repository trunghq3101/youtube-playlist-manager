package com.trunghoang.youtubeplaylistmanager;

import android.Manifest;
import android.accounts.AccountManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.ArrayList;
import java.util.Arrays;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final String PREF_ACCOUNT_NAME = "accountName";
    private final String[] SCOPES = {YouTubeScopes.YOUTUBE_READONLY};

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    public static final String EXTRA_VIDEOID = "com.trunghoang.youtubeplaylistmanager.extra.VIDEOID";

    private static GoogleAccountCredential mCredential;

    private RecyclerView mRecyclerView;
    private static VideosAdapter videosAdapter;
    private static VideosViewModel videosViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        initRecyclerView();

        videosViewModel = ViewModelProviders.of(this).get(VideosViewModel.class);
        videosViewModel.setGoogleAccountCredential(mCredential);
        videosViewModel.getVideos().observe(this, new Observer<VideosViewModel.DataWrapper<java.util.ArrayList<VideoModel>>>() {
            @Override
            public void onChanged(@Nullable VideosViewModel.DataWrapper<ArrayList<VideoModel>> results) {
                if (results.error != null) {
                    if (results.error.getMessage() == "NO_ACCOUNT_NAME") {
                        chooseAccount();
                    } else if (results.error instanceof UserRecoverableAuthIOException) {
                        startActivityForResult(
                            ((UserRecoverableAuthIOException) results.error).getIntent(),
                                MainActivity.REQUEST_AUTHORIZATION);
                    }
                } else {
                    videosAdapter = new VideosAdapter(results.data);
                    mRecyclerView.setAdapter(videosAdapter);
                }
            }
        });
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.videoListView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnItemTouchListener(new RecyclerItemTouchListener(this, new TouchListener() {
            @Override
            public void onTouch(View itemLayout) {
                String videoId = itemLayout.findViewById(R.id.thumbnailView).getTag().toString();
                Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                intent.putExtra(EXTRA_VIDEOID, videoId);
                startActivity(intent);
            }
        }));
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                videosViewModel.setGoogleAccountCredential(mCredential);
                videosViewModel.loadVideosFromPlaylist();
            } else {
                startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        videosViewModel.setGoogleAccountCredential(mCredential);
                        videosViewModel.loadVideosFromPlaylist();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    videosViewModel.setGoogleAccountCredential(mCredential);
                    videosViewModel.loadVideosFromPlaylist();
                } else {
                    chooseAccount();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videosAdapter != null) {
            videosAdapter.releaseLoaders();
        }
    }
}
