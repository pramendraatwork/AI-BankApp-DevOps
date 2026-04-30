# 🏦 AI-BankApp-DevOps

> A production-grade **GitOps CI/CD Pipeline** for a Java Spring Boot Banking Application, deployed on Kubernetes using GitHub Actions, ArgoCD, and Kind.

![GitOps](https://img.shields.io/badge/GitOps-ArgoCD-orange?style=for-the-badge&logo=argo)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-blue?style=for-the-badge&logo=githubactions)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Kind-326CE5?style=for-the-badge&logo=kubernetes)
![Docker](https://img.shields.io/badge/Docker-DockerHub-2496ED?style=for-the-badge&logo=docker)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)

---

## 📋 Table of Contents

- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [GitOps Pipeline Flow](#-gitops-pipeline-flow)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [API Reference](#-api-reference)
- [Kubernetes Resources](#-kubernetes-resources)
- [CI/CD Pipeline](#-cicd-pipeline)
- [ArgoCD Setup](#-argocd-setup)

---

## 🎯 Project Overview

**AI-BankApp-DevOps** is a complete end-to-end GitOps implementation that demonstrates modern DevOps practices. It features:

- A **Java Spring Boot** REST API for banking operations (accounts management)
- A **MySQL** database with persistent storage on Kubernetes
- A **GitHub Actions** CI pipeline that automatically builds, tests, and pushes Docker images
- **ArgoCD** as the GitOps controller that watches the Git repository and automatically syncs changes to the Kubernetes cluster
- **Kind** (Kubernetes in Docker) as the local Kubernetes cluster

The key GitOps principle: **Git is the single source of truth**. Any change to the application or infrastructure goes through Git, and ArgoCD ensures the cluster always matches the desired state in the repository.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                        DEVELOPER MACHINE                            │
│                                                                     │
│   ┌──────────┐     git push      ┌─────────────────────────────┐   │
│   │          │ ─────────────────▶│         GitHub Repo          │   │
│   │Developer │                   │   AI-BankApp-DevOps          │   │
│   │          │                   │                              │   │
│   └──────────┘                   │  ├── src/          (Java)    │   │
│                                  │  ├── k8s/          (K8s)     │   │
│                                  │  ├── Dockerfile              │   │
│                                  │  └── .github/workflows/      │   │
│                                  └──────────────┬───────────────┘   │
└─────────────────────────────────────────────────│───────────────────┘
                                                  │
                                    triggers CI   │
                                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      GITHUB ACTIONS (CI)                            │
│                                                                     │
│   1. ✅ Checkout Code                                               │
│   2. ✅ Set up JDK 17                                               │
│   3. ✅ Build with Maven                                            │
│   4. ✅ Run Tests                                                   │
│   5. ✅ Build Docker Image                                          │
│   6. ✅ Push to DockerHub  ──────────────────▶  DockerHub           │
│   7. ✅ Update k8s/deployment.yaml image tag                        │
│   8. ✅ git commit [skip ci] & push                                 │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
                          │ updated k8s manifest
                          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        ARGOCD (CD)                                  │
│                                                                     │
│   ┌─────────────────────────────────────────┐                      │
│   │  Polls GitHub every 3 minutes           │                      │
│   │  Detects new commit in k8s/             │                      │
│   │  Compares desired vs live state         │                      │
│   │  Auto-syncs manifests to cluster        │                      │
│   │  Rolling update with 0 downtime         │                      │
│   └─────────────────────────────────────────┘                      │
│                          │ sync & deploy                            │
└──────────────────────────│──────────────────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    KIND KUBERNETES CLUSTER                          │
│                                                                     │
│   Namespace: default                                                │
│   ┌─────────────────────────┐   ┌──────────────────────────────┐   │
│   │   BankApp Deployment     │   │      MySQL Deployment         │   │
│   │   replicas: 2            │   │      replicas: 1              │   │
│   │                          │   │                               │   │
│   │  ┌────────────────────┐  │   │  ┌─────────────────────────┐ │   │
│   │  │ bankapp pod 1      │  │   │  │ mysql pod               │ │   │
│   │  │ Spring Boot :8080  │  │   │  │ MySQL 8.0 :3306         │ │   │
│   │  └────────────────────┘  │   │  └─────────────────────────┘ │   │
│   │  ┌────────────────────┐  │   │            │                  │   │
│   │  │ bankapp pod 2      │  │   │  ┌─────────────────────────┐ │   │
│   │  │ Spring Boot :8080  │  │   │  │ PersistentVolumeClaim   │ │   │
│   │  └────────────────────┘  │   │  │ 1Gi Storage             │ │   │
│   └──────────────────────────┘   │  └─────────────────────────┘ │   │
│              │                   └──────────────────────────────┘   │
│   ┌──────────▼──────────────┐                                       │
│   │  bankapp-service         │                                       │
│   │  NodePort: 30080         │◀─────────── http://localhost:30080   │
│   └──────────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 GitOps Pipeline Flow

```
┌──────────┐     ┌──────────────┐     ┌───────────┐     ┌──────────┐     ┌─────────┐
│          │     │    GitHub     │     │  GitHub   │     │DockerHub │     │  Kind   │
│Developer │     │    Repo       │     │  Actions  │     │Registry  │     │Cluster  │
│          │     │               │     │           │     │          │     │         │
└────┬─────┘     └──────┬────────┘     └─────┬─────┘     └────┬─────┘     └────┬────┘
     │                  │                    │                 │                │
     │  git push        │                    │                 │                │
     │─────────────────▶│                    │                 │                │
     │                  │                    │                 │                │
     │                  │  trigger CI        │                 │                │
     │                  │───────────────────▶│                 │                │
     │                  │                    │                 │                │
     │                  │                    │ mvn build+test  │                │
     │                  │                    │─────────┐       │                │
     │                  │                    │◀────────┘       │                │
     │                  │                    │                 │                │
     │                  │                    │ docker build    │                │
     │                  │                    │─────────┐       │                │
     │                  │                    │◀────────┘       │                │
     │                  │                    │                 │                │
     │                  │                    │ docker push     │                │
     │                  │                    │────────────────▶│                │
     │                  │                    │                 │                │
     │                  │                    │ update k8s tag  │                │
     │                  │                    │─────────┐       │                │
     │                  │                    │◀────────┘       │                │
     │                  │                    │                 │                │
     │                  │  git commit+push   │                 │                │
     │                  │◀───────────────────│                 │                │
     │                  │                    │                 │                │
     │                  │  ArgoCD polls (3m) │                 │                │
     │                  │──────────────────────────────────────────────────────▶│
     │                  │                    │                 │                │
     │                  │                    │                 │  pull image    │
     │                  │                    │                 │◀───────────────│
     │                  │                    │                 │                │
     │                  │                    │                 │  rolling update│
     │                  │                    │                 │───────────────▶│
     │                  │                    │                 │                │
```

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Java | 17 |
| **Framework** | Spring Boot | 3.2.0 |
| **Build Tool** | Maven | 3.8.7 |
| **Database** | MySQL | 8.0 |
| **ORM** | Spring Data JPA / Hibernate | Latest |
| **Containerization** | Docker | 28.x |
| **Container Registry** | DockerHub | - |
| **Kubernetes** | Kind (Kubernetes in Docker) | v0.22.0 |
| **GitOps Controller** | ArgoCD | Stable |
| **CI Pipeline** | GitHub Actions | - |
| **OS** | Ubuntu 24.04 (WSL2) | - |

---

## 📁 Project Structure

```
AI-BankApp-DevOps/
│
├── .github/
│   └── workflows/
│       └── ci.yaml                    # GitHub Actions CI Pipeline
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/bankapp/
│       │       ├── BankAppApplication.java        # Main Spring Boot App
│       │       ├── controller/
│       │       │   └── AccountController.java     # REST API endpoints
│       │       ├── model/
│       │       │   └── Account.java               # Account entity
│       │       └── repository/
│       │           └── AccountRepository.java     # JPA Repository
│       └── resources/
│           └── application.properties             # App configuration
│
├── k8s/                               # Kubernetes Manifests (GitOps source)
│   ├── deployment.yaml                # BankApp Deployment + Service
│   ├── mysql-deployment.yaml          # MySQL Deployment + Service
│   ├── mysql-pvc.yaml                 # MySQL Persistent Volume Claim
│   └── secret.yaml                    # MySQL credentials secret
│
├── Dockerfile                         # Multi-stage Docker build
├── kind-cluster.yaml                  # Kind cluster configuration
└── pom.xml                            # Maven dependencies
```

---

## ✅ Prerequisites

Make sure you have these tools installed:

```bash
java -version        # Java 17+
mvn -version         # Maven 3.8+
docker --version     # Docker 20+
kind version         # Kind v0.22+
kubectl version      # kubectl v1.28+
argocd version       # ArgoCD CLI
git --version        # Git 2.x
```

---

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/pramendraatwork/AI-BankApp-DevOps.git
cd AI-BankApp-DevOps
```

### 2. Create Kind Cluster

```bash
kind create cluster --name bankapp-cluster --config kind-cluster.yaml
kubectl get nodes
```

### 3. Install ArgoCD

```bash
kubectl create namespace argocd
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# Wait for pods
kubectl get pods -n argocd -w
```

### 4. Get ArgoCD Password

```bash
kubectl get secret argocd-initial-admin-secret \
  -n argocd \
  -o jsonpath="{.data.password}" | base64 --decode && echo
```

### 5. Access ArgoCD UI

```bash
# In a separate terminal
kubectl port-forward svc/argocd-server -n argocd 9090:443

# Open browser: https://localhost:9090
# Username: admin | Password: (from step 4)
```

### 6. Build & Push Docker Image

```bash
docker build -t pramendradevops/bankapp:latest .
docker push pramendradevops/bankapp:latest
kind load docker-image pramendradevops/bankapp:latest --name bankapp-cluster
```

### 7. Deploy with ArgoCD

```bash
argocd login localhost:9090 --username admin --password <PASSWORD> --insecure

argocd app create ai-bankapp \
  --repo https://github.com/pramendraatwork/AI-BankApp-DevOps.git \
  --path k8s \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace default \
  --sync-policy automated \
  --auto-prune \
  --self-heal \
  --insecure
```

### 8. Verify Deployment

```bash
kubectl get pods
# NAME                       READY   STATUS    RESTARTS
# bankapp-xxxxxxxxx          1/1     Running   0
# bankapp-xxxxxxxxx          1/1     Running   0
# mysql-xxxxxxxxx            1/1     Running   0
```

### 9. Test the App

```bash
curl http://localhost:30080/api/accounts
# Returns: []

curl -X POST http://localhost:30080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"owner": "John Doe", "balance": 5000.00}'
# Returns: {"id":1,"owner":"John Doe","balance":5000.0}
```

---

## 📡 API Reference

Base URL: `http://localhost:30080`

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `GET` | `/api/accounts` | Get all accounts | - |
| `POST` | `/api/accounts` | Create new account | `{"owner": "string", "balance": number}` |
| `GET` | `/api/accounts/{id}` | Get account by ID | - |
| `DELETE` | `/api/accounts/{id}` | Delete account | - |

### Example Requests

```bash
# Get all accounts
curl http://localhost:30080/api/accounts

# Create account
curl -X POST http://localhost:30080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{"owner": "Pramendra", "balance": 99999.00}'

# Get account by ID
curl http://localhost:30080/api/accounts/1

# Delete account
curl -X DELETE http://localhost:30080/api/accounts/1
```

### Example Response

```json
[
  {
    "id": 1,
    "owner": "John Doe",
    "balance": 5000.0
  },
  {
    "id": 2,
    "owner": "Pramendra",
    "balance": 99999.0
  }
]
```

---

## ☸️ Kubernetes Resources

| Resource | Kind | Description |
|----------|------|-------------|
| `bankapp` | Deployment | BankApp with 2 replicas |
| `bankapp-service` | Service (NodePort:30080) | Exposes app on localhost:30080 |
| `mysql` | Deployment | MySQL 8.0 database |
| `mysql` | Service (ClusterIP:3306) | Internal MySQL access |
| `mysql-pvc` | PersistentVolumeClaim | 1Gi persistent storage |
| `mysql-secret` | Secret | MySQL credentials |

---

## ⚙️ CI/CD Pipeline

The GitHub Actions pipeline (`.github/workflows/ci.yaml`) runs on every push to `main` (except k8s/ changes):

```
Step 1: Checkout code
Step 2: Set up JDK 17
Step 3: Build with Maven (mvn clean package)
Step 4: Run tests (mvn test)
Step 5: Login to DockerHub
Step 6: Build Docker image (tagged with git SHA)
Step 7: Push image to DockerHub
Step 8: Update image tag in k8s/deployment.yaml
Step 9: Commit & push updated manifest [skip ci]
```

### Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| `GIT_PAT` | GitHub Personal Access Token (repo + workflow scope) |
| `DOCKERHUB_USERNAME` | DockerHub username |
| `DOCKERHUB_TOKEN` | DockerHub access token |

---

## 🔁 ArgoCD Setup

ArgoCD watches the `k8s/` directory in this repository and automatically syncs any changes to the Kind cluster.

| Setting | Value |
|---------|-------|
| Repo | `https://github.com/pramendraatwork/AI-BankApp-DevOps.git` |
| Path | `k8s/` |
| Cluster | `https://kubernetes.default.svc` |
| Namespace | `default` |
| Sync Policy | `Automated` |
| Auto-prune | `true` |
| Self-heal | `true` |
| Poll Interval | `3 minutes` |

### ArgoCD App Status Check

```bash
argocd app get ai-bankapp
# Sync Status:   Synced ✅
# Health Status: Healthy 💚
```

---

## 🗄️ Database Configuration

MySQL credentials are stored as a Kubernetes Secret:

| Key | Value (base64) | Decoded |
|-----|---------------|---------|
| `mysql-root-password` | `cGFzc3dvcmQ=` | `password` |
| `mysql-database` | `YmFua2Ri` | `bankdb` |

---

## 👨‍💻 Author

**Pramendra** — [@pramendraatwork](https://github.com/pramendraatwork)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

> Built with ❤️ using GitOps principles — *Git is the single source of truth*