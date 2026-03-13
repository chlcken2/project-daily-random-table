package com.dailytable.dailytable.domain.ingredient;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IngredientRepository {

    String findNormalizedByAlias(@Param("aliasName") String aliasName);

    void insertAlias(IngredientEntity.Alias alias);

    void insertIngredient(@Param("normalizedName") String normalizedName);

    boolean existsByNormalizedName(@Param("normalizedName") String normalizedName);

    // User Ingredients CRUD
    List<UserIngredient> findByUserId(@Param("userId") Long userId, @Param("type") Integer type);

    void insertUserIngredient(UserIngredient ingredient);

    void updateUserIngredientQuantity(@Param("userId") Long userId, @Param("normalizedName") String normalizedName, @Param("quantity") double quantity);

    int deleteUserIngredient(@Param("id") Long id, @Param("userId") Long userId);

    UserIngredient findById(@Param("id") Long id);

    // Inner class for user_ingredients
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class UserIngredient {
        private Long id;
        private Long userId;
        private String name;
        private String normalizedName;
        private double quantity;
        private String unit;
        private int type; // 1: ingredient, 2: sauce
    }
}
