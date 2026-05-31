package com.KTB.Board.comment.dto;

import com.KTB.Board.comment.entity.Comment;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class CommentResponse {

    private final Long commentId;
    private final Long userId;
    private final String userName;
    private final String comment;
    private final String date;
    private final String time;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getId();
        this.userId = comment.getUser().getId();
        this.userName = comment.getUser().getNickname();
        this.comment = comment.getComment();
        this.date = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        this.time = comment.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
