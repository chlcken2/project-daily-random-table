package com.dailytable.dailytable.domain.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequest {
    @NotBlank(message = "食材名を入力してください。")
    private String name;

    @NotNull(message = "数量を入力してください。")
    @Positive(message = "数量は0より大きい必要があります。")
    private Double quantity;

    @NotNull(message = "単位を選択してください。")
    private String unit;

    @NotNull(message = "区分を選択してください。")
    private Integer type;
}
