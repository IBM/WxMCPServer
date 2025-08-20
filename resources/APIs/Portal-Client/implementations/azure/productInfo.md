# `/productInfo` Policy Documentation

This document describes the **Azure API Management (APIM) inbound policy** implemented for the `/productInfo` endpoint.  
The policy enriches API responses by retrieving subscription and product information from Azure Resource Manager (ARM), and then constructing a structured JSON object to return to the client.

---

## 📖 Policy Overview

The `/productInfo` policy performs the following steps:

1. **Identify matching request** – The policy executes only if the request URL contains `/productinfo`.
2. **Fetch subscription details** – Queries the APIM subscription resource in Azure to retrieve subscription details for the current request.
3. **Extract product association** – From the subscription’s `properties.scope`, the associated *Product ID* is parsed.
4. **Fetch product details** – Retrieves metadata (ID, name, description) of the associated APIM Product.
5. **Fetch APIs under product** – Queries the Product → APIs association to fetch the list of APIs assigned to the product.
6. **Assemble response** – Constructs and returns a clean JSON payload with:
   - Gateway Product ID
   - Display Name
   - Description
   - Application ID & Name
   - List of related API IDs

---

## 🔗 Admin API Dependencies

The following **Azure Management API calls** are invoked by the policy:

| Step | Endpoint | Method | Description |
|------|----------|--------|-------------|
| 2 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/subscriptions/{subscriptionId}?api-version=2021-08-01` | `GET` | Retrieves subscription details within the APIM instance. |
| 4 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/products/{productId}?api-version=2024-05-01` | `GET` | Retrieves details of the APIM Product associated to the subscription. |
| 6 | `/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.ApiManagement/service/{serviceName}/products/{productId}/apis?api-version=2024-05-01` | `GET` | Lists all APIs associated with the specified product. |

---

## 🔐 Minimum Required Azure Role Permissions

To execute successfully, the **Managed Identity or Service Principal** used by APIM must have **read access** to APIM resources. The **minimal role** required is:

- **Reader** role on the Azure API Management service **OR**
- **API Management Service Reader Role** (least-privilege, scoped to the APIM instance)

These roles provide API read permissions for:
- **Microsoft.ApiManagement/service/subscriptions/read**
- **Microsoft.ApiManagement/service/products/read**
- **Microsoft.ApiManagement/service/products/apis/read**



