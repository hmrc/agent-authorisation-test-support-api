# Agent Authorisation Test Support API documentation version 1.0


### Overview
The Agent Authorisation Test Support API prepares Client's data in the stub and returns a knownFact required when creating agent authorisation request using POST /agents/:arn/invitations.

---

## /agent-authorisation-test-support

### /agent-authorisation-test-support/known-facts/mtd-vat/vrn/{vrn}

* **vrn**: VAT Registration Number
    * Type: string
    
    * Required: true

#### **GET**:

###### Headers

| Name | Type | Description | Required | Examples |
|:-----|:----:|:------------|:--------:|---------:|
| Accept | string | Specifies the response format and the [version](/api-documentation/docs/reference-guide#versioning) of the API to be used. | true | ``` application/vnd.hmrc.1.0+json ```  |

### Response code: 200

#### application/json (application/json) 
Returns VAT Registration Date matching client's VRN for the purpose of MTD-VAT agent authorisation request

```
{
  "service": ["MTD-VAT"],
  "clientType": "business",
  "clientIdType": "vrn",
  "clientId": "101747696",
  "knownFact": "2007-05-18"
}
```

##### *application/json*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|

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

---

### /agent-authorisation-test-support/known-facts/mtd-it/nino/{nino}

* **nino**: National Insurance Number
    * Type: string
    
    * Required: true

#### **GET**:

###### Headers

| Name | Type | Description | Required | Examples |
|:-----|:----:|:------------|:--------:|---------:|
| Accept | string | Specifies the response format and the [version](/api-documentation/docs/reference-guide#versioning) of the API to be used. | true | ``` application/vnd.hmrc.1.0+json ```  |

### Response code: 200

#### application/json (application/json) 
Returns business postcode matching client's NINO for the purpose of MTD-IT agent authorisation request

```
{
  "service": ["MTD-IT"],
  "clientType": "personal",
  "clientIdType": "ni",
  "clientId": "AA999999A",
  "knownFact": "AA11 1AA"
}
```

##### *application/json*:
| Name | Type | Description | Required | Pattern |
|:-----|:----:|:------------|:--------:|--------:|

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

---

### /agent-authorisation-test-support/known-facts/invitations/:id

#### **PUT**:

###### Headers

| Name | Type | Description | Required | Examples |
|:-----|:----:|:------------|:--------:|---------:|
| Accept | string | Specifies the response format and the [version](/api-documentation/docs/reference-guide#versioning) of the API to be used. | true | ``` application/vnd.hmrc.1.0+json ```  |

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

