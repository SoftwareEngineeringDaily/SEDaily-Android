package com.koalatea.thehollidayinn.softwareengineeringdaily.data.models;

import java.util.Date;

/**
 * Created by krh12 on 6/17/2017.
 */

public class Post {
    public String _id;
    public String id;
    public Date date;
    public String slug;
    public String link;
    public Title title;
    public Content content;
    public Integer score;
    public Boolean upvoted;
    public Boolean downvoted;
    public String mp3;
    public String featuredImage;
}
