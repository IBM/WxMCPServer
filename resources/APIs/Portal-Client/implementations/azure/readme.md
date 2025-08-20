# Implementing MCP-Portal API for Azure API Management

## Overview
This directory contains Azure API Management (APIM) policy fragments for the MCP-Portal API implementation. These policies interact with Azure Admin APIs to retrieve API metadata, product information, and more. The configuration relies on several named values for secure and flexible operation.

---
## Step-by-Step instructions

- Import the MCP-Portal API from this GitHub repository ([Specification](../../resources/APIs/Portal-Client/MCP-Portal-Client-1-0.yml)) into Azure API Management (APIM).
- Define a **Managed Identity** or custom **App Registration** with sufficient privileges to access the APIM APIs.
- Define the named values as described below.
- For the **MCP-Portal** API, apply the policy fragment ([general.xml](./policyFragments/general.xml)) to *All Operations -> Inbound Processing -> Policies* to set variables and acquire access tokens.
Adapt the JWT Token validation according to your settins (especially the *audience*). This is optional, you can also acess with API key only.
- Apply policy fragments to respective operations (*Inbound Processing -> Policies*):
    - `getServerDetails` ([productInfo.xml](./policyFragments/productInfo.xml))
    - `getAPIById` ([apiInfo.xml](./policyFragments/apiInfo.xml))
    - `downloadOpenAPISpecificationByID` ([downloadOpenAPISpecification.xml](./policyFragments/downloadOpenAPISpecification.xml))
- Include the **MCP-Portal API** in all API products along with other APIs, and add subscriptions as needed.
- Note: If additional inbound OAuth security policies are defined, the API Key must still be provided to allow metadata access.
- Ensure that **MCP-Portal API** has tag *mcp.ignore* assigned (configured under "Settings" in the API). This ensures that MCP-Portal is not offered as MCP tools to MCP clients
- You can use the tag *mcp.object.name:YOUR_OBJECT* to tell **WxMCPServer** to add a prefix consisting of the object name and "_" before each tool (aka API operation)

---
## Named Values

The following named values must be configured in APIM for these policies to function:

| Named Value           | Role / Usage                                                                                  | Should be Stored as Secret? | Reason                                                                                   |
|-----------------------|----------------------------------------------------------------------------------------------|-----------------------------|------------------------------------------------------------------------------------------|
| `aadClientId`         | Optional: Azure AD App Registration client ID to access admin APIs (only needed if no managed identity is used) using OAuth           | No                          | Public identifier for your app; not sensitive, store as string                           |
| `aadClientSecret`     | Optional: Azure AD App Registration client secret to access admin APIs (only needed if no managed identity is used) using OAuth           | Yes                         | Sensitive credential; must be stored as a secret                                         |
| `aadTenantId`         | Azure AD Tenant ID used to construct token endpoint URLs                                     | No                          | Not sensitive; store as string                                                           |
| `azureSubscriptionId` | Azure Subscription ID used to build Azure Resource Manager (ARM) API URLs                                             | No                          | Not sensitive; store as string                                                           |
| `resourceGroup`       | Name of the Azure Resource Group containing the APIM instance                                | No                          | Not sensitive; store as string                                                           |
| `serviceName`         | Name of the Azure API Management (APIM) service instance                                     | No                          | Not sensitive; store as string                                                           |

### Storage Recommendations
- Store `aadClientSecret` as a **secret** named value in APIM.
- Store all others as **string** named values.
---
## Dependencies

These policy fragments depend on the following Azure Admin APIs and endpoints:

| HTTP Method | API Endpoint                                                                                                                      | Purpose/Action                   | Required Access Rights (OAuth Scope)             |
|-------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------|-------------------------------------------------|
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}?api-version=2024-05-01`                      | Retrieve API metadata            | Azure AD token with Azure Resource Manager (ARM) scope |
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}?export=true&format=openapi-link&api-version=2024-05-01` | Export OpenAPI/Swagger specification | Azure AD token with ARM scope                     |
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/apis/{apiId}/tags?api-version=2024-05-01`                | Retrieve API tags                | Azure AD token with ARM scope                     |
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/subscriptions/{subscriptionId}?api-version=2021-08-01`   | Retrieve APIM subscription details | Azure AD token with ARM scope                  |
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/products/{productId}?api-version=2024-05-01`              | Retrieve APIM product details    | Azure AD token with ARM scope                     |
| GET         | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/products/{productId}/apis?api-version=2024-05-01`          | List APIs assigned to a product  | Azure AD token with ARM scope                     |
| POST        | `https://login.microsoftonline.com/{aadTenantId}/oauth2/v2.0/token`                                                               | Obtain Azure AD access token via OAuth 2.0 client credentials flow  | App registration or managed identity            |



---
## Access Rights and Role Assignment

To access all referenced APIs, the identity (App Registration or Managed Identity) must have:

- **Azure Role Assignment:**
  - Assign the built-in role **API Management Service Reader** at the APIM instance or resource group level (least privilege, sufficient for all read operations).
  - Alternatively, the **Reader** role is also sufficient but broader.
  - For write/management operations, use **API Management Service Contributor** or **Contributor** (not required for current policies).

- **API Permissions (OAuth Scope):**
  - If using JWT validation on top, ensure the `scp` claim includes `mcp.tools.list` as required by the policy.

---


---

