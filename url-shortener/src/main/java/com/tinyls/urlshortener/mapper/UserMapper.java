package com.tinyls.urlshortener.mapper;

import com.tinyls.urlshortener.dto.user.UserRequestDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.model.AuthProvider;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between {@link User} entities and DTOs.
 * Handles manual mapping of user data between different representations.
 * 
 * This mapper ensures:
 * - Sensitive data (like passwords) is never exposed in DTOs
 * - Proper null handling for all mappings
 * - Selective field updates during entity updates
 */
@Component
public class UserMapper {
    /**
     * Maps a User entity to a UserResponseDTO.
     * Excludes sensitive fields and adds computed fields like canChangePassword.
     * 
     * @param user the user entity to convert, can be null
     * @return the corresponding UserResponseDTO, or null if input is null
     */
    public UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .canChangePassword(user.getProvider() == AuthProvider.LOCAL)
                .build();
    }

    /**
     * Maps a UserRequestDTO to a User entity.
     * Only maps basic user information, leaving other fields to be set by the
     * service layer.
     * 
     * @param userDTO the user request DTO to convert, can be null
     * @return the corresponding User entity, or null if input is null
     */
    public User toEntity(UserRequestDTO userDTO) {
        if (userDTO == null) {
            return null;
        }

        return User.builder()
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .build();
    }

    /**
     * Updates a User entity with data from a UserRequestDTO.
     * Only updates non-sensitive fields and preserves existing values if new values
     * are null.
     * 
     * @param userDTO the DTO containing the updated data, can be null
     * @param user    the entity to update, can be null
     */
    public void updateEntityFromRequest(UserRequestDTO userDTO, User user) {
        if (userDTO == null || user == null) {
            return;
        }

        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getName() != null) {
            user.setName(userDTO.getName());
        }
    }
}