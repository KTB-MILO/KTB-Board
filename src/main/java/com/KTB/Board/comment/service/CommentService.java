package com.KTB.Board.comment.service;

import com.KTB.Board.comment.dto.CreateCommentRequest;
import com.KTB.Board.comment.dto.UpdateCommentRequest;
import com.KTB.Board.comment.entity.Comment;
import com.KTB.Board.comment.repository.CommentRepository;
import com.KTB.Board.common.exception.CustomException;
import com.KTB.Board.common.exception.ErrorCode;
import com.KTB.Board.post.entity.Post;
import com.KTB.Board.post.repository.PostRepository;
import com.KTB.Board.user.entity.User;
import com.KTB.Board.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createComment(Long postId, Long userId, CreateCommentRequest request) {
        Post post = findPost(postId);
        User user = findUser(userId);
        commentRepository.save(Comment.builder()
                .post(post)
                .user(user)
                .comment(request.getComment())
                .build());
        post.setCommentCount(post.getCommentCount() + 1);
    }

    @Transactional
    public void updateComment(Long commentId, Long userId, UpdateCommentRequest request) {
        Comment comment = findComment(commentId);
        checkOwner(comment.getUser().getId(), userId);
        comment.setComment(request.getComment());
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = findComment(commentId);
        checkOwner(comment.getUser().getId(), userId);
        comment.getPost().setCommentCount(comment.getPost().getCommentCount() - 1);
        commentRepository.delete(comment);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    }

    private void checkOwner(Long ownerId, Long userId) {
        if (!ownerId.equals(userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
    }
}
