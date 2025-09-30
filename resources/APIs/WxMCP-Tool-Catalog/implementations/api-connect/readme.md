
# A guide for implementing WxMCP-Tool-Catalog API for webMethods API Management

## Table of Contents

- [1. Overview](#1-overview)  
  - [1.1 Requirements](#11-requirements)  
- [2. Step-by-Step Instructions](#2-step-by-step-instructions-for-webmethods-api-gateway)  
  - [2.1 Configuring SaaS Environment](#21-configuring-saas-environment)  
  - [2.2 Configuring Self-Hosted Environment](#22-configuring-self-hosted-environment)  
- [3. Dependencies](#3-dependencies-to-admin-apis)  
- [4. Access Rights and Role Assignment](#4-access-rights-and-role-assignment)  
  - [4.1 SaaS Environment](#41-ibm-webmethods-hybrid-integration-saas)  
  - [4.2 Self-Hosted Environment](#42-ibm-webmethods-hybrid-integration-self-hosted)  

---

## 1. Overview

This directory contains exported archives for the **MCP-Tool Catalog API** implementation. These archives provide preconfigured APIs and alias definitions to integrate the API with the IBM webMethods API Gateway (in both **SaaS** and **self-hosted** environments).

### 1.1 Requirements
The provided archives require **webMethods 11.1+** installations.

---

## 2. Step-by-Step Instructions for webMethods API Gateway

- Import the **MCP-Tool Catalog API** from this repository: ([API Implementations](./exports/WxMCP-wM-APIGateway-Tool-Catalog.zip)) into the webMethods API Gateway.  
- Import the **Global Policies** from this repository: ([Global Policies](./exports/WxMCP-wM-APIGateway-Global_Policies.zip)) into the webMethods API Gateway.  
- After the import you will find the following APIs in your API Gateway instance:  
  - **WxMCP-Tool-Catalog-wMAPIGW (Version: 1.1)** – The implementation of the MCP Tool Catalog API  
  - **IWHI-Service-Admin-API (Version: 1.0)** – A wrapper API for exchanging an `API Key` and `Instance ID` of an IWHI tenant against a Bearer token for invoking API Gateway admin APIs  

- You will also find the following policies in your API Gateway:  
  - **IWHI-Selfhosted-Credentials-Policy**  
  - **IWHI-Service-Token-Policy** 

- And these **Alias** entries:  
  - **iwhi_service_apikey**
  - **iwhi_service_instance_id**
  - **iwhi_selfhosted_credentials**
  - **iwhi_wm_apigw_host**

- Depending on your environment, configure either for [SaaS](#21-configuring-saas-environment) or [self-hosted](#22-configuring-self-hosted-environment).  

- Finally, configure **iwhi_wm_apigw_host**.
 The API Gateway host name (without protocol http/https, but including the port if available)  
---

### 2.1 Configuring SaaS Environment

- Enable **IWHI-Service-Token-Policy**.  
  This policy injects a **custom extension** policy action into **WxMCP-Tool-Catalog-wMAPIGW (Version: 1.1)** for exchanging an API Key against an OAuth Bearer Token (required to access the admin APIs).
- Configure **Alias** entries
**iwhi_service_apikey** and **iwhi_service_instance_id**.
Define the values for exchanging the API key against a Bearer token, as described here: [IBM Docs](https://www.ibm.com/docs/en/hybrid-integration/saas?topic=apis-managing-administration)   
    
 - Use API tag `iwhi.service.id` to apply this policy to the MCP-Tool Catalog API.

---

### 2.2 Configuring Self-Hosted Environment

- Enable **IWHI-Selfhosted-Credentials-Policy**.  
  This policy injects an **Outbound Auth transport** policy action into **WxMCP-Tool-Catalog-wMAPIGW (Version: 1.1)** for outbound authentication without requiring an IWHI admin token.  
-  Configure **Alias** entries 
  **iwhi_selfhosted_credentials** 
  Outbound credentials for API Gateway admin APIs in your self-hosted environment  
- Use API tag `iwhi.selfhosted` to apply this policy.  

---

## 3. Dependencies to Admin APIs

These policy fragments depend on the following IBM webMethods Admin APIs and endpoints:

| **HTTP Method** | **API Endpoint** | **Purpose/Action** |
|-----------------|------------------------------------------------------------------|--------------------------------------|
| POST            | `https://account-iam.platform.saas.ibm.com/api/2.0/services/{instanceId}/apikeys/token` | Exchange API key for a Bearer token (SaaS only) |
| GET             | `https://${iwhi_wm_apigw_host}/rest/apigateway/packages/{packageId}` | Get details about an API product |
| GET             | `https://${iwhi_wm_apigw_host}/rest/apigateway/apis/{apiId}` | Retrieve API metadata |
| GET             | `https://${iwhi_wm_apigw_host}/rest/apigateway/apis/{apiId}?format=openapi` | Download OpenAPI specification for given API Id |

---

## 4. Access Rights and Role Assignment

### 4.1 IBM webMethods Hybrid Integration (SaaS)
For admin API access, the API Key must be assigned to a **service ID** with the role `API management admin`.

### 4.2 IBM webMethods Hybrid Integration (Self-Hosted)
For admin API access, the identity must be a member of the team **API-Gateway-Providers** (either directly or via group assignment).  

---
