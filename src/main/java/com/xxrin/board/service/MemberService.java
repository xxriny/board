package com.xxrin.board.service;

import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.MemberUpdateRequest;
import com.xxrin.board.dto.response.MemberResponse;
import com.xxrin.board.exception.BusinessException;
import com.xxrin.board.exception.ErrorCode;
import com.xxrin.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 회원의 프로필 유스케이스를 관리한다. */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberResponse findMe(Long memberId) {
        return MemberResponse.from(findMember(memberId));
    }

    @Transactional
    public MemberResponse updateMe(Long memberId, MemberUpdateRequest request) {
        Member member = findMember(memberId);
        if (!member.getNickname().equals(request.normalizedNickname())
                && memberRepository.existsByNickname(request.normalizedNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (!member.getPhone().equals(request.normalizedPhone())
                && memberRepository.existsByPhone(request.normalizedPhone())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }
        member.updateProfile(request.normalizedNickname(), request.normalizedPhone());
        return MemberResponse.from(member);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
