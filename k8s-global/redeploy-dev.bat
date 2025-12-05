@echo off
set NAMESPACE=dev

:MENU
echo.
echo ============================================
echo   Choose services to apply
echo ============================================
echo 1. auth
echo 2. order
echo 3. payment
echo 4. product
echo 5. ALL
echo 6. Exit
set /p choice="Enter your choice: "
if "%choice%"=="1" kubectl apply -n %NAMESPACE% -R -f ./dev/services/auth
if "%choice%"=="2" kubectl apply -n %NAMESPACE% -R -f ./dev/services/order
if "%choice%"=="3" kubectl apply -n %NAMESPACE% -R -f ./dev/services/payment
if "%choice%"=="4" kubectl apply -n %NAMESPACE% -R -f ./dev/services/product
if "%choice%"=="5" (
    kubectl apply -n %NAMESPACE% -R -f ./dev/services/auth
    kubectl apply -n %NAMESPACE% -R -f ./dev/services/order
    kubectl apply -n %NAMESPACE% -R -f ./dev/services/payment
    kubectl apply -n %NAMESPACE% -R -f ./dev/services/product
)
if "%choice%"=="6" goto END

goto MENU

:END
echo ============================================
echo   DEV re-deploy finished
echo ============================================
