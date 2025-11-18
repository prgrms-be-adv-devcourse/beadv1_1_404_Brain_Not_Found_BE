#!/bin/bash

BASE_URL="http://localhost:8085/api"
CATEGORY_URL="$BASE_URL/categories"
PRODUCT_URL="$BASE_URL/products"

echo "=== 굿즈 중고거래 플랫폼 테스트 데이터 생성 ==="

# 1. 최상위 카테고리 생성
echo ""
echo "1. 최상위 카테고리 생성 중..."

create_parent_category() {
    local name=$1
    local var_name=$2
    local response=$(curl -s -X POST "$CATEGORY_URL" \
        -H "Content-Type: application/json" \
        -d "{\"name\": \"$name\", \"parentId\": null}")

    # Extract id from response using sed
    local id=$(echo $response | sed -n 's/.*"data":{"id":\([0-9]*\).*/\1/p')
    eval "$var_name=$id"
    echo "  ✓ $name (ID: $id)"
}

create_parent_category "아이돌 굿즈" "IDOL_ID"
create_parent_category "애니메이션 굿즈" "ANIME_ID"
create_parent_category "게임 굿즈" "GAME_ID"
create_parent_category "스포츠 굿즈" "SPORTS_ID"
create_parent_category "캐릭터 굿즈" "CHARACTER_ID"

# 2. 하위 카테고리 생성
echo ""
echo "2. 하위 카테고리 생성 중..."

create_sub_category() {
    local parent_name=$1
    local parent_id=$2
    local sub_name=$3
    local var_name=$4

    local response=$(curl -s -X POST "$CATEGORY_URL" \
        -H "Content-Type: application/json" \
        -d "{\"name\": \"$sub_name\", \"parentId\": $parent_id}")

    # Extract id from response using sed
    local id=$(echo $response | sed -n 's/.*"data":{"id":\([0-9]*\).*/\1/p')
    eval "$var_name=$id"
    echo "  ✓ $parent_name > $sub_name (ID: $id)"
}

# 아이돌 하위 카테고리
create_sub_category "아이돌 굿즈" "$IDOL_ID" "방탄소년단" "BTS_ID"
create_sub_category "아이돌 굿즈" "$IDOL_ID" "블랙핑크" "BP_ID"
create_sub_category "아이돌 굿즈" "$IDOL_ID" "뉴진스" "NJ_ID"
create_sub_category "아이돌 굿즈" "$IDOL_ID" "세븐틴" "SVT_ID"
create_sub_category "아이돌 굿즈" "$IDOL_ID" "에스파" "AESPA_ID"

# 애니메이션 하위 카테고리
create_sub_category "애니메이션 굿즈" "$ANIME_ID" "원피스" "OP_ID"
create_sub_category "애니메이션 굿즈" "$ANIME_ID" "귀멸의 칼날" "KNY_ID"
create_sub_category "애니메이션 굿즈" "$ANIME_ID" "주술회전" "JJK_ID"
create_sub_category "애니메이션 굿즈" "$ANIME_ID" "스파이 패밀리" "SPY_ID"

# 게임 하위 카테고리
create_sub_category "게임 굿즈" "$GAME_ID" "리그 오브 레전드" "LOL_ID"
create_sub_category "게임 굿즈" "$GAME_ID" "닌텐도 스위치" "SWITCH_ID"
create_sub_category "게임 굿즈" "$GAME_ID" "플레이스테이션" "PS_ID"
create_sub_category "게임 굿즈" "$GAME_ID" "포켓몬" "POKEMON_ID"

# 스포츠 하위 카테고리
create_sub_category "스포츠 굿즈" "$SPORTS_ID" "축구" "SOCCER_ID"
create_sub_category "스포츠 굿즈" "$SPORTS_ID" "야구" "BASEBALL_ID"
create_sub_category "스포츠 굿즈" "$SPORTS_ID" "농구" "BASKETBALL_ID"

