# 🚀 Deployment Checklist for AutoCare360 Backend

Use this checklist before deploying to production or staging environments.

## 📋 Pre-Deployment Checklist

### Code & Build
- [ ] All tests passing (`mvn test`)
- [ ] No compiler warnings
- [ ] Code review completed and approved
- [ ] Latest code pulled from main/DevOps branch
- [ ] Version number updated in `pom.xml`
- [ ] Build successful (`mvn clean package`)
- [ ] Docker image builds successfully

### Configuration
- [ ] Environment variables configured for target environment
- [ ] Database credentials updated and secure
- [ ] JWT secret key is strong and unique (min 256 bits)
- [ ] CORS origins configured correctly
- [ ] Logging levels appropriate for environment
- [ ] File paths and directories correct
- [ ] External service URLs updated

### Security
- [ ] No hardcoded secrets in code
- [ ] Sensitive data encrypted
- [ ] HTTPS enabled (production)
- [ ] Security headers configured
- [ ] Rate limiting enabled
- [ ] Input validation in place
- [ ] SQL injection prevention verified
- [ ] XSS protection enabled
- [ ] CSRF protection enabled
- [ ] Authentication & authorization working

### Database
- [ ] Database backup taken
- [ ] Migration scripts reviewed
- [ ] Flyway migrations tested
- [ ] Database indexes optimized
- [ ] Connection pool configured
- [ ] Rollback plan prepared
- [ ] Database credentials rotated

### Infrastructure
- [ ] Resources allocated (CPU, Memory)
- [ ] Storage provisioned
- [ ] Network configured
- [ ] Load balancer configured
- [ ] SSL certificates valid and installed
- [ ] DNS records updated
- [ ] Firewall rules configured
- [ ] Auto-scaling configured

### Monitoring & Logging
- [ ] Logging configured and tested
- [ ] Metrics collection enabled
- [ ] Alerts configured
- [ ] Health checks working
- [ ] Dashboard set up
- [ ] Error tracking enabled
- [ ] Performance monitoring enabled

### Documentation
- [ ] API documentation updated
- [ ] README.md updated
- [ ] Deployment documentation current
- [ ] Configuration guide updated
- [ ] Runbook prepared
- [ ] Architecture diagram current

### Backup & Recovery
- [ ] Backup strategy in place
- [ ] Backup tested and verified
- [ ] Recovery procedures documented
- [ ] Rollback plan prepared
- [ ] Disaster recovery plan reviewed

## 🚀 Deployment Steps

### Step 1: Pre-Deployment
```bash
# 1. Pull latest code
git pull origin DevOps

# 2. Run tests
mvn test

# 3. Build application
mvn clean package -DskipTests

# 4. Build Docker image
docker build -t autocare360-backend:v1.0.0 .

# 5. Tag Docker image
docker tag autocare360-backend:v1.0.0 your-registry/autocare360-backend:v1.0.0
docker tag autocare360-backend:v1.0.0 your-registry/autocare360-backend:latest

# 6. Push to registry
docker push your-registry/autocare360-backend:v1.0.0
docker push your-registry/autocare360-backend:latest
```

### Step 2: Database Migration
```bash
# 1. Backup current database
mysqldump -u username -p autocare360 > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Run Flyway migration
mvn flyway:migrate \
  -Dflyway.url=$DB_URL \
  -Dflyway.user=$DB_USERNAME \
  -Dflyway.password=$DB_PASSWORD
```

### Step 3: Deploy Application

#### Docker Deployment
```bash
# 1. Stop existing container
docker stop autocare360-backend
docker rm autocare360-backend

# 2. Pull latest image
docker pull your-registry/autocare360-backend:latest

# 3. Run new container
docker run -d \
  --name autocare360-backend \
  -p 8080:8080 \
  --env-file .env.prod \
  --restart unless-stopped \
  your-registry/autocare360-backend:latest

# 4. Wait for startup
sleep 30
```

#### Kubernetes Deployment
```bash
# 1. Apply configuration updates
kubectl apply -f kubernetes/configmap.yaml
kubectl apply -f kubernetes/secret.yaml

# 2. Update deployment
kubectl apply -f kubernetes/deployment.yaml

# 3. Wait for rollout
kubectl rollout status deployment/autocare360-backend -n autocare360

# 4. Verify deployment
kubectl get pods -n autocare360
```

### Step 4: Verification
```bash
# 1. Health check
curl -f http://your-domain/actuator/health

# 2. Liveness check
curl -f http://your-domain/actuator/health/liveness

# 3. Readiness check
curl -f http://your-domain/actuator/health/readiness

# 4. Test critical endpoints
curl -X POST http://your-domain/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

### Step 5: Monitoring
```bash
# 1. Check application logs
docker logs autocare360-backend
# OR
kubectl logs -f deployment/autocare360-backend -n autocare360

# 2. Monitor metrics
# Access Grafana dashboard
# Check Prometheus metrics

# 3. Verify alerts are working
# Test alert notifications
```

## ✅ Post-Deployment Checklist

### Immediate (First 15 minutes)
- [ ] Application starts successfully
- [ ] Health checks passing
- [ ] No critical errors in logs
- [ ] Database connections working
- [ ] Authentication working
- [ ] Critical user flows tested
- [ ] Response times acceptable

### Short Term (First hour)
- [ ] Monitor error rates
- [ ] Check resource utilization
- [ ] Verify all features working
- [ ] Test integrations
- [ ] Monitor user activity
- [ ] Check for performance issues

### Extended (First 24 hours)
- [ ] No memory leaks detected
- [ ] Performance stable
- [ ] Error rate within acceptable limits
- [ ] User feedback monitored
- [ ] Backup completed successfully
- [ ] Metrics trending normally

## 🔄 Rollback Procedure

If issues are detected:

### Quick Rollback (Docker)
```bash
# 1. Stop new version
docker stop autocare360-backend
docker rm autocare360-backend

# 2. Run previous version
docker run -d \
  --name autocare360-backend \
  -p 8080:8080 \
  --env-file .env.prod \
  --restart unless-stopped \
  your-registry/autocare360-backend:v0.9.0

# 3. Verify
curl -f http://your-domain/actuator/health
```

### Kubernetes Rollback
```bash
# 1. Rollback deployment
kubectl rollout undo deployment/autocare360-backend -n autocare360

# 2. Check status
kubectl rollout status deployment/autocare360-backend -n autocare360

# 3. Verify
kubectl get pods -n autocare360
```

### Database Rollback
```bash
# Only if database migration needs rollback
mysql -u username -p autocare360 < backup_YYYYMMDD_HHMMSS.sql
```

## 🚨 Emergency Contacts

| Role | Contact | Phone |
|------|---------|-------|
| DevOps Lead | - | - |
| Backend Lead | - | - |
| Database Admin | - | - |
| Security Team | - | - |
| On-Call Engineer | - | - |

## 📊 Success Criteria

Deployment is considered successful when:
- [ ] Application responds within acceptable time (<2s)
- [ ] Error rate < 0.1%
- [ ] No critical errors in logs
- [ ] All health checks passing
- [ ] Resource utilization within limits (CPU <70%, Memory <80%)
- [ ] Database connections stable
- [ ] All critical features working
- [ ] User feedback positive

## 📝 Deployment Notes

**Environment:** _______________

**Date & Time:** _______________

**Deployed By:** _______________

**Version:** _______________

**Issues Encountered:**
- 

**Resolution:**
- 

**Additional Notes:**
- 

---

✅ **Deployment Complete** - Update status in project management tool
