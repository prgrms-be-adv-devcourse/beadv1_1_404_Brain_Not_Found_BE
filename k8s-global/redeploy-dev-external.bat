@echo off
set NAMESPACE=dev

echo ============================================
echo   DEV re-deploy start
echo ============================================

echo [1] remove namespace
kubectl delete namespace %NAMESPACE% --ignore-not-found=true

echo [2] create namespace
kubectl create namespace %NAMESPACE%

:MENU
echo.
echo ============================================
echo   Choose external service to apply
echo ============================================
echo 1. Kafka
echo 2. Elasticsearch
echo 3. Nginx
echo 4. Kibana
echo 5. Zipkin
echo 6. Prometheus
echo 7. Grafana
echo 8. All
echo 9. Exit
set /p choice="Enter your choice: "

if "%choice%"=="1" kubectl apply -n %NAMESPACE% -R -f ./dev/external/kafka
if "%choice%"=="2" kubectl apply -n %NAMESPACE% -R -f ./dev/external/elasticsearch
if "%choice%"=="3" kubectl apply -n %NAMESPACE% -R -f ./dev/external/nginx
if "%choice%"=="4" kubectl apply -n %NAMESPACE% -R -f ./dev/external/kibana
if "%choice%"=="5" kubectl apply -n %NAMESPACE% -R -f ./dev/external/zipkin
if "%choice%"=="6" kubectl apply -n %NAMESPACE% -R -f ./dev/external/prometheus
if "%choice%"=="7" kubectl apply -n %NAMESPACE% -R -f ./dev/external/grafana
if "%choice%"=="8" (
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/kafka
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/elasticsearch
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/nginx
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/kibana
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/zipkin
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/prometheus
    kubectl apply -n %NAMESPACE% -R -f ./dev/external/grafana

)
if "%choice%"=="9" goto END

goto MENU

:END
echo ============================================
echo   DEV re-deploy finished
echo ============================================
