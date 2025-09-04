# WxMCPServer

**WxMCPServer** is a webMethods Integration Server (IS) package that implements an [MCP Server](https://modelcontextprotocol.io/docs/learn/server-concepts) for IBM webMethods Hybrid Integration (IWHI).  
It requires either **webMethods Integration Server** or **webMethods Microservices Runtime** for hosting.

## Table of Contents

- [1. Overview](#1-overview)
- [2. Key Benefits](#2-key-benefits)
- [3. Requirements](#3-requirements)
- [4. Roles and Responsibilities](#4-roles-and-responsibilities)
- [5. Integration Server Global Variables](#5-integration-server-global-variables)
- [6. Configuration Examples](#6-configuration-examples)
  - [6.1 Claude Desktop — API Key](#61-claude-desktop--api-key)
  - [6.2 Claude Desktop — OAUTH Style](#62-claude-desktop--oauth-style)
  - [6.3 DataStaX/Langflow MCP Server - API Key Style](#63-datastaxlangflow-mcp-server---api-key-style)
  - [6.4 DataStaX/Langflow MCP Server - OAuth Style](#64-datastaxlangflow-mcp-server---oauth-style)
- [7. Limitations](#7-limitations)

---

## 1. Overview

WxMCPServer enables you to expose your existing APIs, including their existing API policies, as **MCP Tools** through an enterprise-grade integration platform.

It leverages your existing API management infrastructure:

- **API Catalog**  
  Introspect API Management applications or Developer Portals for APIs to be exposed as MCP tools  
- **API Gateway**  
  Enforce policies such as logging, authentication, and authorization  

![Screenshot](resources/images/overview.png)

In the current solution approach, the **MCP Tool Catalog API** is grouped together with business APIs into an API product. This API product is then used to retrieve metadata (including the [OpenAPI](https://www.openapis.org/) specification) for all APIs in the product.

---

## 2. Key Benefits

- Reuse existing corporate APIs as AI-accessible MCP tools  
- Retain existing API Gateway security and policy enforcement  
- Integrate seamlessly with API catalogs for API discovery  
- One API key or access token can access the MCP Tool Catalog API and all business APIs in the same API product  

---

## 3. Requirements

**WxMCPServer** requires **IBM webMethods Integration Server** or **IBM webMethods Microservices Runtime** as the server (tested with v11.1).  
To integrate with API Management solutions, you must implement the [MCP Tool Catalog API](/resources/APIs/WxMCP-Tool-Catalog/WxMCP-Tool-Catalog-1-1.yml).

There are instructions (and pre-configured assets) on how to implement this API on:

- [webMethods API Management](/resources/APIs/WxMCP-Tool-Catalog/implementations/webMethods/readme.md)  
- [Azure API Management](/resources/APIs/WxMCP-Tool-Catalog/implementations/webMethods/readme.md)  

The approach is generally open for 3rd party API Management solutions ("Federated API Management").

---

## 4. Roles and Responsibilities

- **MCP Host** and **MCP Client**
External components (not part of this solution) essential for using the tools; examples include Claude Desktop or Langflow  
- **WxMCPServer**
webMethods IS package implementing the MCP Server  
- **API Gateway**
Hosts business APIs (to be exposed as MCP tools) and the MCP Tool Catalog API to extract API metadata from the API Catalog  
- **API Catalog**
Allows AI developers to request access to API products and retrieve API keys or OAuth credentials  

The following graphic provides an overview of the architecture:

![Screenshot](/resources/images/architecture.png)

---

## 5. Integration Server Global Variables

You can set default values for `WxMCPServer`, which are used if no corresponding HTTP headers are sent.

*Note:* MCP client HTTP headers **always** take precedence over default values.

| Variable Name                 | Required    | Default Value                      | Description                                                                                         |
|------------------------------|-------------|----------------------------------|-----------------------------------------------------------------------------------------------------|
| `wxmcp.cache.manager.name`    | No          | `WxMCP_Cache_Manager_Default`    | The name of the Cache Manager to be used.                                                          |
| `wxmcp.auth.type`             | Yes         | (none)                           | Authentication type: `"OAUTH"` or `"API_KEY"`.                                                      |
| `wxmcp.portal.client.base.url`| Yes         | (none)                           | Base URL of your MCP Tool Catalog API on API Gateway, e.g., `https://<myWebMethodsAPIGateway>/gateway/WxMCP-Tool-Catalog-wMAPIGW/1.1` |
| `wxmcp.api.key.headername`    | Conditional | (none)                           | Used only when `wxmcp.auth.type` = `"API_KEY"`. Specifies the API key header name.                   |
| `wxmcp.tool.header.prefix`    | No          | `header_`                        | Default prefix for tool header properties.                                                         |
| `wxmcp.tool.query.prefix`     | No          | `query_`                         | Default prefix for tool query parameter properties.                                                |
| `wxmcp.tool.path.prefix`      | No          | `path_`                         | Default prefix for tool path parameter properties.                                                 |
| `wxmcp.tool.response.mode`    | No          | `both`                          | Tool response format: `text`, `structured`, or `both`.                                            |
| `wxmcp.response.code.mode`    | No          | `stdio`                         | Response mode: `"stdio"` (always HTTP 200) or `"http"` (actual status codes).                       |

---

## 6. Configuration Examples

### 6.1 Claude Desktop — API Key



```
{
  "mcpServers": {
    "mcp-iwhi-apikey-demo": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://<Integration Server Host>:<Integration Server Port>/mcp",
        "--header",
        "auth_type:API_KEY",
        "--header",
        "api_key:<The API Key>",
        "--header",
        "portal_client_base_url:https://<webMethods API Gateway Host>:<webMethods API Gateway Port>/gateway/WxMCP-Tool-Catalog/1.1",
        "--header",
        "api_key_headername:<Your API Key header - i.e. x-Gateway-APIKey for webMethods>",
        "--header",
        "tool_header_prefix:header_",
        "--header",
        "tool_query_prefix:query_",
        "--header",
        "tool_path_prefix:path_",
		"--header",
		"tool_response_mode:structured",
		"--header",
		"response_code:http"
      ]
    }
  }
}
```

### 6.2 Claude Desktop — OAUTH Style

```
{
  "mcpServers": {
    "mcp-iwhi-oauth-demo": {
      "command": "npx",
      "args": [
        "-y",
        "mcp-remote",
        "http://<Integration Server Host>:<Integration Server Port>/mcp",
        "--header",
        "auth_type:OAUTH",
        "--header",
        "portal_client_base_url:https://<webMethods API Gateway Host>:<webMethods API Gateway Port>/gateway/WxMCP-Tool-Catalog/1.1",
        "--header",
        "oauth_bearer_token:<The bearer token>",
        "--header",
        "tool_header_prefix:header_",
        "--header",
        "tool_query_prefix:query_",
        "--header",
        "tool_path_prefix:path_",
		"--header",
		"tool_response_mode:structured",
		"--header",
		"response_code:http"
      ]
    }
  }
}
```

### 6.3 DataStaX/Langflow MCP Server - API Key Style

```
npx -y mcp-remote http://<Integration Server Host>:<Integration Server Port>/mcp \
--header "auth_type:API_KEY" \
--header "portal_client_base_url:https://<webMethods API Gateway Host>:<webMethods API Gateway Port>/gateway/WxMCP-Tool-Catalog/1.1" \
--header "api_key:<The API Key>" \
--header "api_key_headername:<Your API Key header - i.e. x-Gateway-APIKey for webMethods>" \
--header "tool_header_prefix:header_" \
--header "tool_query_prefix:query_" \
--header "tool_path_prefix:path_" \
--header "tool_response_mode:both" \
--header "response_code:stdio"
```

### 6.4 DataStaX/Langflow MCP Server - OAuth Style

```
npx -y mcp-remote http://<Integration Server Host>:<Integration Server Port>/mcp \
--header "auth_type:OAUTH" \
--header "portal_client_base_url:https://<webMethods API Gateway Host>:<webMethods API Gateway Port>/gateway/WxMCP-Tool-Catalog/1.1" \
--header "oauth_bearer_token:<The bearer token>" \
--header "tool_header_prefix:header_" \
--header "tool_query_prefix:query_" \
--header "tool_path_prefix:path_" \
--header "tool_response_mode:both" \
--header "response_code:stdio"
```

---

## 7. Limitations

- Only `"Content-Type": "application/json"` is supported for sending and receiving data to APIs.