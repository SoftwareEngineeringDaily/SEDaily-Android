package com.koalatea.thehollidayinn.softwareengineeringdaily.data.mapper;

import com.koalatea.thehollidayinn.softwareengineeringdaily.data.models.PostItem;
import com.koalatea.thehollidayinn.softwareengineeringdaily.network.response.PostResponse;

/**
 * Created by Kurian on 26-Sep-17.
 */
public class PostItemMapper extends BaseDataMapper<PostResponse, PostItem> {

    @Override
    public PostItem map(PostResponse source) {
        //TODO handle null input - throw exception?
        PostItem.Builder builder = PostItem.builder()
                .date(source.date())
                .audioLink(source.audioLink())
                .downVoted(source.downVoted())
                .upVoted(source.upVoted())
                .episodeLink(source.episodeLink())
                .featuredImgLink(source.featuredImageLink())
                .score(source.score())
                .id(source.id());

        if(source.content() != null) {
            builder.content(source.content().renderedContent());
        }
        if(source.title() != null) {
            builder.title(source.title().renderedTitle());
        }
        return builder.build();
    }
}
