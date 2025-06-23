package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.dto.user.UserRequestDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.dto.user.OAuth2UserDTO;
import com.tinyls.urlshortener.dto.user.PasswordUpdateDTO;
import com.tinyls.urlshortener.model.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    /**
     * Create a new user
     * 
     * @param userDTO user data
     * @return created user
     */
    UserResponseDTO createUser(UserRequestDTO userDTO);

    /**
     * Get user by ID
     * 
     * @param id user ID
     * @return user data
     * @throws ResourceNotFoundException if user not found
     */
    UserResponseDTO getUserById(UUID id);

    /**
     * Get user by email
     * 
     * @param email user email
     * @return user data
     * @throws ResourceNotFoundException if user not found
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Update user data
     * 
     * @param id      user ID
     * @param userDTO updated user data
     * @return updated user
     * @throws ResourceNotFoundException if user not found
     */
    UserResponseDTO updateUser(UUID id, UserRequestDTO userDTO);

    /**
     * Update user password
     * 
     * @param id          user ID
     * @param passwordDTO password update data
     * @return updated user
     * @throws ResourceNotFoundException if user not found
     * @throws IllegalArgumentException  if current password is incorrect
     */
    UserResponseDTO updatePassword(UUID id, PasswordUpdateDTO passwordDTO);

    /**
     * Delete user
     * 
     * @param id user ID
     * @throws ResourceNotFoundException if user not found
     */
    void deleteUser(UUID id);

    /**
     * Get all users
     * 
     * @return list of all users
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Create a new OAuth2 user
     * 
     * @param oauth2UserDTO OAuth2 user data
     * @return created user
     */
    UserResponseDTO createOAuth2User(OAuth2UserDTO oauth2UserDTO);

    /**
     * Check if user exists by email
     * 
     * @param email user email
     * @return true if user exists
     */
    boolean existsByEmail(String email);

    /**
     * Validate password strength
     * 
     * @param password password to validate
     * @throws IllegalArgumentException if password validation fails
     */
    void validatePasswordStrength(String password);

    /**
     * Validate user data
     * 
     * @param user the user to validate
     * @throws IllegalStateException if validation fails
     */
    void validateUser(User user);
}