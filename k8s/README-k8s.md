# WxMCPServer MSR Container

This directory contains the Dockerfile and configuration for building a containerized version of WxMCPServer running on webMethods Microservices Runtime (MSR).

## Prerequisites

Ensure you have access to the [webMethods container registry](https://containers.webmethods.io/products) or have the MSR base image available locally.

Building the Image locally

```bash
docker build -t wxmcp-msr:latest .
```

## Configuration

The container expects the following environment variables:

- `APIGW_HOST`: API Gateway hostname
- `APIGW_PORT`: API Gateway port
- `APIGW_USER`: API Gateway username
- `APIGW_PASSWORD`: API Gateway password
- `APIGW_SSL`: SSL enabled flag
- `WXMCP_API_KEY`: API key for WxMCPServer authentication

These are automatically injected from Azure Key Vault when deployed to AKS.

## Testing Locally w/ Kubernetes

-> The scripts expects, that all requirements are installed beforehand.
- docker
- docker image for MSR and local wxmcp-msr
- kubernetes / kubectl
- kubectl-ingress-nginx

```bash
k8s/deploy-local.sh
```
Creates:
- Namespace 'mcp-demo'
- WxMCPServer deployment running on Microservices Runtime v11.1
- Service exposing port 5555
- Ingress exposing the IS UI on the URL: http://mcp.k8s.orb.local/wxmcp
- MCP Server is available on: http://mcp.k8s.orb.local/wxmcp/v1/mcp
- Configmap containing environment variables loaded by the deployment
- Secret containing confidential environment variables loaded by the deployment

## Logs and Debugging
```bash
# Get logs from pods
kubectl logs -n mcp-demo -l app=wxmcp-server
# Get pod details
kubectl describe pods -n mcp-demo -l app=wxmcp-server
# Get service details
kubectl describe service -n mcp-demo wxmcp-server
# Get ingress details
kubectl describe ingress -n mcp-demo mcp-ingress
```

## Cleanup

```bash
# Delete everything
kubectl delete namespace mcp-demo

# Or delete individual resources
kubectl delete -f wxmcp-deployment-local.yaml
kubectl delete -f wxmcp-service.yaml
kubectl delete -f secrets-local.yaml
kubectl delete -f configmap.yaml
kubectl delete -f namespace.yaml
```

## Notes

- The MSR base image size is approximately 1.1GB
- First startup may take up to 60-90 seconds
- Health check endpoint is at `/health`
- Default MSR admin port is 5555
- Diagnostic port is 9999

## WxMCPServer Documentation

For more information about WxMCPServer, see:
- GitHub: https://github.com/IBM/WxMCPServer
- README: Check the [repository README](README.md) for detailed setup and configuration instructions