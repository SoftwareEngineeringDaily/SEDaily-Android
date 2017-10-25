package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Title;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by krh12 on 5/22/2017.
 */

class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
  private List<Post> posts = new ArrayList<>();
  private PodListFragment context;

  static class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.card_title)
    TextView mTextView;

    @BindView(R.id.card_image)
    ImageView imageView;

    private ViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
  }

  PodcastAdapter(Fragment context) {
    // @TODO: This is memory leak worthy. Fix it
    this.context = (PodListFragment) context;
  }

  void setPosts(List<Post> posts) {
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
      intent.putExtra("POST_ID", post.get_id());
      context.getActivity().startActivity(intent);
        }
    });

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Post post = posts.get(position);
    Title postTitle = post.getTitle();
    holder.mTextView.setText(postTitle.getRendered());

    String imageLink = "https://softwareengineeringdaily.com/wp-content/uploads/2015/08/sed21.png";
    if (post.getFeaturedImage() != null) {
      imageLink = post.getFeaturedImage();
    }
    Picasso.with(context.getContext()).load(imageLink).into(holder.imageView);
  }

  @Override
  public int getItemCount() {
    return posts.size();
  }
}