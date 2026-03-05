package com.dailytable.dailytable.global.util;

import java.util.Map;

public class RecipeMapConverter {
    public static final Map<String, Integer> PURPOSE_MAP = Map.of(
            "속세의맛", 1, "다이어트", 2, "건강식", 3, "술안주", 4
    );
    public  static final Map<String, Integer> CUISINE_MAP = Map.of(
            "상관없음", 1, "한식", 2, "일식", 3, "중식", 4, "양식", 5, "동남아", 6
    );
    public  static final Map<String, Integer> DIFFICULTY_MAP = Map.of(
            "상관없음", 0, "하", 1, "중", 2, "상", 3
    );
    public  static final Map<String, String> DIFFICULTY_AI_MAP = Map.of(
            "하", "LOW", "중", "MEDIUM", "상", "HIGH", "상관없음", "ANY"
    );
    public  static final Map<String, Integer> DIFFICULTY_RESULT_MAP = Map.of(
            "LOW", 1, "MEDIUM", 2, "HIGH", 3
    );
    public  static final Map<String, String> DIFFICULTY_LABEL_MAP = Map.of(
            "LOW", "하", "MEDIUM", "중", "HIGH", "상"
    );
    public  static final Map<Integer, String> CUISINE_STYLE_MAP = Map.of(
            1, "Any", 2, "Korean", 3, "Japanese", 4, "Chinese", 5, "Western", 6, "Southeast Asian"
    );
}
