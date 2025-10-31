# 🧪 Notifications Module API Testing Guide

## 💡 Quick Answer: Can I test everything with just the Admin account?

**YES!** ✅ All notification endpoints work for **ANY authenticated user** (Admin, Customer, or Employee). You don't need to create customer/employee accounts to test endpoints E-I. They all just need a valid JWT token.

**For quick testing:** Use `nimal.admin@gmail.com` / `password` (already exists)

**For realistic demo:** Run `test_data_notifications.sql` to create customer account with realistic service/appointment notifications.

See `FAQ_TESTING_USERS.md` for detailed explanation.

---

## 📋 Prerequisites

### 1. Start the Application
```bash
mvn spring-boot:run
```

Wait for: `Started Autocare360Application in X seconds` ✅

### 2. Verify Database Connection
- Application will automatically create:
  - Roles: `ADMIN`, `EMPLOYEE`, `CUSTOMER`
  - Default admin user: `nimal.admin@gmail.com` / `password`
  - Tables: `notifications`, `notification_preferences`

---

## 🔐 Step 1: Authentication (Get JWT Token)

### Login Request

**Endpoint:** `POST http://localhost:8080/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (Admin User):**
```json
{
  "email": "nimal.admin@gmail.com",
  "password": "password"
}
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "nimal.admin@gmail.com",
  "name": "System Admin",
  "roles": ["admin"]
}
```

**Action:** Copy the `token` value - you'll use this for all other requests!

---

## 🧪 Step 2: Test Your Notification Endpoints

### A. Send Test Notification

**Endpoint:** `POST http://localhost:8080/api/notifications/test`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Expected Response:**
```json
{
  "message": "Test notification sent"
}
```

**What Happens:**
- Creates a notification in database
- Attempts to send via WebSocket (if client connected)
- You can verify in DBeaver: `SELECT * FROM notifications;`

---

### B. Get All My Notifications

**Endpoint:** `GET http://localhost:8080/api/notifications`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "type": "TEST",
    "title": "Test Notification",
    "message": "This is a test notification to verify the real-time notification system is working correctly.",
    "isRead": false,
    "createdAt": "2025-10-30T12:34:56Z",
    "data": "{\"testData\":\"This is a test notification\"}"
  }
]
```

---

### C. Get Unread Notifications Only

**Endpoint:** `GET http://localhost:8080/api/notifications/unread`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:** Same format as above, but only unread notifications

---

### D. Get Unread Count

**Endpoint:** `GET http://localhost:8080/api/notifications/unread/count`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "count": 1
}
```

---

### E. Mark Notification as Read

**✅ Works for ANY authenticated user (Admin/Customer/Employee)**

**Endpoint:** `PUT http://localhost:8080/api/notifications/1/read`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "message": "Notification marked as read"
}
```

**Verify:** Run "Get Unread Count" again - should decrease by 1

---

### F. Mark All as Read

**✅ Works for ANY authenticated user (Admin/Customer/Employee)**

**Endpoint:** `PUT http://localhost:8080/api/notifications/read-all`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "message": "All notifications marked as read"
}
```

---

### G. Delete Notification

**✅ Works for ANY authenticated user (Admin/Customer/Employee)**

**Endpoint:** `DELETE http://localhost:8080/api/notifications/1`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "message": "Notification deleted successfully"
}
```

---

### H. Get Notification Preferences

**✅ Works for ANY authenticated user (Admin/Customer/Employee)**

**Endpoint:** `GET http://localhost:8080/api/notifications/preferences`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Expected Response:**
```json
{
  "id": 1,
  "userId": 1,
  "emailNotifications": true,
  "pushNotifications": true,
  "serviceUpdates": true,
  "appointmentReminders": true
}
```

---

### I. Update Notification Preferences

**✅ Works for ANY authenticated user (Admin/Customer/Employee)**

**Endpoint:** `PUT http://localhost:8080/api/notifications/preferences`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body:**
```json
{
  "emailNotifications": true,
  "pushNotifications": false,
  "serviceUpdates": true,
  "appointmentReminders": false
}
```

**Expected Response:**
```json
{
  "id": 1,
  "userId": 1,
  "emailNotifications": true,
  "pushNotifications": false,
  "serviceUpdates": true,
  "appointmentReminders": false
}
```

---

## 🧪 Step 3: Test Service Integration (How Other Modules Use Your Service)

### Simulate Service Progress Notification

Run this SQL in DBeaver to simulate what the "Vehicle & Service Management" module would do:

