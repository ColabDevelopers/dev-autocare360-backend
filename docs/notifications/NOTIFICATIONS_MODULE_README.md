# Real-Time Notifications & Communication Module

## Overview
This module implements real-time communication and notifications for the AutoCare360 system using WebSocket (STOMP protocol) and REST APIs.

## Features Implemented

### 1. Real-Time WebSocket Communication
- **Technology**: Spring WebSocket with STOMP protocol
- **Endpoints**: 
  - `/ws` - WebSocket connection endpoint (with SockJS fallback)
- **Topics**:
  - `/topic/service-updates` - Broadcast service progress updates
  - `/topic/announcements` - System-wide announcements
  - `/user/{userId}/queue/notifications` - User-specific notifications

### 2. Notification Types
- **SERVICE_UPDATE** - Service progress notifications
- **APPOINTMENT_UPDATE** - Appointment confirmations/reminders
- **PROJECT_UPDATE** - Modification request updates
- **ANNOUNCEMENT** - System announcements
- **TEST** - Test notifications

### 3. REST API Endpoints

#### Get Notifications
```
GET /api/notifications
GET /api/notifications/unread
GET /api/notifications/unread/count
```

#### Manage Notifications
```
PUT /api/notifications/{id}/read
PUT /api/notifications/read-all
DELETE /api/notifications/{id}
```

#### Notification Preferences
```
GET /api/notifications/preferences
PUT /api/notifications/preferences
```

#### Test Endpoint
```
POST /api/notifications/test
```

## Database Schema

### notifications Table
- `id` - Primary key
- `user_id` - Foreign key to users table
- `type` - Notification type (VARCHAR)
- `title` - Notification title
- `message` - Notification message
- `data` - Additional JSON data
- `is_read` - Read status
- `created_at` - Timestamp

### notification_preferences Table
- `id` - Primary key
- `user_id` - Foreign key to users table (unique)
- `email_notifications` - Boolean
- `push_notifications` - Boolean
- `service_updates` - Boolean
- `appointment_reminders` - Boolean

## Integration Guide for Team Members

### For Service Management Module (@Sankalya)
When a service status is updated:

```java
@Autowired
private NotificationService notificationService;

public void updateServiceProgress(Long serviceId, String status, int progress) {
    // Your service update logic...
    
    // Send notification
    ServiceProgressUpdate update = ServiceProgressUpdate.builder()
        .serviceId(serviceId)
        .vehicleNumber("ABC-1234")
        .serviceName("Oil Change")
        .status(status)
        .progressPercentage(progress)
        .currentStage("In Progress")
        .message("Service is " + progress + "% complete")
        .customerId(customerId)
        .estimatedCompletion("2 hours")
        .build();
    
    notificationService.notifyServiceProgress(update);
}
```

### For Appointment Module (@Sachini)
When an appointment is confirmed or updated:

```java
@Autowired
private NotificationService notificationService;

public void confirmAppointment(Long appointmentId, Long customerId) {
    // Your appointment logic...
    
    // Send notification
    AppointmentNotification notification = AppointmentNotification.builder()
        .appointmentId(appointmentId)
        .appointmentDate("2025-11-01")
        .appointmentTime("10:00 AM")
        .serviceType("Regular Service")
        .vehicleNumber("ABC-1234")
        .status("CONFIRMED")
        .message("Your appointment has been confirmed for Nov 1, 2025 at 10:00 AM")
        .customerId(customerId)
        .build();
    
    notificationService.notifyAppointment(notification);
}
```

### For Project/Modification Requests (@Sandaru)
When a modification request is approved/rejected:

```java
@Autowired
private NotificationService notificationService;

public void updateProjectStatus(Long projectId, Long customerId, String status) {
    // Your project update logic...
    
    // Send notification
    notificationService.notifyProjectRequest(
        customerId,
        projectId,
        "Custom Paint Job",
        status // "APPROVED", "REJECTED", "IN_PROGRESS", etc.
    );
}
```

### For Employee Time Logging (@Manodi)
When an employee logs time and updates progress:

```java
@Autowired
private NotificationService notificationService;

public void logTimeAndUpdateProgress(Long serviceId, int newProgress) {
    // Your time logging logic...
    
    // If progress changed significantly, notify customer
    ServiceProgressUpdate update = ServiceProgressUpdate.builder()
        .serviceId(serviceId)
        .vehicleNumber(vehicle.getNumber())
        .serviceName(service.getName())
        .status(service.getStatus())
        .progressPercentage(newProgress)
        .customerId(service.getCustomerId())
        .build();
    
    notificationService.notifyServiceProgress(update);
}
```

## Frontend Integration Guide

### Installation
```bash
npm install sockjs-client @stomp/stompjs
```

