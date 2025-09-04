package com.sdw98.method_security.service;

import com.sdw98.method_security.model.Post;
import com.sdw98.method_security.model.Status;
import com.sdw98.method_security.model.User;
import com.sdw98.method_security.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .isPublic(isPublic)
                .status(isPublic ? Status.PUBLISHED : Status.DRAFT)
                .build();

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

    @PostFilter("filterObject.isPublic() == true or " +
            "filterObject.author.username == authentication.name or " +
            "hasRole('ADMIN')"
    )
    public List<Post> getAllPosts() {
        log.info("🔍 [POST_FILTER] 모든 게시글 조회 후 필터링");
        return postRepository.findAll();
    }

    @PreAuthorize("#post.author.username == authentication.name and #post.status.name() == 'DRAFT'")
    public Post publishPost(Post post) {
        log.info("📢 [PRE_AUTHORIZE] 게시글 발행 - 작성자이고 DRAFT 상태");
        post.setStatus(Status.PUBLISHED);
        post.setPublic(true);
        return postRepository.save(post);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void archivePosts(List<Long> postIds) {
        log.info("📦 [PRE_AUTHORIZE] 게시글 아카이브 - ADMIN 권한");
        List<Post> posts = postRepository.findAllById(postIds);
        posts.forEach(post -> post.setStatus(Status.ARCHIVED));
        postRepository.saveAll(posts);
    }

    public boolean isPostOwner(Long postId, String username) {
        Post post = postRepository.findById(postId).orElse(null);
        return post != null && post.getAuthor().getUsername().equals(username);
    }

    public List<Post> getPublicPosts() {
        log.info("🌍 공개 게시글 조회 - 인증 불필요");
        return postRepository.findByIsPublicTrue();
    }

    public List<Post> getPostsByAuthor(User author) {
        log.info("👤 사용자별 게시글 조회 - " + author.getUsername());
        return postRepository.findByAuthor(author);
    }
}