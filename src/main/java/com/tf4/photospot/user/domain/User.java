package com.tf4.photospot.user.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tf4.photospot.global.entity.BaseEntity;
import com.tf4.photospot.spot.domain.BookmarkFolder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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

	@OneToMany(mappedBy = "user")
	private List<BookmarkFolder> bookmarkFolders = new ArrayList<>();

	private String nickname;

	private String profileUrl;

	private String providerType;

	private String account;

	private String role;

	private LocalDateTime deletedAt;

	public User(String nickname, String providerType, String account) {
		this.nickname = nickname;
		this.providerType = providerType;
		this.account = account;
		this.role = Role.USER.getType();
	}

}
