package com.company.searchengine.exception.validation;

import com.company.searchengine.model.constant.SearchConstants;
import com.company.searchengine.model.dto.request.SearchFilterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;

public class SearchFilterValidator implements ConstraintValidator<ValidSearchFilter, SearchFilterRequest> {

    @Override
    public boolean isValid(SearchFilterRequest request, ConstraintValidatorContext context) {
        if (request == null || request.operator() == null || request.value() == null) {
            return true;
        }

        return switch (request.operator()) {
            case IN -> validateInValue(request.value(), context);
            case BETWEEN -> validateBetweenValue(request.value(), context);
            case CONTAINS -> validateContainsValue(request.value(), context);
            default -> true;
        };
    }

    private boolean validateInValue(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof Collection<?> values) || values.isEmpty()) {
            return fail(context, "IN operator requires non-empty list value");
        }

        if (values.size() > SearchConstants.Validation.MAX_IN_VALUES) {
            return fail(context, "IN operator supports maximum "
                    + SearchConstants.Validation.MAX_IN_VALUES + " values");
        }

        if (values.stream().anyMatch(item -> item == null)) {
            return fail(context, "IN operator does not allow null values");
        }

        return true;
    }

    private boolean validateBetweenValue(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof List<?> values) || values.size() != 2) {
            return fail(context, "BETWEEN operator requires exactly two values");
        }

        if (values.get(0) == null || values.get(1) == null) {
            return fail(context, "BETWEEN operator does not allow null values");
        }

        return true;
    }

    private boolean validateContainsValue(Object value, ConstraintValidatorContext context) {
        if (value instanceof String text && text.isBlank()) {
            return fail(context, "CONTAINS operator requires non-empty value");
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
