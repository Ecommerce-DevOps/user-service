package com.selimhorri.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.selimhorri.app.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	Optional<User> findByCredentialUsername(final String username);
	
	@Query("SELECT u FROM User u LEFT JOIN FETCH u.credential WHERE u.userId = :userId")
	Optional<User> findByIdWithCredential(@Param("userId") Integer userId);
	
}
