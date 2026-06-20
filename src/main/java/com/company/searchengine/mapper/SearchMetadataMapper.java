package com.company.searchengine.mapper;

import com.company.searchengine.dao.entity.SearchDomainEntity;
import com.company.searchengine.dao.entity.SearchFieldEntity;
import com.company.searchengine.model.dto.DomainDefinitionDto;
import com.company.searchengine.model.dto.FieldDefinitionDto;
import com.company.searchengine.model.dto.response.SearchFieldResponse;
import com.company.searchengine.model.enums.SearchOperator;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SearchMetadataMapper {

    DomainDefinitionDto toDomainDefinitionDto(SearchDomainEntity entity);

    @Mapping(target = "filterable", expression = "java(Boolean.TRUE.equals(entity.getFilterable()))")
    @Mapping(target = "sortable", expression = "java(Boolean.TRUE.equals(entity.getSortable()))")
    @Mapping(target = "exportable", expression = "java(Boolean.TRUE.equals(entity.getExportable()))")
    @Mapping(target = "projectionDefault", expression = "java(Boolean.TRUE.equals(entity.getProjectionDefault()))")
    @Mapping(target = "arrayField", expression = "java(Boolean.TRUE.equals(entity.getArrayField()))")
    @Mapping(target = "allowedOperators", source = "allowedOperators", qualifiedByName = "parseOperators")
    FieldDefinitionDto toFieldDefinitionDto(SearchFieldEntity entity);

    @Mapping(target = "field", source = "requestField")
    @Mapping(target = "allowedOperators", source = "allowedOperators")
    SearchFieldResponse toSearchFieldResponse(FieldDefinitionDto field);

    @Named("parseOperators")
    default Set<SearchOperator> parseOperators(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return Set.of();
        }

        EnumSet<SearchOperator> operators = EnumSet.noneOf(SearchOperator.class);

        Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(SearchOperator::valueOf)
                .forEach(operators::add);

        return Set.copyOf(operators);
    }
}
