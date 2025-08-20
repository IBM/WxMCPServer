# `/apis/{apiId}` Policy Documentation

This document describes the **Azure API Management (APIM) inbound policy** implemented for the `/apis/{apiId}` endpoint.  
The policy dynamically retrieves detailed metadata about a specific API from Azure Resource Manager and returns a structured JSON response to the client.

---

## 📖 Policy Overview

The `/apis/{apiId}` policy performs these steps:

1. **Extract API ID** – Parses the `{apiId}` segment from the incoming request URL.
2. **Fetch API metadata** – Calls the ARM REST API to get the API's metadata (name, display name, description, version, etc.).
3. **Fetch API specification export** – Retrieves the API’s OpenAPI (Swagger) specification export link in OpenAPI format.
4. **Fetch API tags** – Fetches tags associated with the API.
5. **Assemble response** – Constructs a clean JSON response which includes:
   - API display name
   - API unique ID
   - Description
   - Version
   - Associated tags (display names)
   - Attachments (with OpenAPI spec link)
   - Endpoint URLs constructed for the gateway

---

## 🔗 Admin API Dependencies

The following **Azure Management API calls** are used by the policy:

| Step | Endpoint | Method | Description |
|------|----------|--------|-------------|
| 2 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}?api-version=2024-05-01` | `GET` | Retrieves detailed metadata for a specific API in APIM. |
| 3 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}?export=true&format=openapi-link&api-version=2024-05-01` | `GET` | Retrieves OpenAPI (swagger) export link for the API. |
| 4 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}/tags?api-version=2024-05-01` | `GET` | Retrieves tags associated with the API. |

---

## 🔐 Minimum Required Azure Role Permissions

To enable this policy's ARM API calls, the **Managed Identity or Service Principal** used must have **read access** to APIM resources. The minimal permission roles include:

- **Reader** role on the Azure API Management service **OR**
- **API Management Service Reader Role** scoped to the APIM instance

These roles grant access to these resource operations:
- **Microsoft.ApiManagement/service/apis/read**
- **Microsoft.ApiManagement/service/apis/tags/read**


