package com.trunghoang.youtubeplaylistmanager;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private ArrayList<VideoModel> videoModels;
    private static final String TAG = "VideosAdapter";
    private static Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
    private static ThumbInitListener thumbInitListener;

    public VideosAdapter(ArrayList<VideoModel> videoItemsData) {
        videoModels = videoItemsData;
        thumbInitListener = new ThumbInitListener();
        thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
    }

    @NonNull
    @Override
    public VideosAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_view, parent, false);
        return new VideoViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bindData(videoModels.get(position));
    }

    @Override
    public int getItemCount() {
        return videoModels.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            ((LinearLayoutManager) recyclerView.getLayoutManager()).setRecycleChildrenOnDetach(true);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        releaseLoaders();
    }

    public void releaseLoaders() {
        for (YouTubeThumbnailLoader loader: thumbnailViewToLoaderMap.values()) {
            loader.release();
        }
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        YouTubeThumbnailView thumbnailView;
        TextView titleView;
        TextView metaInfo;

        private String currentVideoId;

        public VideoViewHolder(View layout) {
            super(layout);
            thumbnailView = layout.findViewById(R.id.thumbnailView);
            titleView = layout.findViewById(R.id.titleView);
            metaInfo = layout.findViewById(R.id.metaInfo);
        }

        public void bindData(VideoModel videoModel) {
            currentVideoId = videoModel.getVideoId();
            try {
                YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(thumbnailView);
                thumbnailView.setTag(currentVideoId);
                loader.setVideo(currentVideoId);
            } catch (NullPointerException e) {
                thumbnailView.setTag(currentVideoId);
                thumbnailView.initialize("AIzaSyDTO434OJxLmsPiPb1tq2C4FoKE2q-97sg", thumbInitListener);
            }
            titleView.setText(videoModel.getTitle());
            metaInfo.setText(videoModel.getChannelTitle());
        }

    }

    private class ThumbInitListener implements
            YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailLoader.OnThumbnailLoadedListener {

        @Override
        public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
            youTubeThumbnailLoader.setOnThumbnailLoadedListener(this);
            thumbnailViewToLoaderMap.put(youTubeThumbnailView, youTubeThumbnailLoader);
            youTubeThumbnailLoader.setVideo(youTubeThumbnailView.getTag().toString());
        }

        @Override
        public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
        }

        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
        }
    }

}
