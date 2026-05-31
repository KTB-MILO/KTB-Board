package com.KTB.Board.post.dto;

import lombok.Getter;

@Getter
public class UpdatePostRequest {

    private String title;
    private String content;
    private String image;
}
