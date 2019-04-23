# Agent Authorisation Test Support API documentation version 1.0

### Overview
The Agent Authorisation Test Support API provides test-only ability to accept or reject an authorisation request created with Agent Authorisation API. In normal circumstances, this had to be done by the individual or organisation going through dedicated acceptance UI. For an external test environment, we provide two open test endpoints which conclude the authorisation request with the same effects as UI journey.

---

## /agent-authorisation-test-support

### /agent-authorisation-test-support/invitations/:id

#### **PUT**:

###### Headers

| Name | Type | Description | Required | Examples |
|:-----|:----:|:------------|:--------:|---------:|
| Accept | string | Specifies the response format and the [version](/api-documentation/docs/reference-guide#versioning) of the API to be used. | true | ``` application/vnd.hmrc.1.0+json ```  |
| Content-Length | number | An empty PUT body must have this header value set to 0 | true | 0  |

### Response code: 204
Invitation has been accepted

### Response code: 400

#### errorResponse (application/json) 

```
{
  "code": "BAD_REQUEST",
  "message": "Missing or unsupported version number"
}
```
```
{
  "code": "BAD_REQUEST",
  "message": "Missing or unsupported content-type."
}
```

##### *errorResponse*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|
| code |  string |  | true |  |

### Response code: 404
Invitation not found for the given id

### Response code: 406

#### errorResponse (application/json) 

```
{
  "code": "ACCEPT_HEADER_INVALID",
  "message": "Missing 'Accept' header."
}
```
```
{
  "code": "ACCEPT_HEADER_INVALID",
  "message": "Invalid 'Accept' header"
}
```

##### *errorResponse*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|
| code |  string |  | true |  |

### Response code: 409
Invitation already has been rejected or expired

---
#### **DELETE**:

###### Headers

| Name | Type | Description | Required | Examples |
|:-----|:----:|:------------|:--------:|---------:|
| Accept | string | Specifies the response format and the [version](/api-documentation/docs/reference-guide#versioning) of the API to be used. | true | ``` application/vnd.hmrc.1.0+json ```  |

### Response code: 204
Invitation has been rejected.

### Response code: 400

#### errorResponse (application/json) 

```
{
  "code": "BAD_REQUEST",
  "message": "Missing or unsupported version number"
}
```
```
{
  "code": "BAD_REQUEST",
  "message": "Missing or unsupported content-type."
}
```

##### *errorResponse*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|
| code |  string |  | true |  |

### Response code: 404
Invitation not found for the given id

### Response code: 406

#### errorResponse (application/json) 

```
{
  "code": "ACCEPT_HEADER_INVALID",
  "message": "Missing 'Accept' header."
}
```
```
{
  "code": "ACCEPT_HEADER_INVALID",
  "message": "Invalid 'Accept' header"
}
```

##### *errorResponse*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|
| code |  string |  | true |  |

### Response code: 409
Invitation already has been accepted or expired

---

