package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import com.google.auto.value.AutoValue;

import java.util.Date;

/**
 * Created by Kurian on 26-Sep-17.
 */
@AutoValue
public abstract class PostItem {
    public abstract String id();
    public abstract Date date();
    public abstract String episodeLink();
    public abstract String audioLink();
    public abstract String featuredImgLink();
    public abstract String content();
    public abstract String title();
    public abstract int score();
    public abstract boolean upVoted();
    public abstract boolean downVoted();

    public static Builder builder() {
        return new AutoValue_PostItem.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);
        public abstract Builder date(Date date);
        public abstract Builder episodeLink(String episodeLink);
        public abstract Builder audioLink(String audioLink);
        public abstract Builder featuredImgLink(String featuredImgLink);
        public abstract Builder content(String content);
        public abstract Builder title(String title);
        public abstract Builder score(int score);
        public abstract Builder upVoted(boolean upVoted);
        public abstract Builder downVoted(boolean downVoted);
        public abstract PostItem build();
    }
}
