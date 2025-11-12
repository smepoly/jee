# Script to update Helm chart and redeploy with fixed probe timings

Write-Host "=== Fixing Kubernetes Health Probes ===" -ForegroundColor Cyan

# Upgrade the Helm release
Write-Host "`n1. Upgrading Helm release with updated probes..." -ForegroundColor Yellow
helm upgrade myhospital helm/myhospital

Write-Host "`n2. Waiting for pods to restart..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`n3. Current pod status:" -ForegroundColor Yellow
kubectl get pods -n myhospital

Write-Host "`n4. To watch pods in real-time, run:" -ForegroundColor Green
Write-Host "   kubectl get pods -n myhospital -w" -ForegroundColor White

Write-Host "`n5. To check service status:" -ForegroundColor Green
Write-Host "   kubectl get svc -n myhospital" -ForegroundColor White

Write-Host "`n6. To get API Gateway URL:" -ForegroundColor Green
Write-Host "   minikube service api-gateway -n myhospital --url" -ForegroundColor White
