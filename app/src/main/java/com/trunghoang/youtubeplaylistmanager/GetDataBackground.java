package com.trunghoang.youtubeplaylistmanager;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GetDataBackground extends AsyncTask<Void, Void, ArrayList<VideoModel>> {

    private static final String TAG = "GetDataBackground";

    private YouTube youTube = null;
    private Exception mLastError = null;

    interface DataDownloadListener {
        void onDataDownloaded(ArrayList<VideoModel> results);
        void onDownloadFailed(Exception error);
    }

    private DataDownloadListener dataDownloadListener;

    GetDataBackground(GoogleAccountCredential credential, DataDownloadListener dataDownloadListener) {

        this.dataDownloadListener = dataDownloadListener;

        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        youTube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                .build();
    }

    @Override
    protected ArrayList<VideoModel> doInBackground(Void... voids) {
        try {
            Log.d(TAG, "doInBackground: downloading data");
            return getDataFromApi();
        } catch (Exception e) {
            e.printStackTrace();
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(ArrayList<VideoModel> results) {
        dataDownloadListener.onDataDownloaded(results);
    }

    @Override
    protected void onCancelled() {
        dataDownloadListener.onDownloadFailed(mLastError);
    }

    private ArrayList<VideoModel> getDataFromApi() throws IOException {
        PlaylistItemListResponse result = youTube.playlistItems().list("contentDetails,snippet")
                .setMaxResults(25L)
                .setPlaylistId("PL2qWSIR-0wJ5FmtjiBLa-Tv3rCAP0S_58")
                .execute();
        List<PlaylistItem> items = result.getItems();
        Iterator<PlaylistItem> iterator = items.iterator();
        ArrayList<VideoModel> results = new ArrayList<>();
        while (iterator.hasNext()) {
            PlaylistItem item = iterator.next();
            VideoModel videoModel = new VideoModel();
            videoModel.setVideoId(item.getContentDetails().getVideoId());
            videoModel.setTitle(item.getSnippet().getTitle());
            videoModel.setChannelTitle(item.getSnippet().getChannelTitle());
            results.add(videoModel);
        }
        return results;
    }
}