package com.company.searchengine.exception.validation;

import com.company.searchengine.model.constant.SearchConstants;
import com.company.searchengine.model.dto.request.SearchQueryRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class SearchQueryValidator implements ConstraintValidator<ValidSearchQuery, SearchQueryRequest> {

    @Override
    public boolean isValid(SearchQueryRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        if (!validateDeepPagination(request, context)) {
            return false;
        }

        return validateDuplicateColumns(request, context);
    }

    private boolean validateDeepPagination(SearchQueryRequest request, ConstraintValidatorContext context) {
        int page = request.page() == null
                ? SearchConstants.Pagination.DEFAULT_PAGE
                : Math.max(request.page(), SearchConstants.Pagination.DEFAULT_PAGE);

        int size = request.size() == null
                ? SearchConstants.Pagination.DEFAULT_SIZE
                : Math.min(request.size(), SearchConstants.Pagination.MAX_SIZE);

        long offset = (long) page * size;

        if (offset > SearchConstants.Pagination.MAX_OFFSET) {
            return fail(context, "Deep pagination is not allowed. Maximum offset is "
                    + SearchConstants.Pagination.MAX_OFFSET);
        }

        return true;
    }

    private boolean validateDuplicateColumns(SearchQueryRequest request, ConstraintValidatorContext context) {
        if (request.columns() == null || request.columns().isEmpty()) {
            return true;
        }

        Set<String> seen = new HashSet<>();

        for (String column : request.columns()) {
            if (!seen.add(column)) {
                return fail(context, "Duplicate column is not allowed: " + column);
            }
        }

        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }
}
