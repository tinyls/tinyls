package com.tinyls.urlshortener.mapper;

import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.model.Url;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper interface for converting between {@link Url} entities and
 * {@link UrlDTO} objects.
 * Uses MapStruct for automatic mapping implementation.
 * 
 * This mapper handles:
 * - Entity to DTO conversion
 * - DTO to entity conversion
 * - Entity updates from DTO
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UrlMapper {
    /**
     * Converts a URL entity to a DTO.
     * 
     * @param url the URL entity to convert
     * @return the corresponding URL DTO
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "status")
    UrlDTO toDTO(Url url);

    /**
     * Converts a URL DTO to an entity.
     * 
     * @param urlDTO the URL DTO to convert
     * @return the corresponding URL entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "clicks", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shortCode", ignore = true)
    @Mapping(target = "status", source = "status")
    Url toEntity(UrlDTO urlDTO);

    /**
     * Updates an existing URL entity with data from a DTO.
     * 
     * @param urlDTO the DTO containing the updated data
     * @param url    the entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "clicks", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "shortCode", ignore = true)
    @Mapping(target = "status", source = "status")
    void updateEntityFromDTO(UrlDTO urlDTO, @MappingTarget Url url);
}