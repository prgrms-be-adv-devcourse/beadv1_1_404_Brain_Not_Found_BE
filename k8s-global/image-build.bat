@echo off
setlocal enabledelayedexpansion

:menu
cls
echo ============================================
echo      Docker Image Build Menu
echo ============================================
echo 1. Build gateway
echo 2. Build auth
echo 3. Build order
echo 4. Build payment
echo 5. Build product1
echo 6. Build all
echo 7. Exit
echo ============================================
set /p choice=Choose an option [1-7]:

REM move to root
cd /d "%~dp0\.."

if "%choice%"=="1" goto build_gateway
if "%choice%"=="2" goto build_auth
if "%choice%"=="3" goto build_order
if "%choice%"=="4" goto build_payment
if "%choice%"=="5" goto build_product
if "%choice%"=="6" goto build_all
if "%choice%"=="7" goto end

echo Invalid choice!
pause
goto menu

:build_gateway
echo Building gateway...
docker build --no-cache --progress=plain -f ./gateway/Dockerfile . -t gateway:latest
pause
goto menu

:build_auth
echo Building auth...
docker build --no-cache --progress=plain -f ./auth/Dockerfile . -t auth:latest
pause
goto menu

:build_order
echo Building order...
docker build --no-cache --progress=plain -f ./order/Dockerfile . -t order:latest
pause
goto menu

:build_payment
echo Building payment...
docker build --no-cache --progress=plain -f ./payment/Dockerfile . -t payment:latest
pause
goto menu

:build_product
echo Building product...
docker build --no-cache --progress=plain -f ./products/Dockerfile . -t product:latest
pause
goto menu

:build_all
echo Building all images...
docker build --no-cache --progress=plain -f ./gateway/Dockerfile . -t gateway:latest
docker build --no-cache --progress=plain -f ./auth/Dockerfile . -t auth:latest
docker build --no-cache --progress=plain -f ./order/Dockerfile . -t order:latest
docker build --no-cache --progress=plain -f ./payment/Dockerfile . -t payment:latest
docker build --no-cache --progress=plain -f ./products/Dockerfile . -t product:latest
pause
goto menu

:end
echo Exiting...
