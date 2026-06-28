package io.mite88.mite88shop.posts.service;

import io.mite88.mite88shop.global.code.ResponseCode;
import io.mite88.mite88shop.global.exception.BusinessException;
import io.mite88.mite88shop.members.entity.Member;
import io.mite88.mite88shop.members.service.MemberService;
import io.mite88.mite88shop.posts.dto.EditQnaRequest;
import io.mite88.mite88shop.posts.dto.QnaDescription;
import io.mite88.mite88shop.posts.dto.QnaPageResponse;
import io.mite88.mite88shop.posts.entity.Qna;
import io.mite88.mite88shop.posts.mapper.QnaMapper;
import io.mite88.mite88shop.posts.repository.QnaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository repository;
    private final MemberService memberService;

    @Transactional
    public QnaDescription save(EditQnaRequest request, String username) {
        Qna qna = Qna.builder()
                .title(request.title())
                .content(request.content())
                .build();
        Member member = memberService.findByUsername(username);
        qna.setAuthor(member);
        return QnaMapper.toDescription(repository.save(qna));
    }

    public QnaDescription findById(Long id) {
        Qna qna = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.POST_NOT_FOUND));
        return QnaMapper.toDescription(qna);
    }

    public QnaPageResponse findAll(int page) {
        PageRequest pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        Page<Qna> result = repository.findAll(pageable);
        List<QnaDescription> content = result.getContent().stream()
                .map(QnaMapper::toDescription)
                .toList();
        return new QnaPageResponse(content, result.getTotalPages(), result.getTotalElements(), result.getNumber());
    }

    @Transactional
    public QnaDescription updateQna(EditQnaRequest request, Long id, String username) {
        Qna qna = repository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.POST_NOT_FOUND));
        if (!qna.getAuthor().getUsername().equals(username)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED_POST_UPDATE);
        }
        qna.update(request);
        return QnaMapper.toDescription(qna);
    }

    @Transactional
    public void deleteQna(Long id) {
        repository.deleteById(id);
    }
}
