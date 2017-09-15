package com.koalatea.thehollidayinn.softwareengineeringdaily.adapters;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.activities.PodcastDetailActivity;
import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MediaPlayer;
import com.koalatea.thehollidayinn.softwareengineeringdaily.audio.MusicProvider;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.fragments.PodCardFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by krh12 on 5/22/2017.
 */

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
    private List<Post> posts = new ArrayList<>();
    private List<MediaBrowserCompat.MediaItem> mediaItemList;
    private PodCardFragment context;
    private Button activeActionButton;
    private MediaPlayer mediaPlayer;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public TextView scoreTextView;
        public Button actionButton;
        public ImageView imageView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.card_title);
//            scoreTextView = (TextView) v.findViewById(R.id.score);
            imageView = (ImageView) v.findViewById(R.id.card_image);
        }
    }

    public PodcastAdapter(Fragment context) {
        mediaItemList = new ArrayList<>();
        this.context = (PodCardFragment) context;
        this.mediaPlayer = MediaPlayer.getInstance(context.getActivity());
    }

    public void add(MediaBrowserCompat.MediaItem item) {
        mediaItemList.add(item);
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        this.notifyDataSetChanged();
    }

    @Override
    public PodcastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        final View view =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_podcast_list, parent, false);

        final ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = viewHolder.getAdapterPosition();
                Post post = posts.get(position);
                Intent intent = new Intent(context.getActivity(), PodcastDetailActivity.class);
                intent.putExtra("POST_ID", post._id);
                context.getActivity().startActivity(intent);
            }
        });

//        actionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final int position = viewHolder.getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION) {
//                    Post post = posts.get(position);
//                    playPostMp3(post);
//                }
//            }
//        });

        return viewHolder;
    }

    private void playPostMp3 (Post post) {
        if (post.mp3 == null || post.mp3.isEmpty()) {
            return;
        }

        String source = post.mp3;
        String id = String.valueOf(source.hashCode());

        MusicProvider mMusicProvider = MusicProvider.getInstance();
        MediaMetadataCompat item = mMusicProvider.getMusic(id);

        if (item == null) {
            item = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, source)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
//                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
//                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, iconUrl)
//                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNumber)
//                        .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTrackCount)
                    .build();


            mMusicProvider.updateMusic(id, item);
        }

        MediaBrowserCompat.MediaItem bItem =
                new MediaBrowserCompat.MediaItem(item.getDescription(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);

        boolean isPlaying = id.equals(mediaPlayer.getPlayingMediaId());
        mediaPlayer.onMediaItemSelected(bItem, isPlaying);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.mTextView.setText(post.title.rendered);
//        holder.scoreTextView.setText(String.valueOf(post.score));

        String imageLink = "http://i.imgur.com/DvpvklR.png";
        if (post.featuredImage != null) {
            imageLink = post.featuredImage;
        }
        Picasso.with(context.getContext()).load(imageLink).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

}