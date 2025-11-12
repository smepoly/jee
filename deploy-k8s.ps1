# Script PowerShell pour déployer MyHospital sur Minikube
# Usage: .\deploy-k8s.ps1

Write-Host "=== Déploiement MyHospital sur Kubernetes ===" -ForegroundColor Green

# Vérifier si Minikube est installé
if (!(Get-Command minikube -ErrorAction SilentlyContinue)) {
    Write-Host "Minikube n'est pas installé. Installation..." -ForegroundColor Yellow
    choco install minikube -y
}

# Vérifier si kubectl est installé
if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "kubectl n'est pas installé. Installation..." -ForegroundColor Yellow
    choco install kubernetes-cli -y
}

# Démarrer Minikube
Write-Host "`n1. Démarrage de Minikube..." -ForegroundColor Cyan
minikube start --memory=4096 --cpus=2 --driver=docker

if ($LASTEXITCODE -ne 0) {
    Write-Host "Erreur lors du démarrage de Minikube" -ForegroundColor Red
    exit 1
}

# Créer le namespace
Write-Host "`n2. Création du namespace myhospital..." -ForegroundColor Cyan
kubectl apply -f k8s/namespace.yaml

# Créer les secrets et ConfigMaps
Write-Host "`n3. Création des secrets et ConfigMaps..." -ForegroundColor Cyan
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmaps.yaml

# Déployer Eureka Server
Write-Host "`n4. Déploiement d'Eureka Server..." -ForegroundColor Cyan
kubectl apply -f k8s/eureka-server.yaml

Write-Host "Attente du démarrage d'Eureka (2-3 minutes)..." -ForegroundColor Yellow
kubectl wait --for=condition=ready pod -l app=eureka-server -n myhospital --timeout=300s

if ($LASTEXITCODE -ne 0) {
    Write-Host "Timeout: Eureka Server ne démarre pas" -ForegroundColor Red
    Write-Host "Vérifiez les logs: kubectl logs -n myhospital -l app=eureka-server" -ForegroundColor Yellow
    exit 1
}

# Déployer les services
Write-Host "`n5. Déploiement des microservices..." -ForegroundColor Cyan
kubectl apply -f k8s/patient-service.yaml
kubectl apply -f k8s/doctor-service.yaml
kubectl apply -f k8s/appointment-service.yaml
kubectl apply -f k8s/api-gateway.yaml

# Attendre que tous les pods soient prêts
Write-Host "`n6. Attente du démarrage de tous les services..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Afficher le statut
Write-Host "`n7. Statut des déploiements:" -ForegroundColor Cyan
kubectl get all -n myhospital

Write-Host "`n=== Déploiement terminé! ===" -ForegroundColor Green
Write-Host "`nPour accéder aux services:" -ForegroundColor Yellow
Write-Host "1. API Gateway: kubectl port-forward -n myhospital svc/api-gateway 8888:8888"
Write-Host "2. Eureka: kubectl port-forward -n myhospital svc/eureka-server 8761:8761"
Write-Host "`nOu utilisez: minikube tunnel (dans un autre terminal)"

Write-Host "`nPour voir les logs:" -ForegroundColor Yellow
Write-Host "kubectl logs -f -n myhospital deployment/api-gateway"

Write-Host "`nPour supprimer le déploiement:" -ForegroundColor Yellow
Write-Host "kubectl delete namespace myhospital"
Write-Host "minikube stop"
