package com.koalatea.thehollidayinn.softwareengineeringdaily.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Content;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.PostRepository;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.repositories.UserRepository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by krh12 on 5/31/2017.
 */

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {
    private String[] mDataset;
    private Activity activity;
    private List<Post> posts;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView imageView;
        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.card_title);
            imageView = (ImageView) v.findViewById(R.id.card_image);
        }
    }


    public FeedAdapter(String[] myDataset, Activity activity) {
        posts = new ArrayList<>();
        mDataset = myDataset;
        getFeed("");
        context = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_feed_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(FeedAdapter.ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.mTextView.setText(post.title.rendered);

        String imageLink = "http://i.imgur.com/DvpvklR.png";
        if (post.featuredImage != null) {
            imageLink = post.featuredImage;
        }
        Picasso.with(context).load(imageLink).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void getFeed(String tagId) {
        APIInterface mService = ApiUtils.getKibbleService(activity);

        Map<String, String> data = new HashMap<>();

        if (!tagId.isEmpty()) {
            data.put("tags", tagId);
        }

        UserRepository userRepository = UserRepository.getInstance(activity);
        final PostRepository postRepository = PostRepository.getInstance();

        mService.getPosts(data)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<List<Post>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.v("keithtest", e.toString());
                }

                @Override
                public void onNext(List<Post> newPosts) {
                    posts = newPosts;
                    notifyDataSetChanged();
                }
            });
    }
}