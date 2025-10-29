# AutoCare360 Backend API Guide

Human-readable summary of the API. For the machine spec, import `docs/openapi.yaml`.

## Overview
- Base URL: `http://localhost:8080`
- Auth: JWT Bearer via `Authorization: Bearer <token>`
- Roles: `admin`, `employee`, `customer`
- Content-Type: `application/json`
- Dev convenience: hard‑coded admin login (auto-creates if missing)
  - email: `nimal.admin@gmail.com`
  - password: `password`

## Error format
- 400
```json
{ "error": { "code": "BAD_REQUEST", "message": "..." } }
```
- 409
```json
{ "error": { "code": "EMAIL_ALREADY_IN_USE", "message": "..." } }
```
- 404
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "..." }
```

## Auth & Users
### Register (customers only)
- POST `/auth/register`
- Body
```json
{
  "name":"Customer Name",
  "email":"customer@demo.com",
  "password":"password",
  "phone":"+1-555-123-4567",
  "vehicleMake":"toyota",
  "vehicleModel":"Camry",
  "vehicleYear":"2020"
}
```
- 201 → `{ id, email, name, roles:["customer"] }`

### Login (all roles)
- POST `/auth/login`
- Body
```json
{ "email":"nimal.admin@gmail.com", "password":"password" }
```
- 200 → `{ accessToken, user }`

### Me (role-aware)
- GET `/users/me`
- 200 →
```json
{
  "id": 7,
  "email": "user@domain.com",
  "name": "User Name",
  "roles": ["employee"],
  "status": "ACTIVE",
  "phone": "+94-7X-XXXXXXX",
  "employeeNo": "EMP-0006",
  "department": "Customer Care"
}
```

### Change password (all roles)
- POST `/users/change-password`
- Body
```json
{ "currentPassword":"old", "newPassword":"new" }
```
- 204

### Update my profile (phone only)
- PATCH `/users/me`
- Body
```json
{ "phone":"+1-555-222-3333" }
```
- 204

## Admin — Employees (admin only)
### Create
- POST `/admin/employees`
- Body
```json
{ "name":"Jane Doe", "email":"jane.employee@gmail.com", "department":"Body Shop" }
```
- 201 → Employee (temp password = `password`, `employeeNo` auto)

### List
- GET `/admin/employees`
- 200 → `Employee[]`

### Get
- GET `/admin/employees/{id}`
- 200 → `Employee`

### Update
- PUT `/admin/employees/{id}`
- Body
```json
{ "name":"Jane D.", "department":"Painting", "status":"ACTIVE" }
```
- 200 → `Employee`

### Reset password
- POST `/admin/employees/{id}/reset-password`
- 200 → `Employee` (password reset to `password`)

### Delete
- DELETE `/admin/employees/{id}`
- 204

## Admin — Customers (admin only)
### List
- GET `/admin/customers`
- 200 → `User[]` (customers only)

### Get
- GET `/admin/customers/{id}`
- 200 → `User`

### Patch (name/phone/status)
- PATCH `/admin/customers/{id}`
- Body
```json
{ "name":"New Name", "phone":"+1-555-222-3333", "status":"ACTIVE" }
```
- 200 → `User`

### Delete
- DELETE `/admin/customers/{id}`
- 204

## Curl cheatsheet
- Admin login
```bash
curl -s http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nimal.admin@gmail.com","password":"password"}'
```
- List employees
```bash
curl -s http://localhost:8080/admin/employees \
  -H "Authorization: Bearer <TOKEN>"
```
- Create employee
```bash
curl -s http://localhost:8080/admin/employees \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"name":"Jane Doe","email":"jane.employee@gmail.com","department":"Body Shop"}'
```

## See also
- OpenAPI spec: `docs/openapi.yaml` (import into Swagger/Postman)
