@echo off
set NAMESPACE=dev

:MENU
echo.
echo ============================================
echo   Choose outer service to apply
echo ============================================
echo 1. gateway
echo 2. authdb
echo 3. orderdb
echo 4. paymentdb
echo 5. productdb
echo 6. redis
echo 7. ALL
echo 8. Exit
set /p choice="Enter your choice: "
if "%choice%"=="1" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/gateway
if "%choice%"=="2" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/authdb
if "%choice%"=="3" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/orderdb
if "%choice%"=="4" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/paymentdb
if "%choice%"=="5" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/productdb
if "%choice%"=="6" kubectl apply -n %NAMESPACE% -R -f ./dev/outer/redis
if "%choice%"=="7" (
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/authdb
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/gateway
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/orderdb
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/paymentdb
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/productdb
    kubectl apply -n %NAMESPACE% -R -f ./dev/outer/redis
)
if "%choice%"=="8" goto END

goto MENU

:END
echo ============================================
echo   DEV re-deploy finished
echo ============================================
