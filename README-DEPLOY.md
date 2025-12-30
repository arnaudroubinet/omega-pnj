# Guide de Déploiement - Omega-PNJ

Ce guide couvre le déploiement de l'application Omega-PNJ avec Docker et Kubernetes.

## Table des matières

1. [Déploiement avec Docker Compose](#déploiement-avec-docker-compose)
2. [Construction de l'image Docker](#construction-de-limage-docker)
3. [Déploiement sur Kubernetes](#déploiement-sur-kubernetes)
4. [Configuration avancée](#configuration-avancée)
5. [Troubleshooting](#troubleshooting)

---

## Déploiement avec Docker Compose

Le moyen le plus simple pour démarrer l'application en local avec toutes ses dépendances.

### Prérequis

- Docker 20.10+
- Docker Compose 2.0+
- (Optionnel) GPU NVIDIA avec drivers et nvidia-docker pour accélération

### Démarrage rapide

```bash
# Démarrer tous les services
docker-compose up -d

# Vérifier les logs
docker-compose logs -f

# Arrêter les services
docker-compose down

# Arrêter et supprimer les volumes (ATTENTION: perte de données)
docker-compose down -v
```

### Services inclus

- **postgres**: Base de données PostgreSQL (port 5432)
- **ollama**: Serveur LLM Ollama (port 11434)
- **ollama-init**: Job pour télécharger le modèle llama2
- **app**: Application Omega-PNJ (port 8080)

### Accès

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui
- Dev UI: http://localhost:8080/q/dev

### Sans GPU

Si vous n'avez pas de GPU, commentez la section `deploy` dans le service `ollama` du [docker-compose.yml](docker-compose.yml):

```yaml
# deploy:
#   resources:
#     reservations:
#       devices:
#         - driver: nvidia
#           count: all
#           capabilities: [gpu]
```

---

## Construction de l'image Docker

### Build local

```bash
# Construction de l'image
docker build -t omega-pnj:latest .

# Test de l'image
docker run -p 8080:8080 omega-pnj:latest
```

### Build et push vers un registry

```bash
# Se connecter au registry
docker login your-registry.com

# Tag de l'image
docker tag omega-pnj:latest your-registry.com/omega-pnj:1.0.0

# Push vers le registry
docker push your-registry.com/omega-pnj:1.0.0
```

### Build multi-architecture (optionnel)

```bash
# Créer un builder multi-architecture
docker buildx create --name multiarch --use

# Build pour AMD64 et ARM64
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t your-registry.com/omega-pnj:1.0.0 \
  --push \
  .
```

---

## Déploiement sur Kubernetes

### Prérequis

- Cluster Kubernetes 1.24+
- kubectl configuré
- Ingress controller (nginx recommandé)
- (Optionnel) cert-manager pour SSL
- (Optionnel) nvidia-device-plugin pour GPU
- Stockage persistant (PVC)

### Étapes de déploiement

#### 1. Créer le namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

#### 2. Configurer les secrets

**IMPORTANT**: Changez les mots de passe avant le déploiement en production!

```bash
# Éditer k8s/configmap.yaml et modifier les secrets
kubectl apply -f k8s/configmap.yaml
```

Ou créer les secrets via ligne de commande:

```bash
kubectl create secret generic omega-pnj-secrets \
  --namespace=omega-pnj \
  --from-literal=DB_PASSWORD='votre-mot-de-passe-fort' \
  --from-literal=POSTGRES_PASSWORD='votre-mot-de-passe-fort'
```

#### 3. Déployer PostgreSQL

```bash
kubectl apply -f k8s/postgres-deployment.yaml

# Vérifier le déploiement
kubectl get pods -n omega-pnj -l app=postgres
kubectl logs -n omega-pnj -l app=postgres
```

#### 4. Déployer Ollama

```bash
kubectl apply -f k8s/ollama-deployment.yaml

# Vérifier le déploiement
kubectl get pods -n omega-pnj -l app=ollama
kubectl logs -n omega-pnj -l app=ollama

# Vérifier le téléchargement du modèle
kubectl get jobs -n omega-pnj
kubectl logs -n omega-pnj job/ollama-model-loader
```

**Note GPU**: Si vous avez des nœuds GPU, décommentez les sections GPU dans [k8s/ollama-deployment.yaml](k8s/ollama-deployment.yaml).

#### 5. Déployer l'application

**IMPORTANT**: Modifiez l'image dans [k8s/app-deployment.yaml](k8s/app-deployment.yaml):

```yaml
image: your-registry/omega-pnj:latest  # Remplacez par votre image
```

Puis déployez:

```bash
kubectl apply -f k8s/app-deployment.yaml

# Vérifier le déploiement
kubectl get pods -n omega-pnj -l app=omega-pnj-app
kubectl logs -n omega-pnj -l app=omega-pnj-app -f

# Vérifier les services
kubectl get svc -n omega-pnj
```

#### 6. Configurer l'Ingress

Modifiez le domaine dans [k8s/ingress.yaml](k8s/ingress.yaml):

```yaml
host: omega-pnj.example.com  # Remplacez par votre domaine
```

Puis déployez:

```bash
kubectl apply -f k8s/ingress.yaml

# Vérifier l'Ingress
kubectl get ingress -n omega-pnj
kubectl describe ingress omega-pnj-ingress -n omega-pnj
```

### Déploiement complet en une commande

```bash
# Appliquer tous les manifests dans l'ordre
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/ollama-deployment.yaml

# Attendre que Ollama soit prêt
kubectl wait --for=condition=ready pod -l app=ollama -n omega-pnj --timeout=300s

# Déployer l'app et l'Ingress
kubectl apply -f k8s/app-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

### Vérification du déploiement

```bash
# Vérifier tous les pods
kubectl get pods -n omega-pnj

# Vérifier les services
kubectl get svc -n omega-pnj

# Vérifier les PVC
kubectl get pvc -n omega-pnj

# Vérifier l'Ingress
kubectl get ingress -n omega-pnj

# Logs de l'application
kubectl logs -n omega-pnj deployment/omega-pnj-app -f

# Accéder à l'application (port-forward pour test)
kubectl port-forward -n omega-pnj svc/omega-pnj-service 8080:80
```

---

## Configuration avancée

### Scaling manuel

```bash
# Scaler l'application
kubectl scale deployment omega-pnj-app -n omega-pnj --replicas=5

# Vérifier
kubectl get pods -n omega-pnj -l app=omega-pnj-app
```

### Auto-scaling (HPA)

L'HorizontalPodAutoscaler est déjà configuré dans [k8s/app-deployment.yaml](k8s/app-deployment.yaml):

```bash
# Vérifier l'HPA
kubectl get hpa -n omega-pnj

# Détails
kubectl describe hpa omega-pnj-hpa -n omega-pnj
```

### SSL/TLS avec cert-manager

1. Installer cert-manager:

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml
```

2. Créer un ClusterIssuer pour Let's Encrypt:

```yaml
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    email: your-email@example.com
    privateKeySecretRef:
      name: letsencrypt-prod
    solvers:
    - http01:
        ingress:
          class: nginx
```

3. Décommenter les annotations TLS dans [k8s/ingress.yaml](k8s/ingress.yaml).

### Monitoring avec Prometheus

```bash
# Ajouter les annotations Prometheus au service
kubectl annotate service omega-pnj-service -n omega-pnj \
  prometheus.io/scrape="true" \
  prometheus.io/port="8080" \
  prometheus.io/path="/q/metrics"
```

### Backup de la base de données

```bash
# Backup manuel
kubectl exec -n omega-pnj deployment/postgres -- \
  pg_dump -U npc_user npc_db > backup.sql

# Restauration
kubectl exec -i -n omega-pnj deployment/postgres -- \
  psql -U npc_user -d npc_db < backup.sql
```

### Variables d'environnement personnalisées

Modifier [k8s/configmap.yaml](k8s/configmap.yaml) et recharger:

```bash
kubectl apply -f k8s/configmap.yaml

# Redémarrer les pods pour appliquer les changements
kubectl rollout restart deployment/omega-pnj-app -n omega-pnj
```

---

## Troubleshooting

### Les pods ne démarrent pas

```bash
# Vérifier le statut
kubectl get pods -n omega-pnj

# Voir les événements
kubectl get events -n omega-pnj --sort-by='.lastTimestamp'

# Logs détaillés
kubectl describe pod <pod-name> -n omega-pnj
```

### Problème de connexion à la base de données

```bash
# Vérifier que PostgreSQL est prêt
kubectl logs -n omega-pnj -l app=postgres

# Tester la connexion depuis l'app
kubectl exec -it -n omega-pnj deployment/omega-pnj-app -- \
  curl postgres-service:5432
```

### Ollama ne répond pas

```bash
# Vérifier les logs
kubectl logs -n omega-pnj -l app=ollama

# Vérifier si le modèle est téléchargé
kubectl logs -n omega-pnj job/ollama-model-loader

# Tester l'API Ollama
kubectl exec -it -n omega-pnj deployment/omega-pnj-app -- \
  curl http://ollama-service:11434/
```

### L'Ingress ne fonctionne pas

```bash
# Vérifier l'Ingress controller
kubectl get pods -n ingress-nginx

# Vérifier les logs de l'Ingress
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# Tester en port-forward
kubectl port-forward -n omega-pnj svc/omega-pnj-service 8080:80
```

### Problème de PVC (volume)

```bash
# Vérifier les PVC
kubectl get pvc -n omega-pnj

# Voir les détails
kubectl describe pvc postgres-pvc -n omega-pnj
kubectl describe pvc ollama-pvc -n omega-pnj

# Si le PVC est en Pending, vérifier les StorageClass disponibles
kubectl get storageclass
```

### Rollback d'un déploiement

```bash
# Voir l'historique
kubectl rollout history deployment/omega-pnj-app -n omega-pnj

# Rollback à la version précédente
kubectl rollout undo deployment/omega-pnj-app -n omega-pnj

# Rollback à une version spécifique
kubectl rollout undo deployment/omega-pnj-app -n omega-pnj --to-revision=2
```

### Redémarrer complètement

```bash
# Supprimer tous les déploiements
kubectl delete -f k8s/app-deployment.yaml
kubectl delete -f k8s/ollama-deployment.yaml
kubectl delete -f k8s/postgres-deployment.yaml

# Optionnel: Supprimer les PVC (PERTE DE DONNÉES!)
kubectl delete pvc --all -n omega-pnj

# Redéployer
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/ollama-deployment.yaml
kubectl apply -f k8s/app-deployment.yaml
```

---

## Commandes utiles

```bash
# Voir tous les ressources du namespace
kubectl get all -n omega-pnj

# Shell dans un pod
kubectl exec -it -n omega-pnj deployment/omega-pnj-app -- /bin/sh

# Copier un fichier vers/depuis un pod
kubectl cp backup.sql omega-pnj/postgres-xxx:/tmp/backup.sql

# Surveiller les ressources
kubectl top pods -n omega-pnj
kubectl top nodes

# Voir la configuration appliquée
kubectl get configmap omega-pnj-config -n omega-pnj -o yaml
kubectl get secret omega-pnj-secrets -n omega-pnj -o yaml
```

---

## Production Checklist

Avant de déployer en production:

- [ ] Changer tous les mots de passe dans les secrets
- [ ] Configurer le SSL/TLS avec un certificat valide
- [ ] Configurer les backups automatiques de PostgreSQL
- [ ] Activer le monitoring (Prometheus/Grafana)
- [ ] Configurer les alertes
- [ ] Définir les resource limits appropriées
- [ ] Tester le scaling et les failovers
- [ ] Configurer les PVC avec un StorageClass de qualité production
- [ ] Mettre en place une stratégie de CI/CD
- [ ] Documenter les procédures de disaster recovery
- [ ] Configurer les logs centralisés (ELK/Loki)
- [ ] Implémenter un WAF si exposé publiquement
- [ ] Vérifier la compliance RGPD si applicable

---

## Support

Pour toute question ou problème:
- Vérifier les logs: `kubectl logs -n omega-pnj <pod-name>`
- Consulter la documentation Quarkus: https://quarkus.io
- Consulter la documentation Ollama: https://ollama.ai
