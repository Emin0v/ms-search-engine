# ms-search-engine

[![CI](https://github.com/Emin0v/ms-search-engine/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Emin0v/ms-search-engine/actions/workflows/ci.yml)

A generic, metadata-driven search and filter microservice.

---

## Overview

`ms-search-engine` provides a unified search, filter, sort, and pagination layer for multiple business domains - without requiring a separate Java service per domain.

Domain microservices retain ownership of CRUD and business logic. `ms-search-engine` is responsible only for **search, filter, sort, pagination, and metadata-driven query building**.

Search data is read from **ClickHouse Silver tables** prepared by the Data Team. PostgreSQL is used only for search metadata.

---

## Main Idea

The frontend sends all search requests to a single generic endpoint:

```http
POST /api/v1/search/{domain}/query
```

**Example:**

```http
POST /api/v1/search/contracts/query
```

Here, `contracts` is the **domain code**. The service uses this code to resolve:

- Which ClickHouse table to query
- Which fields are allowed
- Which fields are filterable or sortable
- Which column to use for tenant filtering

---

## Metadata Tables

### `search_domain`

Stores domain-level configuration.

| Column | Description |
|---|---|
| `code` | Domain code used in the API path (e.g. `contracts`) |
| `clickhouse_table` | ClickHouse Silver table name |
| `tenant_column` | Column used for organization-level filtering (optional) |
| `default_sort_field` | Default field for frontend sorting |
| `default_sort_direction` | Default sort direction |
| `active` | Whether the domain is active |

**Example row:**

```
code                  = contracts
clickhouse_table      = silver_contract_360_flat
tenant_column         = organization_id
default_sort_field    = documentDate
default_sort_direction = DESC
```

---

### `search_field`

Stores field-level configuration per domain.

| Column | Description |
|---|---|
| `request_field` | Field name used by the frontend |
| `source_column` | Actual ClickHouse column name |
| `label` | Display label for the frontend |
| `type` | Field type: `TEXT`, `NUMBER`, `DATE`, `DATE_TIME`, `SELECT`, `BOOLEAN` |
| `filterable` | Whether this field can be used in filters |
| `sortable` | Whether this field can be used for sorting |
| `exportable` | Whether this field can be included in exports (future) |
| `projection_default` | Returned by default if frontend does not specify columns |
| `array_field` | Whether the ClickHouse column is an array type |
| `allowed_operators` | Comma-separated list of allowed filter operators |

**Example row:**

```
request_field     = currencyName
source_column     = currency_name
type              = TEXT
filterable        = true
sortable          = true
allowed_operators = EQUALS,CONTAINS
```

> The frontend sends `currencyName`. The backend resolves it to `currency_name` via metadata — frontend field names are **never** used directly in SQL.

---

## Tenant Filtering

If `tenant_column` is set on a domain, the backend automatically appends a tenant filter to every query:

```sql
AND `organization_id` = :tenantValue
```

If `tenant_column` is `null`, no tenant filter is applied.

---

## Example Search Request

**Request:**

```http
POST /api/v1/search/contracts/query
```

```json
{
  "filters": [
    {
      "field": "currencyName",
      "operator": "CONTAINS",
      "value": "AZ"
    }
  ],
  "columns": [
    "contractNumber",
    "contractorName",
    "currencyName",
    "documentDate"
  ],
  "page": 0,
  "size": 20,
  "sortBy": "documentDate",
  "sortDirection": "DESC"
}
```

**Generated ClickHouse SQL:**

```sql
SELECT
    `contract_number`  AS `contractNumber`,
    `contractor_name`  AS `contractorName`,
    `currency_name`    AS `currencyName`,
    `document_date`    AS `documentDate`
FROM `silver_contract_360_flat`
WHERE 1 = 1
  AND `organization_id` = :tenantValue
  AND positionCaseInsensitive(toString(`currency_name`), :p0) > 0
ORDER BY `document_date` DESC
LIMIT :limit OFFSET :offset
```

All values are passed as **named parameters** - never interpolated into the query string.

---

## Internal Flow

```
1. Frontend calls POST /api/v1/search/{domain}/query
2. Controller extracts the domain code from the URL
3. Domain registry loads the search_domain record by code
4. Field registry loads active search_field rows for the domain
5. Validation checks fields, operators, sort targets, and SQL identifiers
6. Query builder constructs a safe ClickHouse SQL statement
7. ClickHouse client executes the query
8. API returns a paginated response
```

---

## Metadata Endpoint

Frontend clients can discover available fields for a domain:

```http
GET /api/v1/search/{domain}/meta/columns
```

**Example:**

```http
GET /api/v1/search/contracts/meta/columns
```

The response includes field name, label, type, filterable/sortable flags, and allowed operators - enabling the frontend to build dynamic filter UIs.

---

## Domain Registration

New domains and fields are registered via **Liquibase changelogs**. This is the recommended MVP approach since metadata fully controls SQL generation and data access.

**Steps for each new searchable page:**

1. Data Team prepares the ClickHouse Silver table
2. Backend registers the domain in `search_domain`
3. Backend registers allowed fields in `search_field`
4. Frontend uses `/meta/columns` to build the filter UI
5. QA tests filter, sort, and pagination behavior

> An Admin API/UI for managing metadata can be added later, after audit, permissions, preview, and approval workflows are implemented.

---

## Safety Rules

The service enforces the following rules at all times:

- No `SELECT *` - projections are always explicit
- Frontend field names are never placed directly into SQL
- Only metadata registry mappings are used for column resolution
- Table and column identifiers are validated against the registry
- SQL identifiers are quoted
- All filter values are passed as named parameters
- Table names are never derived from the request body
- One Java service handles all domains - no per-domain services
- Deep pagination is capped with a hard limit
- Tenant filter is applied only when `tenant_column` is configured

---
## Quick Start

Ensure you have [Docker](https://www.docker.com/) and Java 21 installed.

**1. Start the infrastructure (PostgreSQL & ClickHouse)**
```bash
docker compose -f infra/docker-compose.yml up -d
```
2. Build the application
```bash
./gradlew clean build
```

3. Run the application

```bash

./gradlew bootRun --args='--spring.profiles.active=local'

```

The local profile serves the API on port `8095`. Compose starts the database containers;
it does not create the ClickHouse Silver tables or load search data. The registered domain
and field metadata must match tables and columns prepared in ClickHouse before a search
request can succeed.

## Verification status

Run `./gradlew check` for query/validation regression tests and Checkstyle. CI runs the same
command. The tests cover bound values, tenant parameters, rejected identifiers and fields,
operator constraints, malformed requests, and pagination boundaries. They do not connect to
PostgreSQL or ClickHouse, or establish the surrounding authentication boundary.
Database integration tests and a seeded local demonstration remain verification priorities.

Tenant filtering requires a trusted tenant identity from the surrounding authentication
boundary. Parameterized query values alone do not establish authorization.
