# Déploiement Kubernetes - MyHospital Microservices

Ce guide explique comment déployer l'application MyHospital sur un cluster Kubernetes local avec Helm et ArgoCD (GitOps).

## Prérequis

- Docker Desktop installé et en cours d'exécution
- kubectl installé
- Helm 3.x installé
- Un cluster Kubernetes local (Minikube, Kind, ou k3s)

## Option 1: Déploiement avec Minikube

### 1. Installer Minikube

```powershell
# Télécharger et installer Minikube
choco install minikube

# OU télécharger depuis: https://minikube.sigs.k8s.io/docs/start/
```

### 2. Démarrer Minikube

```powershell
# Démarrer avec 4GB RAM et 2 CPUs (ajustez selon vos ressources)
minikube start --memory=4096 --cpus=2 --driver=docker

# Activer l'addon ingress (optionnel)
minikube addons enable ingress

# Vérifier le statut
minikube status
kubectl get nodes
```

### 3. Déployer avec kubectl (manifestes bruts)

```powershell
# Créer le namespace
kubectl apply -f k8s/namespace.yaml

# Créer les secrets et ConfigMaps
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/configmaps.yaml

# Déployer Eureka Server (doit être déployé en premier)
kubectl apply -f k8s/eureka-server.yaml

# Attendre que Eureka soit prêt (2-3 minutes)
kubectl wait --for=condition=ready pod -l app=eureka-server -n myhospital --timeout=300s

# Déployer les bases de données et services
kubectl apply -f k8s/patient-service.yaml
kubectl apply -f k8s/doctor-service.yaml
kubectl apply -f k8s/appointment-service.yaml

# Déployer l'API Gateway
kubectl apply -f k8s/api-gateway.yaml

# Vérifier les déploiements
kubectl get all -n myhospital
```

### 4. Accéder aux services

```powershell
# Option A: Port-forward pour accès local
kubectl port-forward -n myhospital svc/api-gateway 8888:8888
kubectl port-forward -n myhospital svc/eureka-server 8761:8761

# Option B: Utiliser Minikube tunnel (LoadBalancer)
minikube tunnel
# Dans un autre terminal, récupérer l'IP externe
kubectl get svc -n myhospital api-gateway
```

Accéder à:
- API Gateway: http://localhost:8888
- Eureka Dashboard: http://localhost:8761

## Option 2: Déploiement avec Helm

### 1. Installer le chart Helm

```powershell
# Depuis la racine du projet
helm install myhospital ./helm/myhospital -n myhospital --create-namespace

# Vérifier le déploiement
helm list -n myhospital
kubectl get all -n myhospital
```

### 2. Mettre à jour les valeurs (optionnel)

Éditez `helm/myhospital/values.yaml` puis:

```powershell
helm upgrade myhospital ./helm/myhospital -n myhospital
```

### 3. Désinstaller

```powershell
helm uninstall myhospital -n myhospital
```

## Option 3: GitOps avec ArgoCD

### 1. Installer ArgoCD

```powershell
# Créer le namespace ArgoCD
kubectl create namespace argocd

# Installer ArgoCD
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Attendre que les pods soient prêts
kubectl wait --for=condition=ready pod --all -n argocd --timeout=300s

# Récupérer le mot de passe admin initial
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# Port-forward pour accéder à l'UI
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Accéder à ArgoCD UI: https://localhost:8080
- Username: `admin`
- Password: (celui récupéré ci-dessus)

### 2. Déployer l'application via ArgoCD

**Important:** Avant de continuer, poussez votre code sur GitHub:

```powershell
git add .
git commit -m "Add Kubernetes and Helm configurations"
git push origin main
```

Puis déployez l'application ArgoCD:

```powershell
# Appliquer le manifest ArgoCD
kubectl apply -f argocd/application.yaml

# Vérifier le statut
kubectl get applications -n argocd
```

Dans l'UI ArgoCD, vous verrez l'application "myhospital" qui va automatiquement:
- Synchroniser avec votre repo Git
- Déployer le chart Helm
- Surveiller les changements et se mettre à jour automatiquement

### 3. Utiliser ArgoCD CLI (optionnel)

```powershell
# Installer ArgoCD CLI
choco install argocd-cli

# Login
argocd login localhost:8080

# Synchroniser manuellement
argocd app sync myhospital

# Voir le statut
argocd app get myhospital
```

## Option 4: Kind (Kubernetes in Docker)

### 1. Installer Kind

```powershell
choco install kind

# OU
# go install sigs.k8s.io/kind@latest
```

### 2. Créer un cluster

```powershell
# Créer un fichier kind-config.yaml
@"
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 8888
    hostPort: 8888
    protocol: TCP
  - containerPort: 8761
    hostPort: 8761
    protocol: TCP
