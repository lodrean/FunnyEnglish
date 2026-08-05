# QA Automation

This directory contains all QA automation resources for So to Speak application.

## Structure

```
qa/
├── postman/              # API Tests (Newman/Postman)
│   ├── sotospeak-api.json       # API collection
│   └── test-environment.json       # Test environment variables
└── README.md             # This file
```

## Running Tests

### Prerequisites

1. **Backend must be running** on `http://localhost:8080`
2. **PostgreSQL** must be accessible
3. **Node.js** and **npm** installed

### Install Newman

```bash
npm install -g newman newman-reporter-htmlextra
```

### Run API Tests

```bash
# Run with default environment
cd qa
newman run postman/sotospeak-api.json

# Run with specific environment
newman run postman/sotospeak-api.json \
  -e postman/test-environment.json

# Run with HTML report
newman run postman/sotospeak-api.json \
  -e postman/test-environment.json \
  --reporters cli,htmlextra \
  --reporter-htmlextra-export reports/api-test-report.html
```

### Run in CI/CD

```bash
# Run with delay between requests
newman run postman/sotospeak-api.json \
  -e postman/test-environment.json \
  --delay-request 100

# Run specific folder
newman run postman/sotospeak-api.json \
  --folder "Auth"
```

## Test Coverage

| Endpoint | Method | Tests |
|----------|--------|-------|
| `/auth/register` | POST | User registration |
| `/auth/login` | POST | User login |
| `/categories` | GET | List categories |
| `/categories/{id}` | GET | Category details |
| `/tests` | GET | List tests |
| `/tests/{id}` | GET | Test details |
| `/leaderboard` | GET | Leaderboard |

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `baseUrl` | API base URL | `http://localhost:8080` |
| `testEmail` | Demo user email (auto-created) | `demo@sotospeak.app` |
| `testPassword` | Demo user password | `demo123` |
| `adminEmail` | Admin email (auto-created) | `admin@sotospeak.com` |
| `adminPassword` | Admin password | `admin123` |

> **Note:** Test users are automatically created when the backend starts via `AdminUserInitializer`. No manual registration is required.

## Adding New Tests

1. Open Postman
2. Import `postman/sotospeak-api.json`
3. Add new requests/tests
4. Export collection and replace the JSON file
5. Update this README with new endpoints

## Reports

Test reports are generated in `reports/` directory:
- `api-test-report.html` - HTML report with detailed results
- Newman CLI output - Console summary

## CI/CD Integration

See `.github/workflows/qa-automation.yml` for GitHub Actions configuration.
