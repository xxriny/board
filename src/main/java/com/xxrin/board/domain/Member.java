package com.xxrin.board.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 로그인 정보와 게시글·댓글 작성자 정보를 관리하는 회원이다. */
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(nullable = false, unique = true, length = 11)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    private Member(String email, String passwordHash, String nickname, String phone) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.phone = phone;
        this.role = Role.USER;
    }

    public static Member create(
            String email,
            String passwordHash,
            String nickname,
            String phone) {
        return new Member(email, passwordHash, nickname, phone);
    }

    /** 닉네임과 전화번호만 변경한다. */
    public void updateProfile(String nickname, String phone) {
        this.nickname = nickname;
        this.phone = phone;
        touch();
    }
}