### React WebSocket Hook Example

```javascript
// hooks/useNotifications.js
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useNotifications = (userId, onNotification) => {
    const stompClientRef = useRef(null);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        // Create WebSocket connection
        const socket = new SockJS('http://localhost:8080/ws');
        
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
                console.log('STOMP:', str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            
            onConnect: () => {
                console.log('Connected to WebSocket');
                setConnected(true);
                
                // Subscribe to user-specific notifications
                stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
                    const notification = JSON.parse(message.body);
                    console.log('Received notification:', notification);
                    onNotification(notification);
                });
                
                // Subscribe to service updates
                stompClient.subscribe('/topic/service-updates', (message) => {
                    const update = JSON.parse(message.body);
                    console.log('Service update:', update);
                    // Handle service update
                });
                
                // Subscribe to announcements
                stompClient.subscribe('/topic/announcements', (message) => {
                    const announcement = JSON.parse(message.body);
                    console.log('Announcement:', announcement);
                    // Handle announcement
                });
            },
            
            onDisconnect: () => {
                console.log('Disconnected from WebSocket');
                setConnected(false);
            },
            
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            }
        });

        stompClient.activate();
        stompClientRef.current = stompClient;

        // Cleanup on unmount
        return () => {
            if (stompClientRef.current) {
                stompClientRef.current.deactivate();
            }
        };
    }, [userId, onNotification]);

    return { connected };
};
```

### Usage in React Component

```javascript
// components/NotificationProvider.jsx
import React, { createContext, useContext, useState } from 'react';
import { useNotifications } from '../hooks/useNotifications';
import { toast } from 'react-toastify'; // or any notification library

const NotificationContext = createContext();

export const NotificationProvider = ({ children, userId }) => {
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);

    const handleNotification = (notification) => {
        setNotifications(prev => [notification, ...prev]);
        setUnreadCount(prev => prev + 1);
        
        // Show toast notification
        toast.info(notification.message, {
            position: "top-right",
            autoClose: 5000,
        });
    };

    const { connected } = useNotifications(userId, handleNotification);

    return (
        <NotificationContext.Provider value={{ 
            notifications, 
            unreadCount, 
            connected 
        }}>
            {children}
        </NotificationContext.Provider>
    );
};

export const useNotificationContext = () => useContext(NotificationContext);
```

### REST API Usage Examples

```javascript
// services/notificationService.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/notifications';

export const notificationService = {
    // Get all notifications
    getAll: async () => {
        const response = await axios.get(API_URL, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        return response.data;
    },

    // Get unread notifications
    getUnread: async () => {
        const response = await axios.get(`${API_URL}/unread`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        return response.data;
    },

    // Get unread count
    getUnreadCount: async () => {
        const response = await axios.get(`${API_URL}/unread/count`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        return response.data.count;
    },

    // Mark as read
    markAsRead: async (notificationId) => {
        await axios.put(`${API_URL}/${notificationId}/read`, {}, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
    },

    // Mark all as read
    markAllAsRead: async () => {
        await axios.put(`${API_URL}/read-all`, {}, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
    },

    // Delete notification
    delete: async (notificationId) => {
        await axios.delete(`${API_URL}/${notificationId}`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
    },

    // Get preferences
    getPreferences: async () => {
        const response = await axios.get(`${API_URL}/preferences`, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        return response.data;
    },

    // Update preferences
    updatePreferences: async (preferences) => {
        const response = await axios.put(`${API_URL}/preferences`, preferences, {
            headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
        });
        return response.data;
    }
};
```

## Testing

### Manual Testing Steps

1. **Start the backend server**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Test REST endpoints** using Postman or curl:
   ```bash
   # Get unread count
   curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/notifications/unread/count

   # Send test notification
   curl -X POST -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/api/notifications/test
   ```

3. **Test WebSocket connection** using a WebSocket client or browser console

### Unit Testing

See `NotificationServiceTest.java` for test examples.

## Production Deployment Notes

1. **WebSocket Configuration**: Ensure your deployment supports WebSocket connections
2. **CORS Settings**: Update `WebSocketConfig.java` with production frontend URL
3. **Database Migration**: Run Flyway migration V3__add_notifications.sql
4. **Monitoring**: Monitor WebSocket connections and message delivery

## Troubleshooting

### WebSocket Connection Issues
- Check CORS configuration in `WebSocketConfig.java`
- Verify firewall/proxy allows WebSocket connections
- Check browser console for WebSocket errors

### Notifications Not Delivered
- Verify user preferences allow notifications
- Check database for saved notifications
- Verify WebSocket connection is active

## Future Enhancements
- Email notification integration
- Push notifications for mobile apps
- Notification scheduling
- Rich media notifications (images, videos)
- Notification templates

