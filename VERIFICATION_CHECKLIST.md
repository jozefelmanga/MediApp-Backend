# Database Schema Verification Checklist

## Pre-Flight Checks

Run these checks to verify the schema fixes are correct:

### ✅ 1. User Service Schema Check

**File**: `user-service/src/main/resources/db/V1__create_user_schema.sql`

- [x] `user_id` is `BIGINT AUTO_INCREMENT` (not BINARY(16))
- [x] `auth_user_id` column exists
- [x] `password_hash` column DOES NOT exist
- [x] `patient_profile.patient_id` is `BIGINT` (not AUTO_INCREMENT - shares with user_id)
- [x] Foreign key from `patient_id` to `app_user.user_id`

### ✅ 2. Doctor Service Schema Check

**File**: `doctor-service/src/main/resources/db/schema.sql`

- [x] `doctor_id` is `BIGINT AUTO_INCREMENT` (not CHAR(36))
- [x] `user_id` column exists in `doctor_profile`
- [x] `slot_id` is `BIGINT AUTO_INCREMENT` (not CHAR(36))
- [x] Index on `user_id`

**File**: `doctor-service/src/main/resources/db/migration/V1__init_schema.sql`

- [x] Same as above (migration file matches schema.sql)

### ✅ 3. Entity-Schema Alignment

| Entity | Column | Type | Schema Type | Match? |
|--------|--------|------|-------------|--------|
| AppUser (user) | id | Long | BIGINT AUTO_INCREMENT | ✅ |
| AppUser (security) | id | Long | BIGINT AUTO_INCREMENT | ✅ |
| DoctorProfileEntity | id | Long | BIGINT AUTO_INCREMENT | ✅ |
| DoctorProfileEntity | userId | Long | BIGINT | ✅ |
| AvailabilitySlotEntity | id | Long | BIGINT AUTO_INCREMENT | ✅ |
| PatientProfile | id | Long | BIGINT | ✅ |
| Appointment | appointmentId | Long | BIGINT AUTO_INCREMENT | ✅ |

---

## Deployment Steps

### Step 1: Stop All Services

```bash
# If using bash script
./stop-all-services.sh

# Or Windows batch
stop-all-services.bat

# Or manually stop each service
```

### Step 2: Reset Databases

```bash
# Connect to MySQL
mysql -u root -p

# Run the reset script
source reset-databases.sql

# Or manually:
# DROP DATABASE IF EXISTS mediapp_user;
# DROP DATABASE IF EXISTS mediapp_security;
# DROP DATABASE IF EXISTS mediapp_doctor;
# DROP DATABASE IF EXISTS mediapp_booking;

# Verify databases are dropped
SHOW DATABASES;
# Should NOT show mediapp_* databases

# Exit MySQL
exit
```

### Step 3: Start Services in Order

```bash
# 1. Discovery Server
cd discovery-server
mvn spring-boot:run

# Wait for it to start (check http://localhost:8761)

# 2. Security Service
cd security-service
mvn spring-boot:run

# 3. User Service
cd user-service
mvn spring-boot:run

# 4. Doctor Service
cd doctor-service
mvn spring-boot:run

# 5. Booking Service
cd booking-service
mvn spring-boot:run

# 6. Gateway Service (if needed)
cd gateway-service
mvn spring-boot:run
```

**Or use the startup script**:
```bash
# Windows
start-all-services.bat

# Linux/Mac
./start-all-services.sh
```

### Step 4: Verify Database Creation

```bash
mysql -u root -p

SHOW DATABASES;
# Should now show:
# - mediapp_user
# - mediapp_security
# - mediapp_doctor
# - mediapp_booking

USE mediapp_user;
DESCRIBE app_user;
# Verify: user_id is BIGINT, auth_user_id exists, NO password_hash

DESCRIBE patient_profile;
# Verify: patient_id is BIGINT (not auto_increment)

USE mediapp_doctor;
DESCRIBE doctor_profile;
# Verify: doctor_id is BIGINT, user_id exists

DESCRIBE availability_slot;
# Verify: slot_id is BIGINT, doctor_id is BIGINT

exit
```

