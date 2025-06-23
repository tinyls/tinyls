package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.dto.user.UserRequestDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.dto.user.OAuth2UserDTO;
import com.tinyls.urlshortener.dto.user.PasswordUpdateDTO;
import com.tinyls.urlshortener.exception.EmailAlreadyExistsException;
import com.tinyls.urlshortener.exception.ResourceNotFoundException;
import com.tinyls.urlshortener.exception.PasswordValidationException;
import com.tinyls.urlshortener.exception.IncorrectPasswordException;
import com.tinyls.urlshortener.mapper.UserMapper;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import com.tinyls.urlshortener.repository.UserRepository;
import com.tinyls.urlshortener.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the UserService interface.
 * Handles user management operations including CRUD operations, password
 * management,
 * and OAuth2 user creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userDTO) {
        log.info("Creating new user with email: {}", userDTO.getEmail());

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException(userDTO.getEmail());
        }

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(Role.USER);

        validateUser(user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(UUID id) {
        log.debug("Retrieving user with ID: {}", id);
        return userRepository.findById(id)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.debug("Retrieving user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.debug("Retrieving all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO userDTO) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (!existingUser.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException(userDTO.getEmail());
        }

        userMapper.updateEntityFromRequest(userDTO, existingUser);
        validateUser(existingUser);
        return userMapper.toResponseDTO(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public UserResponseDTO updatePassword(UUID id, PasswordUpdateDTO passwordDTO) {
        log.info("Updating password for user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));

        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), existingUser.getPassword())) {
            throw new IncorrectPasswordException();
        }

        validatePasswordStrength(passwordDTO.getNewPassword());
        existingUser.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));

        validateUser(existingUser);
        return userMapper.toResponseDTO(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id.toString());
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDTO createOAuth2User(OAuth2UserDTO oauth2UserDTO) {
        log.info("Creating OAuth2 user with email: {}", oauth2UserDTO.getEmail());

        if (userRepository.existsByEmail(oauth2UserDTO.getEmail())) {
            throw new EmailAlreadyExistsException(oauth2UserDTO.getEmail());
        }

        User user = new User();
        user.setEmail(oauth2UserDTO.getEmail());
        user.setName(oauth2UserDTO.getName());
        user.setProvider(oauth2UserDTO.getProvider());
        user.setProviderId(oauth2UserDTO.getProviderId());
        user.setRole(oauth2UserDTO.getRole());

        validateUser(user);
        User savedUser = userRepository.save(user);
        return userMapper.toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists with email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new PasswordValidationException("Password must be at least 8 characters long");
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpperCase = true;
            else if (Character.isLowerCase(c))
                hasLowerCase = true;
            else if (Character.isDigit(c))
                hasDigit = true;
            else
                hasSpecialChar = true;
        }

        if (!hasUpperCase || !hasLowerCase || !hasDigit || !hasSpecialChar) {
            throw new PasswordValidationException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }

    @Override
    public void validateUser(User user) {
        if (user.getProvider() == null) {
            throw new IllegalStateException("Provider must be specified");
        }
        if (user.getRole() == null) {
            throw new IllegalStateException("Role must be specified");
        }
        user.validatePassword();
    }
}