```sql
-- This simulates NotificationService.notifyServiceProgress() being called
-- In production, this happens when Sankalya's service module updates progress
INSERT INTO notifications (user_id, type, title, message, data, is_read) VALUES
(1, 'SERVICE_UPDATE', 'Service Progress Update', 
 'Your Oil Change for vehicle ABC-1234 is now In Progress (50% complete)',
 '{"serviceId":1,"vehicleNumber":"ABC-1234","status":"In Progress","progressPercentage":50,"currentStage":"Oil Draining"}',
 false);
```

**Then verify:** `GET http://localhost:8080/api/notifications` should show this new notification

---

### Simulate Appointment Notification

```sql
-- This simulates NotificationService.notifyAppointment() being called
-- In production, this happens when Sachini's appointment module creates/updates appointments
INSERT INTO notifications (user_id, type, title, message, data, is_read) VALUES
(1, 'APPOINTMENT_UPDATE', 'Appointment Confirmed', 
 'Your appointment for Oil Change on 2025-11-05 at 10:00 AM has been confirmed',
 '{"appointmentId":1,"appointmentDate":"2025-11-05","appointmentTime":"10:00","serviceType":"Oil Change","vehicleNumber":"ABC-1234","status":"Confirmed"}',
 false);
```

---

### Simulate Project/Modification Request Notification

```sql
-- This simulates NotificationService.notifyProjectRequest() being called
-- In production, this happens when Sandaru's modification request module processes requests
INSERT INTO notifications (user_id, type, title, message, data, is_read) VALUES
(1, 'PROJECT_UPDATE', 'Project Request Approved', 
 'Your modification request "Custom Spoiler Installation" has been approved',
 '{"projectId":1,"projectName":"Custom Spoiler Installation","status":"Approved"}',
 false);
```

---

## 🎯 Complete Testing Checklist

- [ ] **Authentication:** Login and get JWT token
- [ ] **Test Notification:** Send test notification
- [ ] **Fetch Notifications:** Get all notifications
- [ ] **Fetch Unread:** Get unread notifications only
- [ ] **Count Unread:** Get unread count
- [ ] **Mark Read:** Mark single notification as read
- [ ] **Mark All Read:** Mark all as read
- [ ] **Delete:** Delete a notification
- [ ] **Get Preferences:** Fetch user notification preferences
- [ ] **Update Preferences:** Update notification preferences
- [ ] **Service Integration:** Simulate notifications from other modules (SQL)
- [ ] **Database Verification:** Check data in DBeaver after each operation

---

## 🐛 Troubleshooting

### Issue: 401 Unauthorized
**Solution:** 
- Make sure you copied the full token from login response
- Check header format: `Authorization: Bearer YOUR_TOKEN_HERE` (note the space after Bearer)
- Token expires in 1 hour - login again if expired

### Issue: 403 Forbidden
**Solution:** 
- User doesn't have required role
- For notification endpoints, any authenticated user should work

### Issue: 404 Not Found
**Solution:** 
- Check the URL is correct: `http://localhost:8080/api/notifications`
- Make sure application is running on port 8080

### Issue: 500 Internal Server Error
**Solution:** 
- Check application logs in console
- Common causes:
  - Database connection lost
  - Invalid notification ID in path parameter
  - Missing user ID in auth context

---

## 📊 Database Verification Queries

Run these in DBeaver to verify your API operations:

```sql
-- View all notifications
SELECT * FROM notifications ORDER BY created_at DESC;

-- View notification preferences
SELECT * FROM notification_preferences;

-- Count notifications by type
SELECT type, COUNT(*) as count FROM notifications GROUP BY type;

-- Count unread notifications per user
SELECT user_id, COUNT(*) as unread_count 
FROM notifications 
WHERE is_read = FALSE 
GROUP BY user_id;

-- View user details with roles
SELECT u.id, u.email, u.name, GROUP_CONCAT(r.name) as roles
FROM users u
LEFT JOIN users_roles ur ON u.id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.id
GROUP BY u.id;
```

---

## 🚀 Next Steps: WebSocket Testing

After REST API testing works, test real-time WebSocket notifications:
1. Use a WebSocket client (Postman supports WebSockets, or use browser)
2. Connect to: `ws://localhost:8080/ws`
3. Subscribe to: `/user/queue/notifications`
4. Send test notification via REST API
5. Should receive message in real-time!

---

## ✅ Success Criteria

Your notification module is working correctly if:
- ✅ All REST endpoints return expected responses
- ✅ Notifications appear in database after creation
- ✅ Read/unread status updates correctly
- ✅ Preferences save and load correctly
- ✅ Other modules can trigger notifications (via SQL simulation)
- ✅ Authorization works (401 without token, 200 with valid token)

---

**Created for:** Lasini Induma - Real-Time Communication & Notifications Module
**Date:** October 30, 2025
**Project:** AutoCare360 Backend

