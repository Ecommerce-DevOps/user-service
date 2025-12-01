package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.helper.UserMappingHelper;
import com.selimhorri.app.repository.CredentialRepository;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import io.micrometer.core.instrument.MeterRegistry;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final CredentialRepository credentialRepository;
	private final MeterRegistry meterRegistry;

	@Override
	public List<UserDto> findAll() {
		log.info("*** UserDto List, service; fetch all users *");
		return this.userRepository.findAll()
				.stream()
				.map(UserMappingHelper::map)
				.distinct()
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public UserDto findById(final Integer userId) {
		log.info("*** UserDto, service; fetch user by id *");
		return this.userRepository.findById(userId)
				.map(UserMappingHelper::map)
				.orElseThrow(
						() -> new UserObjectNotFoundException(String.format("User with id: %d not found", userId)));
	}

	@Override
	public UserDto save(final UserDto userDto) {
		log.info("*** UserDto, service; save user *");
		
		// Map DTO to entity
		User user = UserMappingHelper.map(userDto);
		
		// Temporarily remove credential to save user first
		Credential credential = user.getCredential();
		user.setCredential(null);
		
		// Save user first to get the generated ID
		User savedUser = this.userRepository.save(user);
		
		// Now save credential with the user reference
		if (credential != null) {
			credential.setUser(savedUser);
			Credential savedCredential = this.credentialRepository.save(credential);
			savedUser.setCredential(savedCredential);
		}
		
		this.meterRegistry.counter("user_registrations_total").increment();
		return UserMappingHelper.map(savedUser);
	}

	@Override
	public UserDto update(final UserDto userDto) {
		log.info("*** UserDto, service; update user *");
		
		// First verify user exists
		if (!this.userRepository.existsById(userDto.getUserId())) {
			throw new UserObjectNotFoundException(
					String.format("User with id: %d not found", userDto.getUserId()));
		}
		
		// Get existing credential ID separately
		Integer existingCredentialId = this.credentialRepository.findByUserUserId(userDto.getUserId())
				.map(Credential::getCredentialId)
				.orElse(null);
		
		// Map DTO to entity
		User user = UserMappingHelper.map(userDto);
		
		// For updates, handle credential separately
		Credential newCredential = user.getCredential();
		user.setCredential(null);
		
		// Save user first
		User savedUser = this.userRepository.save(user);
		
		// Update credential with existing credential ID
		if (newCredential != null) {
			if (existingCredentialId != null) {
				newCredential.setCredentialId(existingCredentialId);
			}
			newCredential.setUser(savedUser);
			Credential savedCredential = this.credentialRepository.save(newCredential);
			savedUser.setCredential(savedCredential);
		}
		
		return UserMappingHelper.map(savedUser);
	}

	@Override
	public UserDto update(final Integer userId, final UserDto userDto) {
		log.info("*** UserDto, service; update user with userId *");
		
		// First verify user exists
		if (!this.userRepository.existsById(userId)) {
			throw new UserObjectNotFoundException(
					String.format("User with id: %d not found", userId));
		}
		
		// Get existing credential ID separately
		Integer existingCredentialId = this.credentialRepository.findByUserUserId(userId)
				.map(Credential::getCredentialId)
				.orElse(null);
		
		// Map DTO to entity with the provided userId
		User user = UserMappingHelper.map(userDto);
		user.setUserId(userId);
		
		// For updates, handle credential separately
		Credential newCredential = user.getCredential();
		user.setCredential(null);
		
		// Save user first
		User savedUser = this.userRepository.save(user);
		
		// Update credential with existing credential ID
		if (newCredential != null) {
			if (existingCredentialId != null) {
				newCredential.setCredentialId(existingCredentialId);
			}
			newCredential.setUser(savedUser);
			Credential savedCredential = this.credentialRepository.save(newCredential);
			savedUser.setCredential(savedCredential);
		}
		
		return UserMappingHelper.map(savedUser);
	}

	@Override
	public void deleteById(final Integer userId) {
		log.info("*** Void, service; delete user by id *");
		this.userRepository.deleteById(userId);
	}

	@Override
	public UserDto findByUsername(final String username) {
		log.info("*** UserDto, service; fetch user with username *");
		return UserMappingHelper.map(this.userRepository.findByCredentialUsername(username)
				.orElseThrow(() -> new UserObjectNotFoundException(
						String.format("User with username: %s not found", username))));
	}

}
