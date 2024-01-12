package com.tf4.photospot.user.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findUserByProviderTypeAndAccount(String providerType, String account);

	boolean existsByNickname(String nickname);

}