---

## Functional Testing

### Test 1: Patient Registration

```bash
# Via gateway or directly to user-service
curl -X POST http://localhost:8081/api/users/patients \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@test.com",
    "password": "testpass123",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+1234567890",
    "dateOfBirth": "1990-01-15"
  }'
```

**Expected Result**: 
- ✅ Status 200/201
- ✅ User created in security-service (mediapp_security.app_users)
- ✅ User profile created in user-service (mediapp_user.app_user)
- ✅ Patient profile created (mediapp_user.patient_profile)
- ✅ IDs are proper BIGINT values
- ✅ auth_user_id in app_user matches security-service id

### Test 2: Doctor Registration

```bash
# Need admin token from application.properties
curl -X POST http://localhost:8081/api/users/doctors \
  -H "Content-Type: application/json" \
  -H "X-Admin-Token: change-me" \
  -d '{
    "email": "doctor@test.com",
    "password": "testpass123",
    "firstName": "Jane",
    "lastName": "Smith",
    "medicalLicenseNumber": "MD123456",
    "specialtyId": 1,
    "officeAddress": "123 Medical Plaza"
  }'
```

**Expected Result**:
- ✅ Status 200/201
- ✅ User created in security-service
- ✅ User profile created in user-service
- ✅ Doctor profile created in doctor-service (mediapp_doctor.doctor_profile)
- ✅ user_id in doctor_profile matches app_user.user_id
- ✅ All IDs are BIGINT

### Test 3: Login

```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@test.com",
    "password": "testpass123"
  }'
```

**Expected Result**:
- ✅ Status 200
- ✅ Returns JWT token
- ✅ Token contains user ID

### Test 4: Get User Profile

```bash
# Get user ID from registration/login response
curl -X GET http://localhost:8081/api/users/{userId} \
  -H "Authorization: Bearer {jwt_token}"
```

**Expected Result**:
- ✅ Status 200
- ✅ Returns user details with patient/doctor profile
- ✅ Email matches
- ✅ IDs are consistent

---

## Known Remaining Issues

### ⚠️ Distributed Transaction Problem

The registration flow is still **not atomic** across services:

```java
// In UserManagementService.registerDoctor()
// Step 1: Security service (separate transaction)
authResponse = securityServiceClient.registerUser(...)

// Step 2: User service (separate transaction)
user = persistUser(authResponse.authUserId(), ...)

// Step 3: Doctor service (separate transaction)  
doctorProfile = doctorServiceClient.createDoctorProfileSync(...)
```

**Problem**: If step 2 or 3 fails, you have orphaned data in security-service.

**Solutions** (to implement later):
1. **Saga Pattern**: Implement compensating transactions
2. **Outbox Pattern**: Use event-driven eventual consistency
3. **Idempotency**: Make all operations idempotent with retry logic
4. **Health Checks**: Add rollback logic in catch blocks

---

## Success Criteria

All tests pass and:
- ✅ No schema-entity type mismatches
- ✅ No missing columns
- ✅ Passwords only in security-service
- ✅ Consistent Long IDs across all services
- ✅ Login works
- ✅ Profile retrieval works
- ✅ Registration creates records in all appropriate services
- ✅ Foreign key relationships maintained

---

## If Something Goes Wrong

### Issue: "Column 'auth_user_id' not found"
**Solution**: Database wasn't dropped/recreated. Run `reset-databases.sql` again.

### Issue: "Data truncation" or "Incorrect integer value for column 'user_id'"
**Solution**: Old data with BINARY(16) still exists. Drop and recreate databases.

### Issue: Registration succeeds but profile query fails
**Solution**: Check that auth_user_id is properly set and matches security-service ID.

### Issue: Doctor service can't find user
**Solution**: Verify user_id column exists in doctor_profile table.

---

## Next Steps After Verification

1. [ ] Implement proper distributed transaction handling
2. [ ] Add compensating transactions for failed registrations
3. [ ] Change `ddl-auto` from `update` to `validate` in production
4. [ ] Add integration tests for cross-service flows
5. [ ] Implement health checks and circuit breakers
6. [ ] Add database migration version control
