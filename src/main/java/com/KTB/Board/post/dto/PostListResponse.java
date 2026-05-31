package com.KTB.Board.post.dto;

import com.KTB.Board.post.entity.Post;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class PostListResponse {

    private final Long postId;
    private final Long userId;
    private final String userName;
    private final String title;
    private final String date;
    private final String time;
    private final int likeCount;
    private final int commentCount;
    private final int viewCount;

    public PostListResponse(Post post) {
        this.postId = post.getId();
        this.userId = post.getUser().getId();
        this.userName = post.getUser().getNickname();
        this.title = post.getTitle();
        this.date = post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.time = post.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
    }
}
