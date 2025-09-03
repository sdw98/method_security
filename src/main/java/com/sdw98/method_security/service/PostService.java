package com.sdw98.method_security.service;

import com.sdw98.method_security.model.Post;
import com.sdw98.method_security.model.Status;
import com.sdw98.method_security.model.User;
import com.sdw98.method_security.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostService {
    private final PostRepository postRepository;

    @PreAuthorize("isAuthenticated()")
    public Post createPost(String title, String content, User author) {
        log.info("✏️ [PRE_AUTHORIZE] 게시글 작성 - 인증된 사용자");
        return postRepository.save(
                Post.builder()
                        .title(title)
                        .content(content)
                        .author(author)
                        .build()
        );
    }

    @PreAuthorize("isAuthenticated()")
    public Post createPost(String title, String content, User author, boolean isPublic) {
        log.info("✏️ [PRE_AUTHORIZE] 게시글 작성 - 인증된 사용자 (공개설정: " + isPublic + ")");
        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
        post.setPublic(isPublic);
        post.setStatus(isPublic ? Status.PUBLISHED : Status.DRAFT);
        return postRepository.save(post);
    }

    @PreAuthorize("#post.author.username == authentication.name or hasRole('ADMIN')")
    public Post updatePost(Post post) {
        log.info("✏️ [PRE_AUTHORIZE] 게시글 수정 - 작성자 또는 관리자");
        return postRepository.save(post);
    }

    @PreAuthorize("@postService.isPostOwner(#postId, authentication.name) or hasRole('ADMIN')")
    public void deletePost(Long postId) {
        log.info("🗑️ [PRE_AUTHORIZE] 게시글 삭제 - 커스텀 메서드 활용");
        postRepository.deleteById(postId);
    }

    @PostAuthorize("returnObject == null or " +
            "returnObject.isPublic() == true or " +
            "returnObject.author.username == authentication.name or " +
            "hasRole('ADMIN')"
    )
    public Post getPost(Long postId) {
        log.info("🔍 [POST_AUTHORIZE] 게시글 조회 - 공개글/작성자/관리자");
        return postRepository.findById(postId).orElse(null);
    }

    public boolean isPostOwner(Long postId, String username) {
        Post post = postRepository.findById(postId).orElse(null);
        return post != null && post.getAuthor().getUsername().equals(username);
    }
}