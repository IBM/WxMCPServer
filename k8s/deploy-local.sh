#!/bin/bash
# Local Kubernetes Deployment Script
# This script deploys the MCP servers to a local Kubernetes cluster (minikube, kind, Docker Desktop, etc.)

set -e

echo "=========================================="
echo "Local Kubernetes Deployment for MCP Demo"
echo "=========================================="
echo ""

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "Error: kubectl is not installed or not in PATH"
    exit 1
fi

# Check if cluster is accessible
if ! kubectl cluster-info &> /dev/null; then
    echo "Error: Cannot connect to Kubernetes cluster"
    echo "Please start your local cluster (minikube, kind, or Docker Desktop)"
    exit 1
fi

echo "✓ Connected to Kubernetes cluster"
echo ""

# Create namespace
echo "Creating namespace..."
kubectl apply -f namespace.yaml

# Wait for namespace to be ready
kubectl wait --for=jsonpath='{.status.phase}'=Active namespace/mcp-demo --timeout=30s
echo "✓ Namespace created"
echo ""

# Create secrets
echo "Creating secrets..."
echo "⚠️  Please edit k8s/secrets-local.yaml with your actual credentials before proceeding!"
read -p "Have you updated secrets-local.yaml with your credentials? (y/n) " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Please update k8s/secrets-local.yaml and run this script again"
    exit 1
fi

kubectl apply -f secrets-local.yaml
echo "✓ Secrets created"
echo ""

# Create ConfigMap
echo "Creating ConfigMap..."
kubectl apply -f configmap.yaml
echo "✓ ConfigMap created"
echo ""

# Deploy WxMCP Server
echo "Deploying WxMCP Server..."
kubectl apply -f wxmcp-deployment-local.yaml
kubectl apply -f wxmcp-service.yaml
echo "✓ WxMCP Server deployed"
echo ""

# Create Ingress (optional)
if [ -f "ingress.yaml" ]; then
    echo "Creating Ingress..."
    kubectl apply -f ingress.yaml
    echo "✓ Ingress created"
    echo ""
fi

echo "=========================================="
echo "Deployment Status"
echo "=========================================="
echo ""

# Wait for deployments to be ready
echo "Waiting for deployments to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/wxmcp-server -n mcp-demo || true
echo ""

# Show deployment status
echo "Deployments:"
kubectl get deployments -n mcp-demo
echo ""

echo "Pods:"
kubectl get pods -n mcp-demo
echo ""

echo "Services:"
kubectl get services -n mcp-demo
echo ""

echo "=========================================="
echo "Access Information"
echo "=========================================="
echo ""

# Check if ingress exists and get hostname
INGRESS_HOST=$(kubectl get ingress mcp-ingress -n mcp-demo -o jsonpath='{.spec.rules[0].host}' 2>/dev/null)

if [ -n "$INGRESS_HOST" ]; then
    echo "Via Ingress (Recommended for OrbStack/Minikube):"
    echo ""
    echo "WxMCP Server:"
    echo "  http://${INGRESS_HOST}/wxmcp/"
    echo ""
    echo "Note: Ensure your ingress controller is running"
    echo "  - OrbStack: Install NGINX ingress controller"
    echo "  - Minikube: Run 'minikube addons enable ingress'"
    echo "  - Docker Desktop/Kind: Install NGINX ingress controller"
    echo ""
fi

echo "Via Port-Forwarding (Alternative):"
echo ""
echo "WxMCP Server:"
echo "  kubectl port-forward -n mcp-demo svc/wxmcp-server 5555:5555"
echo "  Then access: http://localhost:5555"
echo ""
echo "To view logs:"
echo "  kubectl logs -n mcp-demo -l app=wxmcp-server -f"
echo ""
echo "To delete everything:"
echo "  kubectl delete namespace mcp-demo"
echo ""