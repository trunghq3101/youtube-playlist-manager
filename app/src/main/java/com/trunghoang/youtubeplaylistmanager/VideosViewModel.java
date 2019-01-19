package com.trunghoang.youtubeplaylistmanager;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.util.ArrayList;

class VideosViewModel extends ViewModel {

    private MutableLiveData<DataWrapper<ArrayList<VideoModel>>> videos;
    private GoogleAccountCredential googleAccountCredential;

    class DataWrapper<T> {
        T data;
        Exception error;

        public DataWrapper(T data, Exception error) {
            this.data = data;
            this.error = error;
        }
    }

    MutableLiveData<DataWrapper<ArrayList<VideoModel>>> getVideos() {
        if (videos == null) {
            videos = new MutableLiveData<DataWrapper<ArrayList<VideoModel>>>();
            loadVideosFromPlaylist();
        }
        return videos;
    }

    public void setGoogleAccountCredential(GoogleAccountCredential googleAccountCredential) {
        this.googleAccountCredential = googleAccountCredential;
    }

    public void loadVideosFromPlaylist() {
        if (googleAccountCredential.getSelectedAccountName() == null) {
            videos.setValue(new DataWrapper<ArrayList<VideoModel>>(null, new Exception("NO_ACCOUNT_NAME")));
        } else {
            GetDataBackground getDataBackground = new GetDataBackground(googleAccountCredential, new GetDataBackground.DataDownloadListener() {
                @Override
                public void onDataDownloaded(ArrayList<VideoModel> results) {
                    videos.setValue(new DataWrapper<ArrayList<VideoModel>>(results, null));
                }

                @Override
                public void onDownloadFailed(Exception error) {
                    videos.setValue(new DataWrapper<ArrayList<VideoModel>>(null, error));
                }
            });
            getDataBackground.execute();
        }
    }
}