"@ | Out-File -FilePath kind-config.yaml

# Créer le cluster
kind create cluster --name myhospital --config kind-config.yaml

# Vérifier
kubectl cluster-info --context kind-myhospital
```

Puis suivez les étapes de déploiement kubectl ou Helm ci-dessus.

## Commandes utiles

### Surveillance

```powershell
# Voir les logs d'un pod
kubectl logs -f -n myhospital deployment/api-gateway

# Voir tous les pods
kubectl get pods -n myhospital -w

# Décrire un pod
kubectl describe pod -n myhospital <pod-name>

# Voir les événements
kubectl get events -n myhospital --sort-by='.lastTimestamp'
```

### Debug

```powershell
# Se connecter à un pod
kubectl exec -it -n myhospital <pod-name> -- /bin/sh

# Vérifier les variables d'environnement
kubectl exec -n myhospital <pod-name> -- env

# Tester la connectivité
kubectl run -n myhospital curl --image=curlimages/curl -it --rm -- sh
```

### Scaling

```powershell
# Scaler un deployment
kubectl scale deployment -n myhospital api-gateway --replicas=3

# Autoscaling (HPA)
kubectl autoscale deployment -n myhospital api-gateway --cpu-percent=50 --min=2 --max=10
```

### Nettoyage

```powershell
# Supprimer tous les déploiements
kubectl delete namespace myhospital

# Supprimer le cluster Minikube
minikube delete

# Supprimer le cluster Kind
kind delete cluster --name myhospital
```

## Architecture déployée

```
┌─────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                    │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │              Namespace: myhospital               │   │
│  │                                                  │   │
│  │  ┌────────────┐      ┌─────────────────────┐  │   │
│  │  │ API Gateway│◄─────┤  Eureka Server       │  │   │
│  │  │ (LB:8888)  │      │  (ClusterIP:8761)    │  │   │
│  │  └──────┬─────┘      └──────────────────────┘  │   │
│  │         │                      ▲                │   │
│  │         │                      │                │   │
│  │    ┌────┴──────────────────────┴─────┐         │   │
│  │    │                                  │         │   │
│  │    ▼                                  ▼         │   │
│  │  ┌──────────────┐    ┌────────────────────┐   │   │
│  │  │Patient Service│    │ Doctor Service      │   │   │
│  │  │  (2 replicas) │    │  (2 replicas)       │   │   │
│  │  └───────┬───────┘    └─────────┬─────────┘   │   │
│  │          │                      │              │   │
│  │          ▼                      ▼              │   │
│  │  ┌──────────────┐    ┌────────────────────┐   │   │
│  │  │Patient MySQL │    │ Doctor MySQL        │   │   │
│  │  │ StatefulSet  │    │ StatefulSet         │   │   │
│  │  └──────────────┘    └────────────────────┘   │   │
│  │                                                │   │
│  │  ┌──────────────────────┐                     │   │
│  │  │ Appointment Service   │                     │   │
│  │  │    (2 replicas)       │                     │   │
│  │  └──────────┬───────────┘                     │   │
│  │             │                                  │   │
│  │             ▼                                  │   │
│  │  ┌────────────────────────┐                   │   │
│  │  │ Appointment MySQL       │                   │   │
│  │  │   StatefulSet           │                   │   │
│  │  └─────────────────────────┘                   │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

## Troubleshooting

### Les pods ne démarrent pas

```powershell
kubectl describe pod -n myhospital <pod-name>
kubectl logs -n myhospital <pod-name>
```

### Les services ne se trouvent pas

Vérifiez que Eureka est bien démarré et accessible:

```powershell
kubectl port-forward -n myhospital svc/eureka-server 8761:8761
# Ouvrir http://localhost:8761
```

### Problèmes de base de données

```powershell
# Se connecter à MySQL
kubectl exec -it -n myhospital patient-mysql-0 -- mysql -u root -proot

# Vérifier la base de données
SHOW DATABASES;
USE patientdb;
SHOW TABLES;
```

### Images Docker non trouvées

Assurez-vous que vos images sont poussées sur Docker Hub:

```powershell
docker push sabri74155/api-gateway:latest
docker push sabri74155/patient-service:latest
docker push sabri74155/doctor-service:latest
docker push sabri74155/appointment-service:latest
docker push sabri74155/eureka-server:latest
```

## Prochaines étapes

1. ✅ Déploiement sur cluster local
2. ⏳ Configurer Ingress pour accès externe
3. ⏳ Ajouter monitoring (Prometheus + Grafana)
4. ⏳ Ajouter tracing distribué (Jaeger/Zipkin)
5. ⏳ Implémenter CI/CD complet avec Jenkins + ArgoCD
