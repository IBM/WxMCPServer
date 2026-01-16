
## A guide for implementing WxMCP-Tool-Catalog API for webMethods API Gateway

The implementation relies on the internal webMethods API Gateway Admin APIs and the ([Variable Framework](https://www.ibm.com/docs/en/wam/wm-api-gateway/10.15.0?topic=policies-variable-framework))

## Step-by-Step instructions

### Alias configuration inside webMethods API Gateway
- Create a **Simple Alias** `iwhi_wm_apigw_host` and add the host name of your gateway
- Create a **HTTP Transport security alias** `iwhi_wm_apigw_credentials` and specify the admin credentials for this API Gateway (needed to access admin APIs)

### API import and policy configuration
- Import the **MCP-ToolCatalog API** from this GitHub repository ([Specification](../../WxMCP-Tool-Catalog-1-1.yml)) into webMethods API Gateway (Create an API of type `OpenAPI`).
- Add 3 scopes to the **MCP-ToolCatalog API**:
  - `getApiDetails` (Path `/apis/{apiID}`)
  - `getProductInfo` (Path `/productInfo`)
  - `downloadOpenAPI` (Path `/files/{fileID}`)
- Switch to **Identify & Authorize** policy and add `API Key`  or `OAuth2 Token`
- Switch to **Identify & Authorize** policy and add `API Key`  or `OAuth2 Token`
- Switch to **Request Processing**  policy and add **Request Transformation** policy action for scope `getApiDetails`:
    ```
    Header/Query/Path transformation
    Variable
    ${request.headers.routeTo}
    Value
    rest/apigateway/apis/${request.path.apiID}
    ```
- Switch to **Request Processing**  policy and add **Request Transformation** action for scope `getProductInfo`:
    ```
    Header/Query/Path transformation
    Variable
    ${request.headers.routeTo}
    Value
    rest/apigateway/packages/${packageId}
    ```
- Switch to **Request Processing**  policy and add **Request Transformation** action for scope `downloadOpenAPI`: 
    ```
    Header/Query/Path transformation
    Variable
    ${request.headers.routeTo}
    Value
    rest/apigateway/apis/${request.path.fileID}?format=openapi
    ```
- Switch to **Routing Policy** policy and add **Dynamic Routing**:
    ```
    Configure "RouteTo" -> "Endpoint URI"
    https://${iwhi_wm_apigw_host}/${sys:dyn-Endpoint}

    Add rule using Header "routeTo" and "Endpoint URI"
    https://${iwhi_wm_apigw_host}/${sys:dyn-Endpoint}
    ```
- Switch to **Request Processing**  policy and add **Validate API Specifcation**(Schema) for scope `API Scope` 
- Switch to **Response Processing**  policy and add 2 **Request Transformation**s for scope `getApiDetails`: 
    ```
    1. 
    Condition
    ${response.payload.jsonPath[$.apiResponse.api.apiDefinition.tags[*].name]}
    Operator
    Exists

    Payload Transformation (JSON, Autoformat disabled)
    {
      "name": "${response.payload.jsonPath[$.apiResponse.api.apiName]}",
      "owner": "${response.payload.jsonPath[$.apiResponse.api.owner]}",
      "id": "${response.payload.jsonPath[$.apiResponse.api.id]}",
      "description": "${response.payload.jsonPath[$.apiResponse.api.apiDefinition.info.description]}",
      "version": "${response.payload.jsonPath[$.apiResponse.api.apiVersion]}",
      "tags": ${response.payload.jsonPath[$.apiResponse.api.apiDefinition.tags[*].name]},
      "attachments": [
        {
          "name": "OpenAPI File ID for API: ${response.payload.jsonPath[$.apiResponse.api.apiName]}",
          "fileID": "${response.payload.jsonPath[$.apiResponse.api.id]}"
        }
      ],
      "endPoints": [
        {
          "name": "${response.payload.jsonPath[$.apiResponse.gatewayEndPointList[0].endpointName]}",
          "baseUrl": "${response.payload.jsonPath[$.apiResponse.gatewayEndPointList[0].endpointUrls[0]]}"
        }
      ]
    }

    2.
    Condition (AND)
    ${response.payload.jsonPath[$.apiResponse.api.apiDefinition.tags[*].name]}
    Operator
    Not Exists
    Condition
    ${response.payload.jsonPath[$.apiResponse]}
    Operator
    Exists

    Payload Transformation (JSON, Autoformat disabled)
    {
        "name": "${response.payload.jsonPath[$.apiResponse.api.apiName]}",
        "owner": "${response.payload.jsonPath[$.apiResponse.api.owner]}",
        "id": "${response.payload.jsonPath[$.apiResponse.api.id]}",
        "description": "${response.payload.jsonPath[$.apiResponse.api.apiDefinition.info.description]}",
        "version": "${response.payload.jsonPath[$.apiResponse.api.apiVersion]}",
        "tags": [],
        "attachments": [
            {
                "name": "OpenAPI File ID for API: ${response.payload.jsonPath[$.apiResponse.api.apiName]}",
                "fileID": "${response.payload.jsonPath[$.apiResponse.api.id]}"
            }
        ],
        "endPoints": [
            {
                "name": "${response.payload.jsonPath[$.apiResponse.gatewayEndPointList[0].endpointName]}",
                "baseUrl": "${response.payload.jsonPath[$.apiResponse.gatewayEndPointList[0].endpointUrls[0]]}"
            }
        ]
    }
    ```
- Switch to **Response Processing**  policy and add 1 **Request Transformation** for scope `getProductInfo`:
    ```
    No Condition

    Payload Transformation (JSON, Autoformat disabled)
    {
        "productID": "${packageId}",
        "catalogType": "WM_API_GATEWAY",
        "name": "${response.payload.jsonPath[$.packageResponse.name]}",
        "applicationID": "${request.application.id} ",
        "applicationName": "${request.application.name}",
        "description": "${response.payload.jsonPath[$.packageResponse.description]}",
        "apis": ${response.payload.jsonPath[$.packageResponse.apis[*].id]}
    } ```

## Dependencies

These policy fragments depend on the following webMethods Admin APIs and endpoints:


| HTTP Method | API Endpoint                                                                                                                      | Purpose/Action                   |
|-------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| GET         | `/rest/apigateway/apis/{apiId}`                      | Retrieve API metadata incl. API tags, endpoints and file Id for OpenAPI specification           |
| GET         | `/rest/apigateway/packages/${packageId}` | Get metadata for the API package (product) described by the current security context (API Key or OAuth2 Token) |
| GET         | `/rest/apigateway/apis/${request.path.fileID}?format=openapi`                | Get the OpenAPI 3.x specification belonging to the current API as returned by GET /rest/apigateway/apis/{apiId}                |
