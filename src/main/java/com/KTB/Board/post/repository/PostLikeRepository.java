package com.KTB.Board.post.repository;

import com.KTB.Board.post.entity.Post;
import com.KTB.Board.post.entity.PostLike;
import com.KTB.Board.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostAndUser(Post post, User user);

    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.post.user.id = :userId")
    void deleteByPostUserId(@Param("userId") Long userId);
}
