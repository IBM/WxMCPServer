

# WxMCPServer - Local Flow Services

With the latest release of **WxMCPServer**, you can now also invoke local Integration Server Flow Services.

To be more precise: the Flow Service stubs that have been generated from REST Providers based on OpenAPI 3.x imports.

The overall flow between the actors:

- MCP Host
- webMethods Integration Server hosting WxMCPServer package
- Backend applications to be integrated

is shown in this figure:

   <img src="../images/client-wxmcp-application-flow.png" alt="OAuth Client Generation" width="800"/>


**Benefits:**

- Reduces overall architecture complexity, since the tool catalog is derived from the scopes of the inbound token
- No external gateway or portal component needed, and also no implementation of **Tool Catalog API**
- No outbound HTTP calls necessary; invocation happens at the Java level
- Mitigates problems related to [Confused Deputy](https://modelcontextprotocol.io/specification/draft/basic/security_best_practices#confused-deputy-problem) and **MCP Proxy Server**, since **WxMCPServer** also implements the tools and does not forward to external APIs.


## Overall Use Case

Utilize existing integrations exposed as MCP tools and described by a new OpenAPI 3.x experience API, which is custom-tailored for the AI agent's needs to fulfill its goal.

## Step-by-Step Instructions

### 1. Agentic Experience API

Create an agentic experience API as the base for MCP tools:

- Create your OpenAPI 3.0.1 specification manually or with the help of AI
- Ensure that meaningful operation IDs are added, ready for AI consumption
- Add a global API tag `mcp.object.name:YOUR_OBJECT` at the API level (e.g., "Account"), which results in generated tool names like `YOUR_OBJECT_read` instead of just `read`
- Add OAuth policies and scopes to this API definition; they are mandatory and used to identify the tools made available to the MCP client

You can find a sample [prompt to create an OpenAPI](./agentic-ai-prompt.md) and a sample [Account-Profile-API](./Account-Profile.yaml) in this repository.

### 2. Import API as OpenAPI Provider into webMethods Designer

- Import the API definition as "REST API Descriptor" of type "OpenAPI 3.x" into your package
- Implement the services or simply add mapping steps to create mock data
- Everything else is regular Flow Service development

### 3a. Client Generation and Configuration (Integration Server = Auth Provider)

**Note:**

This description is only valid when `x-auth-type` is set to `INTERNAL`.
If variable `x-auth-type` is set to `THIRD_PARTY`, clients must be created inside the Auth Server.

The OAuth configuration in detail is described in the [documentation](https://www.ibm.com/docs/en/webmethods-integration/wm-integration-server/11.1.0?topic=guide-configuring-oauth).

The most important steps are:

1. After importing the OpenAPI definition in webMethods Designer, check that the scopes have been automatically created:

   <img src="../images/is-scope.png" alt="Integration Server Scopes" width="800"/>

2. Create a new OAuth client application:

   <img src="../images/is-oauth-client-generation.png" alt="OAuth Client Generation" width="800"/>

3. Map the API and **WxMCPServer** scopes to the newly created client:

   <img src="../images/is-client-scope-assignment.png" alt="Client Scope Assignment" width="800"/>

### 3b. Client Generation (External Auth Provider, i.e., Okta, IBM Verify)

**Note:**

This description is only valid when `x-auth-type` is set to `THIRD_PARTY`.
If variable `x-auth-type` is set to `THIRD_PARTY`, clients must be created inside the **external** Auth Server.

Follow the descriptions of your Auth Server about how to create an OAuth client application.

**Important:**

Ensure that the same scopes are exposed by the Auth Server as described by the OpenAPI and imported into Integration Server.

Once this is done, you have to do 2 more things:

1. Configure the connection to the external Auth Server using application credentials with sufficient rights:

   <img src="../images/is-add-auth-server.png" alt="External OAuth Server Configuration" width="600"/>

2. Set the external Auth Server as the new default:

   <img src="../images/is-set-default-auth-server.png" alt="Set Default OAuth Server" width="600"/>

More details can be found in the [documentation](https://www.ibm.com/docs/en/webmethods-integration/wm-integration-server/11.1.0?topic=oauth-using-external-authorization-server).

### 4. Test MCP Connectivity

You are ready to start using your MCP tools. You might want to test them natively using the official [MCP Inspector](https://github.com/modelcontextprotocol/inspector).

**What you need to test:**

- Your Integration Server endpoint (can be local, i.e., by default `http://localhost:5555/v1_5_0/mcp`)
- A valid OAuth token to be sent as a bearer token together with the `Authorization` header

**Example request to obtain a token:**

```bash
curl --request POST \
  --url <YOUR_AUTH_SERVER_TOKEN_ENDPOINT> \
  --header 'Accept: application/json' \
  --header 'Authorization: Basic <CLIENT_ID>:<CLIENT_SECRET>' \
  --header 'content-type: application/x-www-form-urlencoded' \
  --data grant_type=client_credentials \
  --data 'scope=wxmcp.server YOUR_API_SCOPE1 YOUR_API_SCOPE2'
```

- Header `x-auth-type` or global variable `wxmcp.auth.type` set to `INTERNAL` (for Integration Server as Auth Server) or `THIRD_PARTY` (for external OAuth server)