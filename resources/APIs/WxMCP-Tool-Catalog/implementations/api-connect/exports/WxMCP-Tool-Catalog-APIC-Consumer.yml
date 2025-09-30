
## A guide for implementing WxMCP-Tool-Catalog API for IBM API Connect

## Table of Contents

- [1. Overview](#1-overview)  
- [2. Step-by-Step instructions](#2-step-by-step-instructions)  
- [3. Properties](#3-properties)  
  - [3.1. Storage Recommendations](#31-storage-recommendations)  
- [4. Dependencies](#4-dependencies)  
- [5. Access Rights and Role Assignment](#5-access-rights-and-role-assignment)  

## 1. Overview

This directory contains IBM API Connect (API Connect) assembly policies for implementing the **MCP-Tool Catalog API**. The API exposes product, API, and OpenAPI document information in an API Connect-compliant way. It is based on the **WxMCP Tool Catalog 1.1 specification** and integrates with IBM API Connect Manager APIs to retrieve metadata.  

The configuration relies on **API Connect assembly policies (Gatewayscript, Invoke)** and external **properties** for consumer authentication.  

The API is tagged with `mcp.ignore` to prevent it from being exposed as an MCP tool through **WxMCPServer**.

---

## 2. Step-by-Step instructions

- Import the API Connect specific OpenAPI 3.0 implementation [WxMCP-Tool-Catalog API](./exports/WxMCP-Tool-Catalog-APIC-Consumer.yml) into IBM API Connect.
- On the API Connect homepage, locate the **Download Toolkit** section and download `credentials.json` under **Step 2: Download credentials**. This file contains the **consumer_toolkit** `base URL`, `client ID`, and `client secret` needed for API access.
- In API Connect, navigate to **Manage**, select the catalog of your choice (or create a new one), then go to **Catalog Settings → Portal**. Log in to the portal to create a new Portal User.
- Now you have all the values for the **Catalog properties**. Define them (see section 3) so they can be substituted during deployment.
- Ensure the included assembly policies (`gatewayscript`, `invoke`, `set-variable`, `operation-switch`) exist exactly as provided in the OpenAPI definition.
- Apply inbound authentication via **X-IBM-Client-Id** (API Key). This is required for tool clients.
- Keep the tag **mcp.ignore** assigned in API settings so that the catalog API is not itself exposed as a tool.
- Add at least one business API to the same **Product** as **WxMCP-Tool-Catalog-API** and publish all those APIs to the same product and catalog.
- Test the operations of **WxMCP-Tool-Catalog-API** inside the API Connect GUI or use external tools like Postman.
- Configure the MCP client for **WxMCP-Tool-Catalog-API** according to the [examples](https://github.com/IBM/WxMCPServer/?tab=readme-ov-file#7-configuration-examples).

---

## 3. Properties  

The following API-level properties must be configured for the policies to function:

| Property                     | Role / Usage                                                      |
|------------------------------|------------------------------------------------------------------|
| `wxmcp_consumer_client_id`   | Client ID from **consumer_toolkit**.                             |
| `wxmcp_consumer_client_secret` | Client Secret from **consumer_toolkit**.                        |
| `wxmcp_consumer_name`        | Local user name as signed up in the consumer-specific **Portal**.|
| `wxmcp_consumer_password`    | Password for the `wxmcp_consumer_name` user.                     |
| `wxmcp_consumer_base_url`    | Base URL as defined in the `endpoint` property of **consumer_toolkit**.|

### 3.1. Storage Recommendations
- Store secrets (`wxmcp_consumer_client_secret`, `wxmcp_consumer_password`) as **Vault-managed properties** or secure property values.
- Store others (`wxmcp_consumer_client_id`, `wxmcp_consumer_name`, `wxmcp_consumer_base_url`) as standard string properties.

---

## 4. Dependencies  

The assembly policies rely on the following IBM API Connect consumer-facing APIs:

| HTTP Method | API Endpoint                                       | Purpose / Action                                                            |
|-------------|--------------------------------------------------|-----------------------------------------------------------------------------|
| POST        | `$(wxmcp_consumer_base_url)/token`                | Obtain access token with password grant using client ID/secret and user credentials. |
| GET         | `$(wxmcp_consumer_base_url)/public-products`      | Retrieve product list for authenticated consumer.                           |
| GET         | `$(wxmcp_consumer_base_url)/public-apis/{apiId}`  | Retrieve API metadata.                                                       |
| GET         | `$(wxmcp_consumer_base_url)/public-apis/{apiId}/document` | Retrieve detailed API documentation, including x-ibm-endpoints.             |
| GET         | `$(wxmcp_consumer_base_url)/public-apis/{apiId}/document` | Download OpenAPI specification document by API ID.                         |

---

## 5. Access Rights and Role Assignment  

To access all referenced APIs, the consumer application must have:

- **API Connect Consumer Organization Credentials:**
  - The application must belong to a consumer org and be subscribed to the MCP-related products.
  - The `client_id` and `client_secret` must be valid for the chosen org and catalog.

- **IBM API Connect Role / Permission:**
  - The consumer username must have sufficient access to invoke `public-products` and `public-apis` endpoints.
  - By default, consumer roles in a catalog (such as **Developer Portal Users**) are sufficient for read operations.

- **Assembly Security:**
  - The policies validate the `X-IBM-Client-Id` header per subscription.
  - Optionally, JWT validation can be enforced via API Connect assemblies if additional consumer authentication is required.

---
