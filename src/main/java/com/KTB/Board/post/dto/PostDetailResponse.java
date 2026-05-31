package com.KTB.Board.post.dto;

import com.KTB.Board.comment.dto.CommentResponse;
import com.KTB.Board.post.entity.Post;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class PostDetailResponse {

    private final Long postId;
    private final Long userId;
    private final String userName;
    private final String title;
    private final String content;
    private final String image;
    private final String date;
    private final String time;
    private final int likeCount;
    private final int commentCount;
    private final int viewCount;
    private final List<CommentResponse> comments;

    public PostDetailResponse(Post post) {
        this.postId = post.getId();
        this.userId = post.getUser().getId();
        this.userName = post.getUser().getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.image = post.getImage();
        this.date = post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.time = post.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.comments = post.getComments().stream().map(CommentResponse::new).toList();
    }
}
