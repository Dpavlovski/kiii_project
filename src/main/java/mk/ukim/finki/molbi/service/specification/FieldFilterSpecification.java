package mk.ukim.finki.molbi.service.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import mk.ukim.finki.molbi.model.enums.RequestType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;


public class FieldFilterSpecification {
    public static <T> Specification<T> filterEquals(Class<T> clazz, String field, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(fieldToPath(field, root), value);
    }

    public static <T> Specification<T> filterEquals(Class<T> clazz, String field, Long value) {
        if (value == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(fieldToPath(field, root), value);
    }

    public static <T> Specification<T> filterEquals(Class<T> clazz, String field, Boolean value) {
        if (value == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(fieldToPath(field, root), value);
    }

    public static <T> Specification<T> filterContainsText(Class<T> clazz, String field, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(fieldToPath(field, root), "%" + value + "%");
    }

    public static <T> Specification<T> filterEquals(Class<T> clazz, String field, RequestType value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(fieldToPath(field, root), value);
    }

    public static <T> Specification<T> filterIsAfter(Class<T> clazz, String field, LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(fieldToPath(field, root), LocalDateTime.now());
    }

    public static <T> Specification<T> filterIsBefore(Class<T> clazz, String field, LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(fieldToPath(field, root), value);
    }

    private static <T> Path fieldToPath(String field, Root<T> root) {
        String[] parts = field.split("\\.");
        Path res = root;
        for (String p : parts) {
            res = res.get(p);
        }
        return res;
    }
}
