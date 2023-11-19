package com.tf4.photospot.user.infrastructure;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tf4.photospot.user.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findUserByAccountAndProviderType(String account, String provider);

}
