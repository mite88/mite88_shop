package io.mite88.mite88shop.posts.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.posts.dto.EditPostRequest;
import io.mite88.mite88shop.posts.dto.PostDescription;
import io.mite88.mite88shop.posts.entity.Posts;
import io.mite88.mite88shop.posts.mapper.PostMapper;
import io.mite88.mite88shop.posts.repository.PostJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostJpaRepository repository;
    private final MemberService memberService;

    @Transactional
    public PostDescription save(EditPostRequest request, String username) {

        Posts post = Posts.builder()
                .title(request.title())
                .content(request.content())
                .build();

        Member findMember = memberService.findByUsername(username);

        post.setAuthor(findMember);

        Posts saved = repository.save(post);

        return PostMapper.toDescription(saved);

    }

    public PostDescription findById(Long id) {
        Optional<Posts> postOptional = repository.findById(id);

        // NoSuchElementException 대신 BusinessException(ResponseCode.POST_NOT_FOUND) 던지도록 수정
        Posts findPost = postOptional.orElseThrow(() -> new BusinessException(ResponseCode.POST_NOT_FOUND));

        return PostMapper.toDescription(findPost);
    }

    public List<PostDescription> findAll() {

        List<Posts> posts = repository.findAll();

        List<PostDescription> postDescriptions = new ArrayList<>();

        for ( Posts post : posts ) {
            PostDescription description = PostMapper.toDescription(post);
            postDescriptions.add(description);
        }

        return postDescriptions;

    }

    @Transactional
    public PostDescription updatePost(EditPostRequest request, Long id, String username) {

        Optional<Posts> postOptional = repository.findById(id);

        // NoSuchElementException 대신 BusinessException(ResponseCode.POST_NOT_FOUND) 던지도록 수정
        Posts findPost = postOptional.orElseThrow(() -> new BusinessException(ResponseCode.POST_NOT_FOUND));

        if ( !findPost.getAuthor().getUsername().equals(username) ) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED_POST_UPDATE);
        }

        findPost.update(request);

        return PostMapper.toDescription(findPost);

    }

    @Transactional
    public void deletePost(Long id) {
        repository.deleteById(id);
    }

}