package com.ll.products.config;

import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductStatus;
import com.ll.products.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
//@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("=== 굿즈 중고거래 플랫폼 테스트 데이터 생성 ===");

        // 1. 최상위 카테고리 생성
        log.info("1. 최상위 카테고리 생성 중...");

        Category idolCategory = Category.builder()
                .name("아이돌 굿즈")
                .build();
        categoryRepository.save(idolCategory);
        log.info("  ✓ 아이돌 굿즈 (ID: {})", idolCategory.getId());

        Category animeCategory = Category.builder()
                .name("애니메이션 굿즈")
                .build();
        categoryRepository.save(animeCategory);
        log.info("  ✓ 애니메이션 굿즈 (ID: {})", animeCategory.getId());

        Category gameCategory = Category.builder()
                .name("게임 굿즈")
                .build();
        categoryRepository.save(gameCategory);
        log.info("  ✓ 게임 굿즈 (ID: {})", gameCategory.getId());

        Category characterCategory = Category.builder()
                .name("캐릭터 굿즈")
                .build();
        categoryRepository.save(characterCategory);
        log.info("  ✓ 캐릭터 굿즈 (ID: {})", characterCategory.getId());

        Category movieCategory = Category.builder()
                .name("영화/드라마 굿즈")
                .build();
        categoryRepository.save(movieCategory);
        log.info("  ✓ 영화/드라마 굿즈 (ID: {})", movieCategory.getId());

        // 2. 하위 카테고리 생성
        log.info("");
        log.info("2. 하위 카테고리 생성 중...");

        // 아이돌 하위 카테고리
        Category btsCategory = Category.builder()
                .name("방탄소년단")
                .build();
        btsCategory.setParent(idolCategory);
        categoryRepository.save(btsCategory);
        log.info("  ✓ 아이돌 굿즈 > 방탄소년단 (ID: {})", btsCategory.getId());

        Category blackpinkCategory = Category.builder()
                .name("블랙핑크")
                .build();
        blackpinkCategory.setParent(idolCategory);
        categoryRepository.save(blackpinkCategory);
        log.info("  ✓ 아이돌 굿즈 > 블랙핑크 (ID: {})", blackpinkCategory.getId());

        Category newjeansCategory = Category.builder()
                .name("뉴진스")
                .build();
        newjeansCategory.setParent(idolCategory);
        categoryRepository.save(newjeansCategory);
        log.info("  ✓ 아이돌 굿즈 > 뉴진스 (ID: {})", newjeansCategory.getId());

        Category aespaCategory = Category.builder()
                .name("에스파")
                .build();
        aespaCategory.setParent(idolCategory);
        categoryRepository.save(aespaCategory);
        log.info("  ✓ 아이돌 굿즈 > 에스파 (ID: {})", aespaCategory.getId());

        Category seventeenCategory = Category.builder()
                .name("세븐틴")
                .build();
        seventeenCategory.setParent(idolCategory);
        categoryRepository.save(seventeenCategory);
        log.info("  ✓ 아이돌 굿즈 > 세븐틴 (ID: {})", seventeenCategory.getId());

        // 애니메이션 하위 카테고리
        Category onePieceCategory = Category.builder()
                .name("원피스")
                .build();
        onePieceCategory.setParent(animeCategory);
        categoryRepository.save(onePieceCategory);
        log.info("  ✓ 애니메이션 굿즈 > 원피스 (ID: {})", onePieceCategory.getId());

        Category narutoCategory = Category.builder()
                .name("나루토")
                .build();
        narutoCategory.setParent(animeCategory);
        categoryRepository.save(narutoCategory);
        log.info("  ✓ 애니메이션 굿즈 > 나루토 (ID: {})", narutoCategory.getId());

        Category demonSlayerCategory = Category.builder()
                .name("귀멸의 칼날")
                .build();
        demonSlayerCategory.setParent(animeCategory);
        categoryRepository.save(demonSlayerCategory);
        log.info("  ✓ 애니메이션 굿즈 > 귀멸의 칼날 (ID: {})", demonSlayerCategory.getId());

        // 게임 하위 카테고리
        Category leagueCategory = Category.builder()
                .name("리그오브레전드")
                .build();
        leagueCategory.setParent(gameCategory);
        categoryRepository.save(leagueCategory);
        log.info("  ✓ 게임 굿즈 > 리그오브레전드 (ID: {})", leagueCategory.getId());

        Category valorantCategory = Category.builder()
                .name("발로란트")
                .build();
        valorantCategory.setParent(gameCategory);
        categoryRepository.save(valorantCategory);
        log.info("  ✓ 게임 굿즈 > 발로란트 (ID: {})", valorantCategory.getId());

        Category overwatchCategory = Category.builder()
                .name("오버워치")
                .build();
        overwatchCategory.setParent(gameCategory);
        categoryRepository.save(overwatchCategory);
        log.info("  ✓ 게임 굿즈 > 오버워치 (ID: {})", overwatchCategory.getId());

        // 캐릭터 하위 카테고리
        Category sanrioCategory = Category.builder()
                .name("산리오")
                .build();
        sanrioCategory.setParent(characterCategory);
        categoryRepository.save(sanrioCategory);
        log.info("  ✓ 캐릭터 굿즈 > 산리오 (ID: {})", sanrioCategory.getId());

        Category disneyCategory = Category.builder()
                .name("디즈니")
                .build();
        disneyCategory.setParent(characterCategory);
        categoryRepository.save(disneyCategory);
        log.info("  ✓ 캐릭터 굿즈 > 디즈니 (ID: {})", disneyCategory.getId());

        Category linefriendsCategory = Category.builder()
                .name("라인프렌즈")
                .build();
        linefriendsCategory.setParent(characterCategory);
        categoryRepository.save(linefriendsCategory);
        log.info("  ✓ 캐릭터 굿즈 > 라인프렌즈 (ID: {})", linefriendsCategory.getId());

        // 영화/드라마 하위 카테고리
        Category marvelCategory = Category.builder()
                .name("마블")
                .build();
        marvelCategory.setParent(movieCategory);
        categoryRepository.save(marvelCategory);
        log.info("  ✓ 영화/드라마 굿즈 > 마블 (ID: {})", marvelCategory.getId());

        Category harryPotterCategory = Category.builder()
                .name("해리포터")
                .build();
        harryPotterCategory.setParent(movieCategory);
        categoryRepository.save(harryPotterCategory);
        log.info("  ✓ 영화/드라마 굿즈 > 해리포터 (ID: {})", harryPotterCategory.getId());

        // 카테고리 맵 생성
        Map<String, Category> categoryMap = new HashMap<>();
        categoryMap.put("방탄소년단", btsCategory);
        categoryMap.put("블랙핑크", blackpinkCategory);
        categoryMap.put("뉴진스", newjeansCategory);
        categoryMap.put("에스파", aespaCategory);
        categoryMap.put("세븐틴", seventeenCategory);
        categoryMap.put("원피스", onePieceCategory);
        categoryMap.put("나루토", narutoCategory);
        categoryMap.put("귀멸의 칼날", demonSlayerCategory);
        categoryMap.put("리그오브레전드", leagueCategory);
        categoryMap.put("발로란트", valorantCategory);
        categoryMap.put("오버워치", overwatchCategory);
        categoryMap.put("산리오", sanrioCategory);
        categoryMap.put("디즈니", disneyCategory);
        categoryMap.put("라인프렌즈", linefriendsCategory);
        categoryMap.put("마블", marvelCategory);
        categoryMap.put("해리포터", harryPotterCategory);

        // 3. 상품 생성 (CSV 파일)
        log.info("");
        log.info("3. 상품 데이터 CSV 파일에서 로드 중...");

        try {
            ClassPathResource resource = new ClassPathResource("data/products_720.csv");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                reader.readLine();
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length == 7) {
                        String categoryName = fields[0];
                        String name = fields[1];
                        String description = fields[2];
                        int price = Integer.parseInt(fields[3]);
                        int quantity = Integer.parseInt(fields[4]);
                        String sellerName = fields[5];
                        ProductStatus status = ProductStatus.valueOf(fields[6]);

                        Category category = categoryMap.get(categoryName);
                        if (category != null) {
                            Product product = Product.builder()
                                    .name(name)
                                    .category(category)
                                    .sellerCode("seller" + String.format("%03d", (count % 10) + 1))
                                    .sellerName(sellerName)
                                    .quantity(quantity)
                                    .description(description)
                                    .price(price)
                                    .status(status)
                                    .isDeleted(false)
                                    .build();
                            productRepository.save(product);
                            count++;
                        }
                    }
                }
                log.info("  ✓ CSV에서 {}개 상품 로드 완료", count);
            }
        } catch (Exception e) {
            log.error("CSV 파일 로드 실패", e);
            throw new RuntimeException("CSV 파일 로드 중 오류 발생", e);
        }
        log.info("========================================");
        log.info("굿즈 중고거래 플랫폼 테스트 데이터 생성 완료");
        log.info("카테고리: {}개", categoryRepository.count());
        log.info("상품: {}개", productRepository.count());
        log.info("========================================");
    }
}
