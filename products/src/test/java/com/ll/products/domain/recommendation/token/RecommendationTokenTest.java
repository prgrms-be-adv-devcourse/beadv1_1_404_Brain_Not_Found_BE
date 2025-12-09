package com.ll.products.domain.recommendation.token;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;

public class RecommendationTokenTest {

    public static void main(String[] args) {
        Encoding encoding = Encodings.newDefaultEncodingRegistry().getEncodingForModel("text-embedding-3-large").get();

        String productMetadata = "상품명: 블랙핑크 BORN PINK 앨범. 설명: 블랙핑크 2집 정규앨범입니다. 포토카드 랜덤 포함이고 미개봉 새상품이에요. 네고 불가능합니다. 기타 설명은 이렇고 저렇고 그렇습니다. 더 자세한 설명은 이미지를 참조하세요.. 카테고리: 블랙핑크. 가격: 22,000원.";
        String keyword = "블랙핑크 굿즈";
        int metadataTokenCount = encoding.countTokens(productMetadata);
        int keywordTokenCount = encoding.countTokens(keyword);

        System.out.println("Metadata Token Count: " + metadataTokenCount);
        System.out.println("Keyword Token Count: " + keywordTokenCount);
    }
}
