package com.xxrin.board.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xxrin.board.domain.Member;
import com.xxrin.board.dto.request.MemberUpdateRequest;
import com.xxrin.board.repository.MemberRepository;
import com.xxrin.board.service.MemberService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MemberProfileTest {

    @Test
    void updatesOnlyNicknameAndPhoneUsingDirtyChecking() {
        MemberRepository members = mock(MemberRepository.class);
        Member member = Member.create(
                "user@example.com",
                "password-hash",
                "기존닉네임",
                "01012345678");
        ReflectionTestUtils.invokeMethod(member, "prePersist");
        LocalDateTime before = member.getUpdatedAt();
        when(members.findById(1L)).thenReturn(Optional.of(member));

        var response = new MemberService(members)
                .updateMe(1L, new MemberUpdateRequest("새닉네임", "010-9876-5432"));

        assertThat(response.email()).isEqualTo("user@example.com");
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.phone()).isEqualTo("01098765432");
        assertThat(response.updatedAt()).isAfter(before);
    }
}
