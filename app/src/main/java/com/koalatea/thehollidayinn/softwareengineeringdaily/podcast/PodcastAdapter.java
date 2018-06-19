package com.koalatea.thehollidayinn.softwareengineeringdaily.podcast;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koalatea.thehollidayinn.softwareengineeringdaily.R;
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Post;
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.Title;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/*
 * Created by krh12 on 5/22/2017.
 */

class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.ViewHolder> {
  private List<Post> posts = new ArrayList<>();
  private final PublishSubject<Post> onClickSubject = PublishSubject.create();

  static class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.card_title)
    TextView mTextView;

    @BindView(R.id.card_image)
    ImageView imageView;

//    @BindView(R.id.card_desc)
//    TextView description;

    private ViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }
  }

  void setPosts(List<Post> posts) {
    this.posts = posts;
    this.notifyDataSetChanged();
  }

  @Override
  public PodcastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
    final View view =  LayoutInflater.from(parent.getContext())
      .inflate(R.layout.fragment_podcast_grid, parent, false);

    final ViewHolder viewHolder = new ViewHolder(view);

    view.setOnClickListener(v -> {
      final int position = viewHolder.getAdapterPosition();
      Post post = posts.get(position);

      // @TODO: How to pass text view as well?
      onClickSubject.onNext(post);
    });

    return viewHolder;
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Post post = posts.get(position);
    Title postTitle = post.getTitle();

    if (postTitle == null) return;

    holder.mTextView.setText(postTitle.getRendered());
//    holder.description.setText(postTitle.getRendered());

    String imageLink = "https://softwareengineeringdaily.com/wp-content/uploads/2015/08/sed21.png";
    if (post.getFeaturedImage() != null) {
      imageLink = post.getFeaturedImage();
    }

    Picasso.with(SEDApp.component.context())
        .load(imageLink)
        .resize(100, 100)
        .centerCrop()
        .into(holder.imageView);
  }

  @Override
  public int getItemCount() {
    return posts.size();
  }

  public Observable<Post> getPositionClicks() {
    return onClickSubject;
  }
}