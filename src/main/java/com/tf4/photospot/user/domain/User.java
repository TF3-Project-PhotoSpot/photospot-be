package com.tf4.photospot.user.domain;

import java.time.LocalDateTime;

import com.tf4.photospot.global.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nickname;

	private String profileUrl;

	private String providerType;

	private String account;

	@Enumerated(EnumType.STRING)
	private Role role;

	private LocalDateTime deletedAt;

	@Builder
	public User(String nickname, String providerType, String account) {
		this.nickname = nickname;
		this.providerType = providerType;
		this.account = account;
		this.role = Role.USER;
	}

	public void updateProfile(String profileUrl) {
		this.profileUrl = profileUrl;
	}

}
