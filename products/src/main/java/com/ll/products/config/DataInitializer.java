package com.ll.products.config;

import com.ll.products.domain.category.model.entity.Category;
import com.ll.products.domain.category.repository.CategoryRepository;
import com.ll.products.domain.product.model.entity.Product;
import com.ll.products.domain.product.model.entity.ProductImage;
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

        // 에스파 굿즈
        log.info("📦 에스파 굿즈 생성 중...");
        productRepository.save(Product.builder().name("에스파 DRAMA 앨범").category(aespaCategory).sellerCode("seller002").sellerName("판매자2").quantity(5).description("에스파 미니 4집 DRAMA. 포토북 및 포토카드 포함. 미개봉 새상품.").price(18000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("에스파 윈터 포토카드").category(aespaCategory).sellerCode("seller002").sellerName("판매자2").quantity(8).description("윈터 공식 포토카드. Savage 앨범 버전. 상태 S급.").price(20000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("에스파 나비 막대").category(aespaCategory).sellerCode("seller002").sellerName("판매자2").quantity(3).description("에스파 공식 응원봉 나비 막대. 1세대 버전.").price(42000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("에스파 카리나 포토카드").category(aespaCategory).sellerCode("seller002").sellerName("판매자2").quantity(7).description("카리나 공식 포토카드. Girls 앨범 버전.").price(25000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("에스파 시즌 그리팅 2024").category(aespaCategory).sellerCode("seller002").sellerName("판매자2").quantity(2).description("에스파 2024 시즌그리팅. 미개봉 풀세트.").price(48000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 에스파 굿즈 5개 생성 완료");

        // 세븐틴 굿즈
        log.info("📦 세븐틴 굿즈 생성 중...");
        productRepository.save(Product.builder().name("세븐틴 FML 앨범").category(seventeenCategory).sellerCode("seller002").sellerName("판매자2").quantity(6).description("세븐틴 정규 10집 FML 앨범. 포토카드 랜덤 포함.").price(19000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("세븐틴 에스쿱스 포토카드").category(seventeenCategory).sellerCode("seller002").sellerName("판매자2").quantity(4).description("에스쿱스 공식 포토카드. HOT 앨범 버전.").price(12000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("세븐틴 공식 응원봉").category(seventeenCategory).sellerCode("seller002").sellerName("판매자2").quantity(2).description("세븐틴 크랫봉 버전2. 블루투스 연동 가능.").price(47000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("세븐틴 디노 포토카드").category(seventeenCategory).sellerCode("seller002").sellerName("판매자2").quantity(5).description("디노 공식 포토카드. Face the Sun 버전.").price(10000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("세븐틴 Season's Greetings").category(seventeenCategory).sellerCode("seller002").sellerName("판매자2").quantity(3).description("2024 시즌그리팅. 다이어리 포함 풀세트.").price(45000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 세븐틴 굿즈 5개 생성 완료");

        // 원피스 굿즈
        log.info("📦 원피스 굿즈 생성 중...");
        productRepository.save(Product.builder().name("원피스 루피 피규어").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(4).description("몽키 D. 루피 기어5 피규어. 프리미엄 버전. 미개봉.").price(85000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("원피스 조로 피규어").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(3).description("로로노아 조로 삼도류 버전 피규어. 한정판.").price(78000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("원피스 에이스 피규어").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(2).description("포트거스 D 에이스 메라메라 열매 버전. 희귀템.").price(95000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("원피스 만화책 1-105권").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(1).description("원피스 만화책 전권 세트. 1권부터 105권. 상태 양호.").price(350000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("원피스 초퍼 인형").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(10).description("토니토니 초퍼 인형. 대형 사이즈. 공식 라이센스.").price(35000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("원피스 카드게임 부스터박스").category(onePieceCategory).sellerCode("seller003").sellerName("판매자3").quantity(5).description("원피스 카드게임 부스터 박스. 미개봉 정품.").price(52000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 원피스 굿즈 6개 생성 완료");

        // 나루토 굿즈
        log.info("📦 나루토 굿즈 생성 중...");
        productRepository.save(Product.builder().name("나루토 우즈마키 나루토 피규어").category(narutoCategory).sellerCode("seller003").sellerName("판매자3").quantity(5).description("나루토 선인모드 피규어. 고퀄리티 한정판.").price(68000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("나루토 사스케 피규어").category(narutoCategory).sellerCode("seller003").sellerName("판매자3").quantity(4).description("우치하 사스케 사륜안 버전. 디테일 최상급.").price(72000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("나루토 카카시 피규어").category(narutoCategory).sellerCode("seller003").sellerName("판매자3").quantity(3).description("하타케 카카시 사륜안 버전. 인기상품.").price(65000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("나루토 아카츠키 망토").category(narutoCategory).sellerCode("seller003").sellerName("판매자3").quantity(6).description("나루토 아카츠키 공식 코스프레 망토. 사이즈 L.").price(45000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("나루토 만화책 전권").category(narutoCategory).sellerCode("seller003").sellerName("판매자3").quantity(1).description("나루토 만화책 1-72권 완결 세트. 상태 A급.").price(280000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 나루토 굿즈 5개 생성 완료");

        // 귀멸의 칼날 굿즈
        log.info("📦 귀멸의 칼날 굿즈 생성 중...");
        productRepository.save(Product.builder().name("귀멸의 칼날 탄지로 피규어").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(7).description("카마도 탄지로 히노카미 카구라 버전 피규어.").price(58000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("귀멸의 칼날 네즈코 피규어").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(6).description("카마도 네즈코 귀화 버전. 고퀄리티 피규어.").price(62000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("귀멸의 칼날 렌고쿠 피규어").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(4).description("렌고쿠 쿄쥬로 염주 버전. 한정판 상품.").price(75000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("귀멸의 칼날 일륜도 키링").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(15).description("귀멸의 칼날 일륜도 미니어처 키링. 여러 캐릭터 버전.").price(8000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("귀멸의 칼날 만화책 1-23권").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(2).description("귀멸의 칼날 완결 세트. 전권 새책.").price(180000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("귀멸의 칼날 시노부 피규어").category(demonSlayerCategory).sellerCode("seller003").sellerName("판매자3").quantity(5).description("코쵸 시노부 벌주 버전. 고퀄리티.").price(68000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 귀멸의 칼날 굿즈 6개 생성 완료");

        // 리그오브레전드 굿즈
        log.info("📦 리그오브레전드 굿즈 생성 중...");
        productRepository.save(Product.builder().name("LOL 아리 피규어").category(leagueCategory).sellerCode("seller004").sellerName("판매자4").quantity(4).description("리그오브레전드 아리 K/DA 버전 피규어. 공식 라이센스.").price(95000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("LOL 야스오 피규어").category(leagueCategory).sellerCode("seller004").sellerName("판매자4").quantity(3).description("야스오 하이눈 스킨 피규어. 한정판.").price(88000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("LOL 징크스 피규어").category(leagueCategory).sellerCode("seller004").sellerName("판매자4").quantity(5).description("징크스 오디세이 스킨 버전. 고퀄리티.").price(82000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("LOL 공식 마우스패드").category(leagueCategory).sellerCode("seller004").sellerName("판매자4").quantity(10).description("리그오브레전드 공식 게이밍 마우스패드. 대형.").price(35000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("LOL 월드챔피언십 기념 저지").category(leagueCategory).sellerCode("seller004").sellerName("판매자4").quantity(2).description("2023 LOL 월즈 우승팀 T1 저지. 공식 상품.").price(120000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 리그오브레전드 굿즈 5개 생성 완료");

        // 발로란트 굿즈
        log.info("📦 발로란트 굿즈 생성 중...");
        productRepository.save(Product.builder().name("발로란트 제트 피규어").category(valorantCategory).sellerCode("seller004").sellerName("판매자4").quantity(6).description("발로란트 제트 캐릭터 피규어. 공식 라이센스.").price(65000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("발로란트 레이나 피규어").category(valorantCategory).sellerCode("seller004").sellerName("판매자4").quantity(4).description("레이나 캐릭터 피규어. 고퀄리티 도색.").price(68000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("발로란트 공식 키보드").category(valorantCategory).sellerCode("seller004").sellerName("판매자4").quantity(3).description("발로란트 에디션 게이밍 키보드. 기계식 청축.").price(180000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("발로란트 마우스").category(valorantCategory).sellerCode("seller004").sellerName("판매자4").quantity(5).description("발로란트 공식 게이밍 마우스. RGB 라이팅.").price(95000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("발로란트 포스터 세트").category(valorantCategory).sellerCode("seller004").sellerName("판매자4").quantity(8).description("발로란트 캐릭터 포스터 5종 세트. A2 사이즈.").price(25000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 발로란트 굿즈 5개 생성 완료");

        // 오버워치 굿즈
        log.info("📦 오버워치 굿즈 생성 중...");
        productRepository.save(Product.builder().name("오버워치 트레이서 피규어").category(overwatchCategory).sellerCode("seller004").sellerName("판매자4").quantity(5).description("트레이서 넨도로이드 피규어. 공식 제품.").price(58000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("오버워치 디바 피규어").category(overwatchCategory).sellerCode("seller004").sellerName("판매자4").quantity(4).description("D.Va 메카 포함 피규어 세트. 한정판.").price(120000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("오버워치 리퍼 피규어").category(overwatchCategory).sellerCode("seller004").sellerName("판매자4").quantity(3).description("리퍼 블랙워치 스킨 피규어.").price(72000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("오버워치 공식 헤드셋").category(overwatchCategory).sellerCode("seller004").sellerName("판매자4").quantity(2).description("오버워치 에디션 게이밍 헤드셋. 7.1 서라운드.").price(150000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("오버워치 아트북").category(overwatchCategory).sellerCode("seller004").sellerName("판매자4").quantity(6).description("오버워치 공식 아트북. 하드커버 한정판.").price(55000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 오버워치 굿즈 5개 생성 완료");

        // 산리오 굿즈
        log.info("📦 산리오 굿즈 생성 중...");
        productRepository.save(Product.builder().name("헬로키티 인형").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(10).description("헬로키티 대형 인형. 50cm 사이즈. 공식 라이센스.").price(45000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("시나모롤 파우치").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(15).description("시나모롤 방수 파우치. 여행용 대형 사이즈.").price(28000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("쿠로미 가방").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(8).description("쿠로미 에코백. 튼튼한 캔버스 소재.").price(35000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("마이멜로디 텀블러").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(12).description("마이멜로디 보온 텀블러. 500ml 용량.").price(32000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("폼폼푸린 쿠션").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(6).description("폼폼푸린 캐릭터 쿠션. 40cm 사이즈.").price(38000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("산리오 스티커 세트").category(sanrioCategory).sellerCode("seller005").sellerName("판매자5").quantity(20).description("산리오 캐릭터 스티커 100장 세트.").price(15000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 산리오 굿즈 6개 생성 완료");

        // 디즈니 굿즈
        log.info("📦 디즈니 굿즈 생성 중...");
        productRepository.save(Product.builder().name("미키마우스 피규어").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(8).description("클래식 미키마우스 피규어. 빈티지 스타일.").price(55000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("겨울왕국 엘사 인형").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(10).description("겨울왕국2 엘사 노래하는 인형. 공식 제품.").price(48000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("토이스토리 우디 인형").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(5).description("토이스토리 우디 음성 인형. 45cm 대형.").price(62000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("디즈니 프린세스 피규어 세트").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(4).description("디즈니 프린세스 7종 피규어 세트.").price(85000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("스티치 인형").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(12).description("릴로 앤 스티치 대형 인형. 60cm 사이즈.").price(52000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("디즈니 레고 성 세트").category(disneyCategory).sellerCode("seller005").sellerName("판매자5").quantity(3).description("디즈니 캐슬 레고 세트. 4000피스 이상.").price(380000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 디즈니 굿즈 6개 생성 완료");

        // 라인프렌즈 굿즈
        log.info("📦 라인프렌즈 굿즈 생성 중...");
        productRepository.save(Product.builder().name("브라운 인형").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(15).description("라인프렌즈 브라운 대형 인형. 70cm 사이즈.").price(58000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("코니 인형").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(12).description("라인프렌즈 코니 중형 인형. 50cm.").price(45000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("샐리 파우치").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(20).description("라인프렌즈 샐리 화장품 파우치.").price(22000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("BT21 타타 인형").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(10).description("BT21 타타 캐릭터 인형. BTS 콜라보.").price(42000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("BT21 쿠키 인형").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(10).description("BT21 쿠키 캐릭터 인형.").price(42000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("라인프렌즈 텀블러").category(linefriendsCategory).sellerCode("seller005").sellerName("판매자5").quantity(18).description("라인프렌즈 캐릭터 보온 텀블러 세트.").price(35000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 라인프렌즈 굿즈 6개 생성 완료");

        // 마블 굿즈
        log.info("📦 마블 굿즈 생성 중...");
        productRepository.save(Product.builder().name("아이언맨 헬멧 레플리카").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(2).description("아이언맨 마크 85 헬멧 1:1 레플리카. LED 작동.").price(450000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("캡틴 아메리카 방패").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(3).description("캡틴 아메리카 진동금 방패 레플리카. 금속 재질.").price(320000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("토르 망치 묠니르").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(4).description("토르 망치 묠니르 레플리카. 무거운 중량감.").price(280000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("스파이더맨 피규어").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(8).description("스파이더맨 노 웨이 홈 버전 피규어.").price(75000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("어벤져스 무한의 건틀릿").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(5).description("타노스 무한의 건틀릿 레플리카. LED 인피니티 스톤.").price(180000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("블랙 팬서 헬멧").category(marvelCategory).sellerCode("seller006").sellerName("판매자6").quantity(3).description("블랙 팬서 헬멧 레플리카. 와칸다 포에버.").price(250000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 마블 굿즈 6개 생성 완료");

        // 해리포터 굿즈
        log.info("📦 해리포터 굿즈 생성 중...");
        productRepository.save(Product.builder().name("해리포터 마법 지팡이").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(10).description("해리포터 공식 마법 지팡이. 올리밴더 제작.").price(55000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("호그와트 교복 그리핀도르").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(5).description("그리핀도르 기숙사 교복 풀세트. 망토 포함.").price(180000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("해리포터 마법책 세트").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(3).description("해리포터 전집 7권 양장본 특별판.").price(220000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("헤드윅 봉제인형").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(8).description("해리포터 올빼미 헤드윅 대형 인형.").price(48000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("골든 스니치").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(12).description("해리포터 골든 스니치 장식품. 날개 움직임.").price(35000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        productRepository.save(Product.builder().name("마법사의 체스 세트").category(harryPotterCategory).sellerCode("seller006").sellerName("판매자6").quantity(4).description("해리포터 마법사의 체스 럭셔리 세트.").price(280000).status(ProductStatus.ON_SALE).isDeleted(false).build());
        log.info("  ✓ 해리포터 굿즈 6개 생성 완료");

        log.info("");
        log.info("🎉 굿즈 중고거래 플랫폼 테스트 데이터 생성 완료");
        log.info("  - 카테고리: {}개", categoryRepository.count());
        log.info("  - 상품: {}개", productRepository.count());
        log.info("========================================");
    }
}

