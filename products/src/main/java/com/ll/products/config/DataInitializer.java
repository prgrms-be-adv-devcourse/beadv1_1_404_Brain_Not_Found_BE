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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
@Profile("!prod") // 프로덕션 환경에서는 실행되지 않도록
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

        // 3. 상품 생성
        log.info("");
        log.info("3. 상품 생성 중...");
        log.info("");
        log.info("📦 방탄소년단 굿즈 생성 중...");

        Product btsAlbum = Product.builder()
                .name("BTS MAP OF THE SOUL 7 앨범")
                .category(btsCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(3)
                .description("BTS 정규 4집 앨범입니다. 포토카드 포함, 미개봉 새상품.")
                .price(25000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(btsAlbum);

        Product btsPhotocard = Product.builder()
                .name("BTS 지민 포토카드 세트")
                .category(btsCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(5)
                .description("지민 공식 포토카드 5장 세트. Butter 앨범 포카 포함.")
                .price(15000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(btsPhotocard);

        Product btsArmyBomb = Product.builder()
                .name("BTS 공식 응원봉 ARMY BOMB")
                .category(btsCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(2)
                .description("BTS 3세대 공식 응원봉. 블루투스 연동 가능.")
                .price(45000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(btsArmyBomb);

        Product btsWinterPackage = Product.builder()
                .name("BTS 윈터 패키지 2023")
                .category(btsCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(1)
                .description("2023 시즌그리팅 윈터 패키지. 미개봉 풀박스.")
                .price(55000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(btsWinterPackage);

        Product btsJungkookCard = Product.builder()
                .name("BTS 정국 포토카드")
                .category(btsCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(10)
                .description("정국 공식 포토카드 단품. BE 앨범 버전. 상태 A급.")
                .price(8000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(btsJungkookCard);
        log.info("  ✓ BTS 굿즈 5개 생성 완료");

        // 블랙핑크 굿즈
        log.info("📦 블랙핑크 굿즈 생성 중...");

        Product bpAlbum = Product.builder()
                .name("블랙핑크 BORN PINK 앨범")
                .category(blackpinkCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(4)
                .description("블랙핑크 2집 정규앨범. 포토카드 랜덤 포함. 미개봉 새상품.")
                .price(22000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(bpAlbum);

        Product bpJennieCard = Product.builder()
                .name("블랙핑크 제니 포토카드")
                .category(blackpinkCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(6)
                .description("제니 공식 포토카드. Pink Venom 버전. 상태 S급.")
                .price(12000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(bpJennieCard);

        Product bpLightstick = Product.builder()
                .name("블랙핑크 공식 응원봉")
                .category(blackpinkCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(3)
                .description("블랙핑크 공식 응원봉 1세대. 정품 인증 완료.")
                .price(50000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(bpLightstick);
        log.info("  ✓ 블랙핑크 굿즈 3개 생성 완료");

        // 뉴진스 굿즈
        log.info("📦 뉴진스 굿즈 생성 중...");

        Product newjeansAlbum = Product.builder()
                .name("뉴진스 Get Up 앨범")
                .category(newjeansCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(5)
                .description("뉴진스 1집 앨범 Get Up. 버니비치백 버전. 포토카드 포함.")
                .price(20000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(newjeansAlbum);

        Product newjeansMinjiCard = Product.builder()
                .name("뉴진스 민지 포토카드")
                .category(newjeansCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(4)
                .description("민지 공식 포토카드. OMG 앨범 버전. 상태 S급.")
                .price(15000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(newjeansMinjiCard);

        Product newjeansBunny = Product.builder()
                .name("뉴진스 공식 토끼 인형")
                .category(newjeansCategory)
                .sellerCode("seller001")
                .sellerName("판매자1")
                .quantity(2)
                .description("뉴진스 공식 캐릭터 토끼 인형. 중형 사이즈. 미사용 새제품.")
                .price(35000)
                .status(ProductStatus.ON_SALE)
                .isDeleted(false)
                .build();
        productRepository.save(newjeansBunny);
        log.info("  ✓ 뉴진스 굿즈 3개 생성 완료");

        log.info("");
        log.info("🎉 굿즈 중고거래 플랫폼 테스트 데이터 생성 완료");
        log.info("  - 카테고리: {}개", categoryRepository.count());
        log.info("  - 상품: {}개", productRepository.count());
        log.info("========================================");
    }
}

