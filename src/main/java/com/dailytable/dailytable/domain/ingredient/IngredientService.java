package com.dailytable.dailytable.domain.ingredient;

import com.dailytable.dailytable.domain.gacha.GachaDto;
import com.dailytable.dailytable.global.ai.GeminiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    // Regex: 한글/영어/일본어/한자 및 공백만 허용 (일본어 장음 ー 포함)
    private static final Pattern VALID_INGREDIENT_PATTERN =
            Pattern.compile("^[가-힣a-zA-Z\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF\\sー]+$");

    // 블랙리스트 - 풍부한 데이터
    private static final Set<String> BLACKLIST = new HashSet<>(Arrays.asList(
            // 추상명사/감정/가치/철학
            "사랑", "행복", "엄마", "아빠", "추억", "마음", "정성", "감동",
            "우정", "눈물", "기쁨", "슬픔", "분노", "희망", "꿈", "영혼",
            "미래", "과거", "현재", "인생", "삶", "죽음", "평화", "자유", "정의", "진리",
            "love", "happiness", "soul", "memory", "dream", "hope", "anger", "fear",
            "peace", "freedom", "life", "death", "future", "past", "justice", "truth",
            "돈", "money", "시간", "time", "공기", "air", "바람", "wind", "우주", "universe",
            "불", "fire", "물", "water", "흙", "soil", "earth", "하늘", "sky", "바다", "sea",
            // 비식재료/물건/재질/건축
            "플라스틱", "유리", "금속", "철", "돌", "나무", "종이", "비닐", "고무", "콘크리트", "시멘트", "벽돌",
            "plastic", "glass", "metal", "iron", "stone", "wood", "paper", "vinyl", "rubber", "concrete", "cement", "brick",
            "자동차", "컴퓨터", "스마트폰", "책상", "의자", "침대", "TV", "텔레비전", "냉장고", "세탁기", "에어컨",
            "car", "computer", "phone", "desk", "chair", "bed", "television", "fridge", "washing machine", "air conditioner",
            "연필", "볼펜", "지우개", "노트", "책", "가방", "신발", "옷", "바지", "셔츠", "안경", "시계",
            "pencil", "pen", "eraser", "notebook", "book", "bag", "shoes", "clothes", "pants", "shirt", "glasses", "watch",
            // 신체/생물/의료
            "사람", "인간", "손가락", "발가락", "머리카락", "피", "뼈", "살점", "내장", "눈알", "심장",
            "human", "person", "finger", "toe", "hair", "blood", "bone", "skin", "heart", "eye",
            "강아지", "고양이", "반려견", "반려묘", "햄스터", "토끼", "금붕어", "앵무새",
            "dog", "cat", "pet", "hamster", "rabbit", "fish", "parrot",
            "약", "알약", "주사기", "반창고", "붕대", "바이러스", "세균", "medicine", "pill", "drug", "virus", "bacteria",
            // 비속어/욕설
            "바보", "멍청이", "쓰레기", "개새끼", "시발", "씨발", "존나", "졸라", "병신",
            "trash", "garbage", "shit", "fuck", "damn", "idiot", "stupid", "asshole",
            // 위험물/화학물질/무기
            "독", "poison", "세제", "detergent", "비누", "soap", "샴푸", "shampoo", "치약", "toothpaste",
            "락스", "bleach", "농약", "pesticide", "휘발유", "gasoline", "기름", "oil", "폭탄", "bomb", "총", "gun", "칼", "knife",
            "담배", "cigarette",
            // 사회/정치/경제/종교
            "대통령", "정치", "경제", "주식", "코인", "비트코인", "예수", "부처", "알라", "하나님", "신",
            "president", "politics", "economy", "stock", "coin", "bitcoin", "god", "jesus", "buddha"
    ));

    // 단위 단어들
    private static final Set<String> UNIT_WORDS = new HashSet<>(Arrays.asList(
            "개", "쪽", "큰술", "작은술", "티스푼", "스푼", "컵", "줌", "꼬집", "팩", "봉지", "마리",
            "톨", "알", "조각", "모", "판", "줄", "캔", "병", "통", "박스", "상자", "포",
            "단", "묶음", "채", "뿌리", "포기", "장", "접시", "공기", "사발", "대접", "바구니", "망",
            "근", "관", "돈", "냥", "되", "말", "홉", "리터", "밀리리터", "킬로그램", "그램",
            "인분", "그릇", "모금", "방울", "스쿱", "덩어리", "자루", "송이", "손", "마디", "움큼", "가마니",
            "g", "ml", "kg", "l", "oz", "lb", "cup", "cups", "tbsp", "tsp", "pt", "qt", "gal", "mg",
            "gram", "grams", "kilogram", "kilograms", "liter", "liters", "milliliter", "milliliters",
            "pound", "pounds", "ounce", "ounces", "pint", "pints", "quart", "quarts", "gallon", "gallons",
            "cloves", "clove", "pieces", "piece", "slices", "slice", "bunch", "bunches", "pinch", "pinches",
            "dash", "dashes", "can", "cans", "bottle", "bottles", "package", "packages", "bag", "bags",
            "box", "boxes", "stick", "sticks", "head", "heads", "stalk", "stalks", "bulb", "bulbs",
            "serving", "servings", "fillet", "fillets", "whole", "drop", "drops", "scoop", "scoops",
            "handful", "handfuls", "sprig", "sprigs", "sheet", "sheets",
            "個", "個入り", "本", "枚", "杯", "大さじ", "小さじ", "袋", "パック", "箱", "缶", "瓶",
            "匹", "頭", "羽", "房", "丁", "束", "玉", "切れ", "片", "粒", "皿", "合", "升", "斤", "尾"
    ));

    // 형용사/수식어
    private static final Set<String> ADJECTIVES = new HashSet<>(Arrays.asList(
            "큰", "작은", "다진", "썬", "잘게", "굵게", "얇게", "두껍게", "채썬", "깍둑썬", "으깬", "간",
            "어슷썬", "반으로", "자른", "통", "갈은", "다져놓은", "슬라이스한", "납작하게", "길게", "잘라놓은", "손질된", "껍질벗긴",
            "large", "small", "big", "chopped", "minced", "sliced", "diced", "ground", "crushed", "shredded",
            "grated", "whole", "halved", "quartered", "cubed", "mashed", "pureed", "julienned", "peeled", "trimmed",
            "大きい", "小さい", "刻み", "切り", "スライス", "千切り", "角切り", "すりおろし", "挽き", "丸ごと", "半月切り", "皮むき",
            "신선한", "냉동", "해동", "삶은", "구운", "볶은", "데친", "튀긴", "찐", "절인", "말린", "건조", "훈제", "숙성", "익힌",
            "생", "날것의", "가공", "조리된", "데워먹는", "즉석", "인스턴트", "불린", "삭힌", "염장", "발효", "반조리", "가열된",
            "fresh", "organic", "frozen", "thawed", "boiled", "grilled", "fried", "steamed", "pickled", "dried",
            "smoked", "aged", "cooked", "roasted", "baked", "raw", "instant", "processed", "cured", "marinated", "fermented",
            "新鮮な", "冷凍", "解凍", "ボイル", "焼き", "炒め", "茹で", "揚げ", "蒸し", "漬け", "乾燥", "燻製", "熟成",
            "生", "加工", "即席", "発酵",
            "맛있는", "매운", "달콤한", "상한", "남은", "자투리", "국산", "수입산", "유기농", "무농약", "친환경",
            "저염", "저지방", "고단백", "프리미엄", "특급", "햇", "묵은", "제철", "싱싱한", "최고급",
            "delicious", "spicy", "sweet", "rotten", "leftover", "domestic", "imported", "organic",
            "low-sodium", "low-fat", "premium", "grade-a", "seasonal",
            "美味しい", "辛い", "甘い", "残り", "国産", "輸入", "有機", "無農薬", "旬の", "高級"
    ));

    public IngredientService(IngredientRepository ingredientRepository,
                             GeminiClient geminiClient,
                             ObjectMapper objectMapper) {
        this.ingredientRepository = ingredientRepository;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 재료 입력값 검증
     * 
     * 동작 원리:
     * 1. 정규식 검사: 허용된 문자(한글/영어/일본어/한자/공백) 외 입력 차단
     * 2. 블랙리스트 검사: 비식재료(사랑, 돈 등 추상명사) 필터링
     * 3. 길이 검사: 1~50자 범위 체크
     * 
     * @param inputs 검증할 재료 입력 목록
     * @return 각 입력의 유효성 결과 목록
     */
    public List<GachaDto.ValidationResult> validateIngredients(List<GachaDto.IngredientInput> inputs) {
        List<GachaDto.ValidationResult> results = new ArrayList<>();
        if (inputs == null) return results;

        for (GachaDto.IngredientInput input : inputs) {
            String name = input.getName() != null ? input.getName().trim() : "";
            if (name.isEmpty()) {
                continue;
            }

            // Check regex pattern
            if (!VALID_INGREDIENT_PATTERN.matcher(name).matches()) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("許可されていない文字が含まれています").build());
                continue;
            }

            // Check blacklist
            String lowerName = name.toLowerCase();
            boolean blacklisted = BLACKLIST.stream()
                    .anyMatch(word -> lowerName.contains(word));
            if (blacklisted) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("素材ではない項目です").build());
                continue;
            }

            // Check minimum length
            if (name != null && (name.length() < 1 || name.length() > 50)) {
                results.add(GachaDto.ValidationResult.builder()
                        .valid(false).name(name).reason("素材名は1～50文字である必要があります").build());
                continue;
            }

            results.add(GachaDto.ValidationResult.builder()
                    .valid(true).name(name).reason(null).build());
        }

        return results;
    }

    /**
     * 3단계 재료명 정규화 프로세스
     * 
     * 동작 원리:
     * 1단계: 코드 기반 전처리 - 숫자, 단위, 형용사 제거
     * 2단계: 별칭 DB 조회 - 이미 등록된 동의어가 있으면 재사용 (AI 비용 절감)
     * 3단계: AI 정규화 - 새로운 재료명이면 Gemini API로 표준화
     * 
     * 예시: "큰 신선한 다진 양파 2개" → "양파"

-
    public String normalize(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return rawName != null ? rawName.trim() : "";
        }

        // 1단계: 전처리 (숫자/단위/형용사 제거)
        String cleaned = preprocessName(rawName.trim());

        // 2단계: 별칭 테이블에서 기존 매핑 조회
        // 예: "대파"가 입력되면 "파"로 매핑된 기록이 있는지 확인
        String aliasResult = ingredientRepository.findNormalizedByAlias(cleaned);
        if (aliasResult != null && !aliasResult.isEmpty()) return aliasResult;

        // 원본 이름으로도 별칭 검색 (전처리 전/후 모두 검색)
        if (!cleaned.equals(rawName.trim())) {
            aliasResult = ingredientRepository.findNormalizedByAlias(rawName.trim());
            if (aliasResult != null && !aliasResult.isEmpty()) return aliasResult;
        }

        // 3단계: AI 정규화 (별칭을 찾지 못한 경우에만 수행)
        // 비용 절감을 위해 캐싱(별칭 테이블)을 먼저 확인하고 AI는 마지막에 호출
        try {
            String aiResult = geminiClient.normalizeIngredient(rawName);
            JsonNode node = objectMapper.readTree(aiResult);
            String normalized = node.path("normalized").asText("").trim();

            if (normalized.isEmpty()) {
                normalized = cleaned;
            }

            if (!normalized.isEmpty()) {
                // AI 결과를 별칭 테이블에 저장하여 다음번 재사용
                IngredientEntity.Alias alias = IngredientEntity.Alias.builder()
                        .aliasName(rawName.trim())
                        .normalizedName(normalized)
                        .build();
                try {
                    ingredientRepository.insertAlias(alias);
                    ingredientRepository.insertIngredient(normalized);
                } catch (Exception e) {
                    log.warn("별칭 저장 실패: {}", e.getMessage());
                }
                return normalized;
            }
        } catch (Exception e) {
            log.warn("AI normalization failed for '{}': {}", rawName, e.getMessage());
        }

        // 폴백: 전처리된 이름 반환, 실패 시 원본 반환
        return !cleaned.isEmpty() ? cleaned : rawName.trim();
    }

    /**
     * 1단계: 코드 기반 전처리
     * 
     * 동작 원리:
     * - 정규식으로 숫자 제거 (예: "2개" → "개")
     * - 단위 단어 목록 순회하며 제거 (g, ml, 개, 個 등)
     * - 형용사 목록 순회하며 제거 (큰, 작은, 신선한, chopped 등)
     * - 중복 공백 정리
     * 
     * @param name 원본 재료명
     * @return 전처리된 재료명
     */
    private String preprocessName(String name) {
        if (name == null) return "";

        String result = name;

        // 숫자 제거 (소수점 포함)
        // 예: "2.5kg 양파" → "kg 양파"
        result = result.replaceAll("[0-9]+\\.?[0-9]*", "").trim();

        // 단위 단어 제거
        // 영어 단어는 단어 경계(\b) 사용, 한글/일본어는 직접 replace
        for (String unit : UNIT_WORDS) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(unit) + "\\b", "").trim();
            // Also handle Korean/Japanese unit words without word boundaries
            if (unit.matches("[가-힣\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF]+")) {
                result = result.replace(unit, "").trim();
            }
        }

        // 형용사/상태 수식어 제거
        for (String adj : ADJECTIVES) {
            result = result.replaceAll("(?i)\\b" + Pattern.quote(adj) + "\\b", "").trim();
            if (adj.matches("[가-힣\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF]+")) {
                result = result.replace(adj, "").trim();
            }
        }

        // 중복 공백 정리
        result = result.replaceAll("\\s+", " ").trim();

        // 전처리 결과가 비어있으면 원본 반환 (데이터 손실 방지)
        return result.isEmpty() ? name : result;
    }

    /**
     * Get user's ingredients by type
     */
    public List<IngredientRepository.UserIngredient> getMyIngredients(Long userId, Integer type) {
        return ingredientRepository.findByUserId(userId, type);
    }

    /**
     * 재료 등록 또는 업데이트
     * 
     * 동작 원리:
     * 1. 입력 검증: 빈 값 체크 → 허용 문자 정규식 검사
     * 2. 정규화: 3단계 정규화 프로세스 실행 (preprocess → alias → AI)
     * 3. DB 저장: 원본명 + 정규화된 표준명 함께 저장
     * 
     * 프론트엔드에서 이미 키보드 입력을 제어하지만,
     * 서버에서도 한번 더 검증하여 보안/데이터 무결성 확보
     * 
     * @param userId 사용자 ID
     * @param name 입력한 재료명 (원본)
     * @param quantity 수량
     * @param unit 단위
     * @param type 1=식재료, 2=소스
     * @return 저장된 사용자 재료 정보
     * @throws IllegalArgumentException 허용되지 않은 문자가 포함된 경우
     */
    @Transactional
    public IngredientRepository.UserIngredient addIngredient(Long userId, String name, Double quantity, String unit, Integer type) {
        // 1단계: 기본 입력 검증
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("食材名は必須です");
        }

        // 2단계: 정규식 패턴 검증 (한글/영어/일본어/한자/공백만 허용)
        // 프론트엔드와 동일한 정규식으로 이중 검증
        if (!VALID_INGREDIENT_PATTERN.matcher(name.trim()).matches()) {
            throw new IllegalArgumentException("食材名には韓国語、英語、日本語、漢字のみ使用できます");
        }

        // 3단계: 재료명 정규화 (표준화된 이름 생성)
        // 예: "큰 신선한 다진 양파 2개" → "양파"
        String normalized = normalize(name);

        // 4단계: DB에 저장 (원본명 + 정규화된 표준명)
        IngredientRepository.UserIngredient ingredient = IngredientRepository.UserIngredient.builder()
                .userId(userId)
                .name(name)           // 사용자가 입력한 원본
                .normalizedName(normalized)  // AI/룰 기반 표준화된 이름
                .quantity(quantity)
                .unit(unit)
                .type(type)
                .build();

        ingredientRepository.insertUserIngredient(ingredient);

        return ingredient;
    }

    /**
     * 재료 삭제 (Soft Delete)
     * 
     * 실제로 DB에서 삭제하지 않고 deleted 플래그만 설정
     * 
     * @param userId 사용자 ID (본인 확인용)
     * @param id 삭제할 재료 ID
     * @return 삭제 성공 여부
     */
    @Transactional
    public boolean deleteIngredient(Long userId, Long id) {
        int updated = ingredientRepository.deleteUserIngredient(id, userId);
        return updated > 0;
    }
}