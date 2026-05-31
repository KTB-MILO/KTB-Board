package com.KTB.Board.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreatePostRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String image;
}
