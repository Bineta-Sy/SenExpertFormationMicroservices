# Déploiement Kubernetes — SenExpertFormation

## Prérequis
- `kubectl` installé et configuré
- Cluster disponible (minikube, Docker Desktop K8s, ou cluster cloud)
- Images Docker buildées localement

---

## 1. Builder les images Docker

```bash
# Depuis la racine de chaque projet (après mvn package)

cd user_service
docker build -t senexpert/user-service:latest .

cd ../formation_service
docker build -t senexpert/formation-service:latest .
```

### Si vous utilisez Minikube
```bash
# Pointer Docker vers le daemon Minikube pour éviter de pousser sur un registry
eval $(minikube docker-env)

# Puis rebuilder les images (même commandes qu'au-dessus)
```

---

## 2. Déployer dans l'ordre

```bash
# 1. Namespace
kubectl apply -f 00-namespace.yaml

# 2. Secrets et ConfigMap
kubectl apply -f 01-secret.yaml
kubectl apply -f 02-configmap.yaml

# 3. Bases de données (attendre qu'elles soient prêtes avant les services)
kubectl apply -f 03-postgres-users.yaml
kubectl apply -f 04-postgres-formations.yaml

# Vérifier que les pods PostgreSQL sont Running
kubectl get pods -n senexpert

# 4. Microservices
kubectl apply -f 05-user-service.yaml
kubectl apply -f 06-formation-service.yaml
```

### Tout déployer en une commande
```bash
kubectl apply -f . -n senexpert
```

---

## 3. Vérifier l'état du cluster

```bash
# Pods
kubectl get pods -n senexpert

# Services
kubectl get services -n senexpert

# Logs d'un service
kubectl logs -f deployment/user-service -n senexpert
kubectl logs -f deployment/formation-service -n senexpert
```

---

## 4. Accéder aux services

### Minikube
```bash
minikube service user-service-svc -n senexpert
minikube service formation-service-svc -n senexpert
```

### NodePort direct
- user-service  : http://<NODE_IP>:30081
- formation-service : http://<NODE_IP>:30082

---

## 5. Tester l'API

### Inscription
```bash
curl -X POST http://<NODE_IP>:30081/api/utilisateurs/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"Diallo","prenom":"Mamadou","email":"m.diallo@test.com","motDePasse":"pass123","role":"AGENT"}'
```

### Login (récupérer le token JWT)
```bash
curl -X POST http://<NODE_IP>:30081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"m.diallo@test.com","motDePasse":"pass123"}'
```

### Consulter les offres (public)
```bash
curl http://<NODE_IP>:30082/api/offres
```

### Soumettre une demande (avec JWT)
```bash
curl -X POST http://<NODE_IP>:30082/api/demandes \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"utilisateurId":1,"offreId":1}'
```

---

## 6. Supprimer le déploiement

```bash
kubectl delete namespace senexpert
```