# 캐릭터 하위 카테고리
create_sub_category "캐릭터 굿즈" "$CHARACTER_ID" "포켓몬 캐릭터" "POKEMON_CHAR_ID"
create_sub_category "캐릭터 굿즈" "$CHARACTER_ID" "디즈니" "DISNEY_ID"
create_sub_category "캐릭터 굿즈" "$CHARACTER_ID" "산리오" "SANRIO_ID"
create_sub_category "캐릭터 굿즈" "$CHARACTER_ID" "라인프렌즈" "LINE_ID"

sleep 1

## 3. 상품 생성
#echo ""
#echo "3. 상품 생성 중..."
#
#create_product() {
#    local name=$1
#    local category_id=$2
#    local price=$3
#    local quantity=$4
#    local description=$5
#    local image_url=$6
#    local seller_code=$7
#
#    curl -s -X POST "$PRODUCT_URL" \
#        -H "Content-Type: application/json" \
#        -H "X-User-Code: $seller_code" \
#        -H "X-Role: SELLER" \
#        -d "{
#            \"name\": \"$name\",
#            \"categoryId\": $category_id,
#            \"quantity\": $quantity,
#            \"description\": \"$description\",
#            \"price\": $price,
#            \"images\": [
#                {
#                    \"url\": \"$image_url\",
#                    \"sequence\": 0,
#                    \"isMain\": true
#                }
#            ]
#        }" > /dev/null
#
#    echo "  ✓ $name"
#}
#
## 방탄소년단 굿즈 (5개 - 간략화)
#echo ""
#echo "📦 방탄소년단 굿즈 생성 중..."
#create_product "BTS MAP OF THE SOUL 7 앨범" "$BTS_ID" 25000 3 "BTS 정규 4집 앨범입니다. 포토카드 포함, 미개봉 새상품." "https://picsum.photos/400/400?random=1" "$USER_CODE_1"
#create_product "BTS 지민 포토카드 세트" "$BTS_ID" 15000 5 "지민 공식 포토카드 5장 세트. Butter 앨범 포카 포함." "https://picsum.photos/400/400?random=2" "$USER_CODE_1"
#create_product "BTS 공식 응원봉 ARMY BOMB" "$BTS_ID" 45000 2 "BTS 3세대 공식 응원봉. 블루투스 연동 가능." "https://picsum.photos/400/400?random=3" "$USER_CODE_1"
#create_product "BTS 윈터 패키지 2023" "$BTS_ID" 55000 1 "2023 시즌그리팅 윈터 패키지. 미개봉 풀박스." "https://picsum.photos/400/400?random=4" "$USER_CODE_1"
#create_product "BTS 정국 포토카드" "$BTS_ID" 8000 10 "정국 공식 포토카드 단품. BE 앨범 버전. 상태 A급." "https://picsum.photos/400/400?random=5" "$USER_CODE_1"
#
## 블랙핑크 굿즈 (5개)
#echo "📦 블랙핑크 굿즈 생성 중..."
#create_product "블랙핑크 BORN PINK 앨범" "$BP_ID" 22000 4 "블랙핑크 2집 정규앨범. 포토카드 랜덤 포함. 미개봉 새상품." "https://picsum.photos/400/400?random=6" "$USER_CODE_1"
#create_product "블랙핑크 제니 포토카드" "$BP_ID" 12000 6 "제니 공식 포토카드. Pink Venom 버전. 상태 S급." "https://picsum.photos/400/400?random=7" "$USER_CODE_1"
#create_product "블랙핑크 공식 응원봉" "$BP_ID" 50000 3 "블랙핑크 공식 응원봉 1세대. 정품 인증 완료." "https://picsum.photos/400/400?random=8" "$USER_CODE_1"
#create_product "블랙핑크 로제 포토북" "$BP_ID" 35000 2 "로제 솔로 앨범 포토북. -R- 앨범 구성품. 미개봉." "https://picsum.photos/400/400?random=9" "$USER_CODE_1"
#create_product "블랙핑크 지수 포토카드 세트" "$BP_ID" 18000 5 "지수 포토카드 5종 세트. 다양한 앨범 버전 포함." "https://picsum.photos/400/400?random=10" "$USER_CODE_1"
#
## 뉴진스 굿즈 (5개)
#echo "📦 뉴진스 굿즈 생성 중..."
#create_product "뉴진스 Get Up 앨범" "$NJ_ID" 20000 5 "뉴진스 1집 앨범 Get Up. 버니비치백 버전. 포토카드 포함." "https://picsum.photos/400/400?random=11" "$USER_CODE_1"
#create_product "뉴진스 민지 포토카드" "$NJ_ID" 15000 4 "민지 공식 포토카드. OMG 앨범 버전. 상태 S급." "https://picsum.photos/400/400?random=12" "$USER_CODE_1"
#create_product "뉴진스 하니 포토카드 세트" "$NJ_ID" 25000 3 "하니 포토카드 3종 세트. Ditto, OMG, Get Up 각 1장." "https://picsum.photos/400/400?random=13" "$USER_CODE_1"
#create_product "뉴진스 공식 토끼 인형" "$NJ_ID" 35000 2 "뉴진스 공식 캐릭터 토끼 인형. 중형 사이즈. 미사용 새제품." "https://picsum.photos/400/400?random=14" "$USER_CODE_1"
#create_product "뉴진스 해린 포토카드" "$NJ_ID" 12000 6 "해린 공식 포토카드. Get Up 앨범 버전. 상태 A+급." "https://picsum.photos/400/400?random=15" "$USER_CODE_1"
#
## 세븐틴 굿즈 (5개)
#echo "📦 세븐틴 굿즈 생성 중..."
#create_product "세븐틴 FML 앨범" "$SVT_ID" 23000 4 "세븐틴 10집 미니앨범 FML. 포토카드 랜덤 포함. 미개봉 새상품." "https://picsum.photos/400/400?random=16" "$USER_CODE_1"
#create_product "세븐틴 에스쿱스 포토카드" "$SVT_ID" 10000 6 "에스쿱스 공식 포토카드. FML 앨범 버전. 상태 S급." "https://picsum.photos/400/400?random=17" "$USER_CODE_1"
#create_product "세븐틴 공식 응원봉" "$SVT_ID" 48000 2 "세븐틴 캐럿봉 2세대. 블루투스 연동 가능. 사용감 적음." "https://picsum.photos/400/400?random=18" "$USER_CODE_1"
#create_product "세븐틴 정한 포토카드 세트" "$SVT_ID" 18000 5 "정한 포토카드 5종 세트. 다양한 앨범 버전. 슬리브 보관." "https://picsum.photos/400/400?random=19" "$USER_CODE_1"
#create_product "세븐틴 도겸 포토카드" "$SVT_ID" 11000 7 "도겸 공식 포토카드. Face the Sun 앨범. 상태 A+급." "https://picsum.photos/400/400?random=20" "$USER_CODE_1"
#
## 에스파 굿즈 (5개)
#echo "📦 에스파 굿즈 생성 중..."
#create_product "에스파 MY WORLD 앨범" "$AESPA_ID" 21000 5 "에스파 3집 미니앨범. 포토카드 랜덤 포함. 미개봉 새상품." "https://picsum.photos/400/400?random=21" "$USER_CODE_1"
#create_product "에스파 카리나 포토카드" "$AESPA_ID" 14000 4 "카리나 공식 포토카드. Spicy 버전. 상태 S급. 기스 없음." "https://picsum.photos/400/400?random=22" "$USER_CODE_1"
#create_product "에스파 윈터 포토카드 세트" "$AESPA_ID" 22000 3 "윈터 포토카드 4종 세트. Girls, Spicy 버전 포함. 슬리브 보관." "https://picsum.photos/400/400?random=23" "$USER_CODE_1"
#create_product "에스파 공식 키링" "$AESPA_ID" 9000 8 "에스파 멤버별 키링 세트. 4종. 미사용 새제품. 메탈 재질." "https://picsum.photos/400/400?random=24" "$USER_CODE_1"
#create_product "에스파 닝닝 포토카드" "$AESPA_ID" 12000 6 "닝닝 공식 포토카드. MY WORLD 앨범. 상태 A+급." "https://picsum.photos/400/400?random=25" "$USER_CODE_1"
#
## 원피스 굿즈 (5개)
#echo "📦 원피스 굿즈 생성 중..."
#create_product "원피스 루피 피규어" "$OP_ID" 45000 3 "원피스 기어5 루피 피규어. 고퀄리티 정품. 높이 20cm." "https://picsum.photos/400/400?random=26" "$USER_CODE_1"
#create_product "원피스 조로 피규어" "$OP_ID" 42000 4 "로로노아 조로 피규어. 3도류 포즈. 정품 확인." "https://picsum.photos/400/400?random=27" "$USER_CODE_1"
#create_product "원피스 극장판 블루레이" "$OP_ID" 35000 2 "원피스 FILM RED 블루레이. 초회 한정판. 미개봉 새상품." "https://picsum.photos/400/400?random=28" "$USER_CODE_1"
#create_product "원피스 포스터 세트" "$OP_ID" 15000 6 "원피스 공식 포스터 5종 세트. 접힘 없음. 각 60x40cm." "https://picsum.photos/400/400?random=29" "$USER_CODE_1"
#create_product "원피스 나미 피규어" "$OP_ID" 38000 5 "나미 피규어. 원피스 스탬피드 버전. 정품. 디테일 우수." "https://picsum.photos/400/400?random=30" "$USER_CODE_1"
#
## 귀멸의 칼날 굿즈 (5개)
#echo "📦 귀멸의 칼날 굿즈 생성 중..."
#create_product "귀멸의 칼날 탄지로 피규어" "$KNY_ID" 40000 4 "카마도 탄지로 피규어. 히노카미 카구라 버전. 이펙트 파츠 포함." "https://picsum.photos/400/400?random=31" "$USER_CODE_1"
#create_product "귀멸의 칼날 네즈코 피규어" "$KNY_ID" 38000 5 "카마도 네즈코 피규어. 혈귀술 이펙트 포함. 정품 박스 완벽." "https://picsum.photos/400/400?random=32" "$USER_CODE_1"
#create_product "귀멸의 칼날 블루레이 BOX" "$KNY_ID" 120000 1 "귀멸의 칼날 TV 애니메이션 블루레이 완전생산한정판. 전권 세트." "https://picsum.photos/400/400?random=33" "$USER_CODE_1"
#create_product "귀멸의 칼날 렌고쿠 피규어" "$KNY_ID" 45000 3 "렌고쿠 쿄쥬로 피규어. 극장판 버전. 불꽃 이펙트 파츠 포함." "https://picsum.photos/400/400?random=34" "$USER_CODE_1"
#create_product "귀멸의 칼날 아크릴 스탠드" "$KNY_ID" 15000 7 "귀멸의 칼날 주 9명 아크릴 스탠드 세트. 미사용 새제품." "https://picsum.photos/400/400?random=35" "$USER_CODE_1"
#
## 주술회전 굿즈 (5개)
#echo "📦 주술회전 굿즈 생성 중..."
#create_product "주술회전 유지 피규어" "$JJK_ID" 42000 4 "이타도리 유지 피규어. 흑섬광 포즈. 이펙트 파츠 포함." "https://picsum.photos/400/400?random=36" "$USER_CODE_1"
#create_product "주술회전 고죠 사토루 피규어" "$JJK_ID" 55000 2 "고죠 사토루 피규어. 무하한 포즈. 프리미엄 퀄리티. 한정판." "https://picsum.photos/400/400?random=37" "$USER_CODE_1"
#create_product "주술회전 0 블루레이" "$JJK_ID" 40000 3 "극장판 주술회전 0 블루레이. 초회 한정판. 특전 포함. 미개봉." "https://picsum.photos/400/400?random=38" "$USER_CODE_1"
#create_product "주술회전 메구미 피규어" "$JJK_ID" 40000 5 "후시구로 메구미 피규어. 십종영법 포즈. 그림자 이펙트 포함." "https://picsum.photos/400/400?random=39" "$USER_CODE_1"
#create_product "주술회전 아크릴 스탠드" "$JJK_ID" 13000 7 "주술회전 메인 캐릭터 아크릴 스탠드 6종 세트. 미사용 새제품." "https://picsum.photos/400/400?random=40" "$USER_CODE_1"
#
## 스파이 패밀리 굿즈 (5개)
#echo "📦 스파이 패밀리 굿즈 생성 중..."
#create_product "스파이 패밀리 아냐 인형" "$SPY_ID" 30000 5 "아냐 포저 공식 인형. 중형 사이즈 30cm. 미사용 새제품. 택 부착." "https://picsum.photos/400/400?random=41" "$USER_CODE_1"
#create_product "스파이 패밀리 로이드 피규어" "$SPY_ID" 35000 4 "로이드 포저 피규어. 스파이 코스튬. 정품. 박스 완벽." "https://picsum.photos/400/400?random=42" "$USER_CODE_1"
#create_product "스파이 패밀리 블루레이" "$SPY_ID" 45000 3 "스파이 패밀리 TV 애니메이션 블루레이 1기. 미개봉 새상품." "https://picsum.photos/400/400?random=43" "$USER_CODE_1"
#create_product "스파이 패밀리 아크릴 스탠드" "$SPY_ID" 12000 8 "스파이 패밀리 가족 아크릴 스탠드 4종 세트. 미사용 새제품." "https://picsum.photos/400/400?random=44" "$USER_CODE_1"
#create_product "스파이 패밀리 요르 피규어" "$SPY_ID" 38000 4 "요르 포저 피규어. 암살자 코스튬. 무기 파츠 포함." "https://picsum.photos/400/400?random=45" "$USER_CODE_1"
#
## LOL 굿즈 (5개)
#echo "📦 리그 오브 레전드 굿즈 생성 중..."
#create_product "LOL 아리 피규어" "$LOL_ID" 45000 3 "리그오브레전드 아리 챔피언 피규어. 정품. 높이 25cm. 디테일 우수." "https://picsum.photos/400/400?random=46" "$USER_CODE_1"
#create_product "LOL 징크스 피규어" "$LOL_ID" 48000 2 "징크스 챔피언 피규어. 무기 파츠 포함. 프리미엄 퀄리티. 한정판." "https://picsum.photos/400/400?random=47" "$USER_CODE_1"
#create_product "LOL 야스오 피규어" "$LOL_ID" 50000 3 "야스오 챔피언 피규어. 검 이펙트 포함. 정품 확인. 박스 완벽." "https://picsum.photos/400/400?random=48" "$USER_CODE_1"
#create_product "LOL 공식 아크릴 스탠드" "$LOL_ID" 15000 7 "리그오브레전드 인기 챔피언 아크릴 스탠드 10종. 미사용 새제품." "https://picsum.photos/400/400?random=49" "$USER_CODE_1"
#create_product "LOL 키링 세트" "$LOL_ID" 12000 8 "LOL 챔피언 키링 12종 세트. 미사용. 메탈 재질. 정품." "https://picsum.photos/400/400?random=50" "$USER_CODE_1"
#
## 닌텐도 스위치 굿즈 (5개)
#echo "📦 닌텐도 스위치 굿즈 생성 중..."
#create_product "닌텐도 스위치 OLED" "$SWITCH_ID" 380000 2 "닌텐도 스위치 OLED 모델. 미개봉 새상품. 정품. 화이트 컬러. 1년 보증." "https://picsum.photos/400/400?random=51" "$USER_CODE_1"
#create_product "젤다의 전설 티어스 오브 더 킹덤" "$SWITCH_ID" 65000 5 "닌텐도 스위치 게임 패키지. 미개봉 새상품. 한글판. 정품 인증." "https://picsum.photos/400/400?random=52" "$USER_CODE_1"
#create_product "마리오 카트 8 디럭스" "$SWITCH_ID" 58000 4 "닌텐도 스위치 게임. 미개봉. 한글 지원. 멀티플레이 가능." "https://picsum.photos/400/400?random=53" "$USER_CODE_1"
#create_product "스플래툰3" "$SWITCH_ID" 59000 3 "스플래툰3 게임 패키지. 미개봉 새상품. 한글판 정품." "https://picsum.photos/400/400?random=54" "$USER_CODE_1"
#create_product "닌텐도 프로 컨트롤러" "$SWITCH_ID" 78000 6 "닌텐도 스위치 프로 컨트롤러. 정품. 미사용 새제품. 블랙." "https://picsum.photos/400/400?random=55" "$USER_CODE_1"
#
## PS5 굿즈 (5개)
#echo "📦 플레이스테이션 굿즈 생성 중..."
#create_product "플레이스테이션5" "$PS_ID" 650000 1 "PS5 디스크 에디션. 미개봉 새상품. 정품. 1년 보증. 화이트 컬러." "https://picsum.photos/400/400?random=56" "$USER_CODE_1"
#create_product "PS5 듀얼센스 컨트롤러" "$PS_ID" 78000 5 "PS5 듀얼센스 무선 컨트롤러. 미사용 새제품. 화이트. 정품." "https://picsum.photos/400/400?random=57" "$USER_CODE_1"
#create_product "갓 오브 워 라그나로크" "$PS_ID" 69000 4 "PS5 게임 디스크. 미개봉 새상품. 한글 더빙. 정품." "https://picsum.photos/400/400?random=58" "$USER_CODE_1"
#create_product "스파이더맨2" "$PS_ID" 72000 3 "PS5 스파이더맨2 게임. 미개봉. 한글 자막. 정품 인증." "https://picsum.photos/400/400?random=59" "$USER_CODE_1"
#create_product "호라이즌 포비든 웨스트" "$PS_ID" 65000 5 "PS5 게임 디스크. 미개봉 새상품. 한글 더빙 지원. 정품." "https://picsum.photos/400/400?random=60" "$USER_CODE_1"
#
## 포켓몬 굿즈 (5개)
#echo "📦 포켓몬 굿즈 생성 중..."
#create_product "포켓몬 피카츄 인형" "$POKEMON_ID" 35000 5 "포켓몬 공식 피카츄 인형. 대형 40cm. 미사용 새제품. 택 부착." "https://picsum.photos/400/400?random=61" "$USER_CODE_1"
#create_product "포켓몬 이브이 인형" "$POKEMON_ID" 32000 6 "포켓몬 공식 이브이 인형. 중형 30cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=62" "$USER_CODE_1"
#create_product "포켓몬 카드 부스터 박스" "$POKEMON_ID" 120000 2 "포켓몬 카드 게임 최신탄 부스터박스. 미개봉. 30팩 구성. 한글판." "https://picsum.photos/400/400?random=63" "$USER_CODE_1"
#create_product "포켓몬 몬코레 피규어" "$POKEMON_ID" 8000 15 "포켓몬 몬스터 콜렉션 피규어 단품. 랜덤. 정품. 미개봉." "https://picsum.photos/400/400?random=64" "$USER_CODE_1"
#create_product "포켓몬 꼬부기 인형" "$POKEMON_ID" 30000 4 "포켓몬 공식 꼬부기 인형. 중형 28cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=65" "$USER_CODE_1"
#
## 축구 굿즈 (5개)
#echo "📦 축구 굿즈 생성 중..."
#create_product "손흥민 토트넘 유니폼" "$SOCCER_ID" 95000 3 "토트넘 23/24 홈 유니폼 손흥민 7번. 정품. 사이즈 L. 미착용 새상품. 택 부착." "https://picsum.photos/400/400?random=66" "$USER_CODE_1"
#create_product "맨유 공식 유니폼" "$SOCCER_ID" 89000 4 "맨체스터 유나이티드 홈 유니폼. 23/24 시즌. 정품. 사이즈 M. 미착용." "https://picsum.photos/400/400?random=67" "$USER_CODE_1"
#create_product "바르셀로나 유니폼" "$SOCCER_ID" 92000 3 "FC 바르셀로나 홈 유니폼. 23/24 시즌. 정품. 사이즈 L. 미착용 새상품." "https://picsum.photos/400/400?random=68" "$USER_CODE_1"
#create_product "리버풀 공식 머플러" "$SOCCER_ID" 35000 6 "리버풀 FC 공식 머플러. 미사용 새제품. 정품. YNWA 로고." "https://picsum.photos/400/400?random=69" "$USER_CODE_1"
#create_product "나이키 축구공 프리미어리그" "$SOCCER_ID" 58000 5 "나이키 프리미어리그 공인구. 5호. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=70" "$USER_CODE_1"
#
## 야구 굿즈 (5개)
#echo "📦 야구 굿즈 생성 중..."
#create_product "LG 트윈스 유니폼" "$BASEBALL_ID" 89000 4 "LG 트윈스 2024 홈 유니폼. 정품. 사이즈 100. 미착용 새상품. 이름 각인 가능." "https://picsum.photos/400/400?random=71" "$USER_CODE_1"
#create_product "두산 베어스 응원봉" "$BASEBALL_ID" 18000 10 "두산 베어스 공식 응원봉. 미사용 새제품. LED 작동. 정품." "https://picsum.photos/400/400?random=72" "$USER_CODE_1"
#create_product "SSG 랜더스 모자" "$BASEBALL_ID" 25000 8 "SSG 랜더스 공식 볼캡. 미사용 새제품. 프리사이즈. 정품." "https://picsum.photos/400/400?random=73" "$USER_CODE_1"
#create_product "키움 히어로즈 유니폼" "$BASEBALL_ID" 85000 3 "키움 히어로즈 2024 홈 유니폼. 정품. 사이즈 95. 미착용 새상품." "https://picsum.photos/400/400?random=74" "$USER_CODE_1"
#create_product "롯데 자이언츠 점퍼" "$BASEBALL_ID" 95000 3 "롯데 자이언츠 공식 점퍼. 사이즈 L. 미착용 새상품. 정품. 가을 시즌용." "https://picsum.photos/400/400?random=75" "$USER_CODE_1"
#
## 농구 굿즈 (5개)
#echo "📦 농구 굿즈 생성 중..."
#create_product "레이커스 르브론 유니폼" "$BASKETBALL_ID" 98000 3 "LA 레이커스 르브론 제임스 23번 유니폼. 정품. 사이즈 L. 미착용 새상품." "https://picsum.photos/400/400?random=76" "$USER_CODE_1"
#create_product "골든스테이트 커리 유니폼" "$BASKETBALL_ID" 95000 4 "골든스테이트 워리어스 커리 30번 유니폼. 정품. 사이즈 M. 미착용." "https://picsum.photos/400/400?random=77" "$USER_CODE_1"
#create_product "시카고 불스 조던 유니폼" "$BASKETBALL_ID" 110000 2 "시카고 불스 조던 23번 복각 유니폼. 정품. 사이즈 L. 미착용 새상품." "https://picsum.photos/400/400?random=78" "$USER_CODE_1"
#create_product "나이키 농구화 조던1" "$BASKETBALL_ID" 189000 3 "에어 조던1 하이 농구화. 사이즈 270. 미착용 새상품. 정품." "https://picsum.photos/400/400?random=79" "$USER_CODE_1"
#create_product "스팔딩 NBA 농구공" "$BASKETBALL_ID" 65000 5 "스팔딩 NBA 공인구. 7호. 미사용 새제품. 실내외 겸용." "https://picsum.photos/400/400?random=80" "$USER_CODE_1"
#
## 포켓몬 캐릭터 굿즈 (5개)
#echo "📦 포켓몬 캐릭터 굿즈 생성 중..."
#create_product "포켓몬 잠만보 쿠션" "$POKEMON_CHAR_ID" 45000 4 "포켓몬 잠만보 대형 쿠션. 60cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=81" "$USER_CODE_1"
#create_product "포켓몬 푸린 인형" "$POKEMON_CHAR_ID" 28000 6 "포켓몬 공식 푸린 인형. 중형 30cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=82" "$USER_CODE_1"
#create_product "포켓몬 뮤 피규어" "$POKEMON_CHAR_ID" 65000 2 "포켓몬 뮤 프리미엄 피규어. 높이 18cm. 정품. 한정판. 박스 포함." "https://picsum.photos/400/400?random=83" "$USER_CODE_1"
#create_product "포켓몬 에코백" "$POKEMON_CHAR_ID" 18000 10 "포켓몬 공식 캔버스 에코백. 미사용 새제품. 피카츄 프린팅." "https://picsum.photos/400/400?random=84" "$USER_CODE_1"
#create_product "포켓몬 스티커 세트" "$POKEMON_CHAR_ID" 8000 15 "포켓몬 공식 스티커 30종 세트. 미사용 새제품. 다양한 디자인." "https://picsum.photos/400/400?random=85" "$USER_CODE_1"
#
## 디즈니 굿즈 (5개)
#echo "📦 디즈니 굿즈 생성 중..."
#create_product "디즈니 미키마우스 인형" "$DISNEY_ID" 38000 5 "디즈니 공식 미키마우스 인형. 대형 45cm. 미사용 새제품. 택 부착." "https://picsum.photos/400/400?random=86" "$USER_CODE_1"
#create_product "디즈니 미니마우스 인형" "$DISNEY_ID" 38000 5 "디즈니 공식 미니마우스 인형. 대형 45cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=87" "$USER_CODE_1"
#create_product "디즈니 엘사 인형" "$DISNEY_ID" 42000 4 "겨울왕국 엘사 공식 인형. 중형 40cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=88" "$USER_CODE_1"
#create_product "디즈니 스티치 인형" "$DISNEY_ID" 35000 6 "릴로 앤 스티치 공식 인형. 중형 35cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=89" "$USER_CODE_1"
#create_product "디즈니 겨울왕국 레고" "$DISNEY_ID" 85000 2 "레고 디즈니 겨울왕국 엘사 성 세트. 미개봉 새상품. 정품. 700피스." "https://picsum.photos/400/400?random=90" "$USER_CODE_1"
#
## 산리오 굿즈 (5개)
#echo "📦 산리오 굿즈 생성 중..."
#create_product "헬로키티 인형" "$SANRIO_ID" 32000 6 "산리오 헬로키티 공식 인형. 중형 35cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=91" "$USER_CODE_1"
#create_product "시나모롤 인형" "$SANRIO_ID" 35000 5 "산리오 시나모롤 공식 인형. 중형 38cm. 미사용 새제품. 정품. 인기상품." "https://picsum.photos/400/400?random=92" "$USER_CODE_1"
#create_product "마이멜로디 인형" "$SANRIO_ID" 32000 6 "산리오 마이멜로디 공식 인형. 중형 35cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=93" "$USER_CODE_1"
#create_product "쿠로미 인형" "$SANRIO_ID" 33000 5 "산리오 쿠로미 공식 인형. 중형 35cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=94" "$USER_CODE_1"
#create_product "폼폼푸린 인형" "$SANRIO_ID" 30000 4 "산리오 폼폼푸린 공식 인형. 중형 30cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=95" "$USER_CODE_1"
#
## 라인프렌즈 굿즈 (5개)
#echo "📦 라인프렌즈 굿즈 생성 중..."
#create_product "브라운 인형" "$LINE_ID" 38000 5 "라인프렌즈 브라운 공식 인형. 대형 45cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=96" "$USER_CODE_1"
#create_product "코니 인형" "$LINE_ID" 38000 5 "라인프렌즈 코니 공식 인형. 대형 45cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=97" "$USER_CODE_1"
#create_product "샐리 인형" "$LINE_ID" 35000 4 "라인프렌즈 샐리 공식 인형. 중형 40cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=98" "$USER_CODE_1"
#create_product "BT21 쿠키 인형" "$LINE_ID" 32000 6 "BT21 쿠키 공식 인형. 중형 35cm. 미사용 새제품. 정품. 인기상품." "https://picsum.photos/400/400?random=99" "$USER_CODE_1"
#create_product "BT21 타타 인형" "$LINE_ID" 32000 5 "BT21 타타 공식 인형. 중형 35cm. 미사용 새제품. 정품." "https://picsum.photos/400/400?random=100" "$USER_CODE_2"
#
#echo ""
#echo "🎉 굿즈 중고거래 플랫폼 테스트 데이터 생성 완료"
