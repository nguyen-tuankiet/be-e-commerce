# Commission Settings API Refactor - Documentation

## Overview
This document outlines the refactoring of the commission settings API endpoint, including backward compatibility notes and migration guidance.

## API Changes

### 1. PATCH /api/admin/commission (Updated)

#### Previous Implementation
- **Request Fields**: 
  - `fixedCommissionFee` (BigDecimal)
  - `minimumCommissionBalance` (BigDecimal)
  - `autoLockEnabled` (Boolean)
  
- **Response Fields**:
  - `fixedCommissionFee` (BigDecimal)
  - `minimumCommissionBalance` (BigDecimal)
  - `autoLockEnabled` (Boolean)
  - `updatedAt` (LocalDateTime)
  - `updatedBy` (String)

#### New Implementation (v2)
- **Request Fields**:
  - `fixedCommissionFee` (BigDecimal, required) - with validation: @NotNull, @DecimalMin("0")
  - `minimumCommissionBalance` (BigDecimal, required) - with validation: @NotNull, @DecimalMin("0")
  - Removed: `autoLockEnabled` (moved to separate GET endpoint)
  
- **Response Fields**:
  - `fixedCommissionFee` (BigDecimal)
  - `minimumCommissionBalance` (BigDecimal)
  - `updatedAt` (LocalDateTime)
  - `updatedBy` (String)
  - Removed: `autoLockEnabled`

#### Example Request
```json
{
  "fixedCommissionFee": 10000,
  "minimumCommissionBalance": 20000
}
```

#### Example Response
```json
{
  "fixedCommissionFee": 10000,
  "minimumCommissionBalance": 20000,
  "updatedAt": "2026-05-29T12:30:00",
  "updatedBy": "Admin admin@example.com"
}
```

### 2. GET /api/admin/commission-settings (New)

This new endpoint retrieves the complete commission configuration including the auto-lock status.

#### Request
- Method: GET
- No request body required
- Requires: ADMIN role

#### Response Fields
- `fixedCommissionFee` (BigDecimal) - Fixed commission fee value
- `minimumCommissionBalance` (BigDecimal) - Minimum balance threshold
- `autoLockEnabled` (Boolean) - Whether auto-lock is enabled
- `updatedAt` (LocalDateTime) - Last update timestamp

#### Example Response
```json
{
  "fixedCommissionFee": 10000,
  "minimumCommissionBalance": 20000,
  "autoLockEnabled": true,
  "updatedAt": "2026-05-29T12:30:00"
}
```

## Backward Compatibility Notes

### Breaking Changes
1. **Commission Update Endpoint (PATCH /api/admin/commission)**
   - The `autoLockEnabled` field has been removed from the request body
   - The `autoLockEnabled` field has been removed from the response body
   - Clients sending `autoLockEnabled` in the request will receive a validation error

2. **Platform and VAT Configuration**
   - The `platformFeePercent` and `vatPercent` fields are now managed under `/api/admin/settings` (AdminSettings)
   - These fields have been removed from the commission management endpoints

### Migration Guide for Clients

#### For Frontend Applications:
1. **Update PATCH /api/admin/commission requests**:
   - Remove the `autoLockEnabled` field from request payloads
   - Only send `fixedCommissionFee` and `minimumCommissionBalance`

   **Before**:
   ```javascript
   const payload = {
     fixedCommissionFee: 10000,
     minimumCommissionBalance: 20000,
     autoLockEnabled: true  // ❌ Will cause validation error
   };
   ```

   **After**:
   ```javascript
   const payload = {
     fixedCommissionFee: 10000,
     minimumCommissionBalance: 20000
   };
   ```

2. **Update response handling for PATCH /api/admin/commission**:
   - Remove any code that depends on the `autoLockEnabled` field in the response
   - Use the new GET endpoint to retrieve complete settings including auto-lock status

   **Before**:
   ```javascript
   const response = await api.patch('/api/admin/commission', payload);
   const autoLockEnabled = response.data.autoLockEnabled; // ❌ No longer available
   ```

   **After**:
   ```javascript
   // Update commission
   const response = await api.patch('/api/admin/commission', payload);
   
   // Get complete settings including auto-lock from new endpoint
   const settings = await api.get('/api/admin/commission-settings');
   const autoLockEnabled = settings.data.autoLockEnabled;
   ```

3. **Add new endpoint integration**:
   - Call GET `/api/admin/commission-settings` to retrieve complete commission configuration
   - Use this endpoint when you need the `autoLockEnabled` status

#### For Backend/API Services:
1. **Server-side validation updates**:
   - Remove any DTOs or models expecting `autoLockEnabled` in PATCH requests
   - Update response parsing to not expect `autoLockEnabled` in PATCH responses
   - Implement calls to the new GET endpoint when full config is needed

2. **Database/Cache updates**:
   - The system still persists `autoLockEnabled` in the database
   - It's now retrieved separately via the new GET endpoint
   - No data migration required; existing settings are preserved

## Implementation Details

### Files Modified

1. **DTOs**:
   - `CommissionUpdateRequest.java` - Removed `autoLockEnabled` field, added validation messages
   - `CommissionResponse.java` - Removed `autoLockEnabled` field, added JavaDoc with backward compatibility notes
   - `CommissionSettingsResponse.java` - New DTO for GET endpoint response

2. **Service Layer**:
   - `AdminService.java` - Added `getCommissionSettings()` method signature
   - `AdminServiceImpl.java` - Updated `updateCommission()` implementation, added `getCommissionSettings()` implementation

3. **Controller**:
   - `AdminController.java` - Updated PATCH endpoint documentation, added new GET endpoint with Swagger documentation templates

### Validation Annotations

Both request fields in `CommissionUpdateRequest` now include:
- `@NotNull` - Ensures the field is present
- `@DecimalMin("0")` - Ensures the value is non-negative
- Custom validation messages for better error reporting

### Swagger/OpenAPI Documentation

Swagger annotations have been added (commented out) in the controller. To enable Swagger UI:

1. Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.0.2</version>
</dependency>
```

2. Uncomment the `@Operation` and `@ApiResponse` annotations in `AdminController.java`

3. After application restart, access Swagger UI at: `http://localhost:8080/swagger-ui.html`

## Testing Guide

### Manual Testing with cURL

1. **Update Commission Settings**:
```bash
curl -X PATCH http://localhost:8080/api/admin/commission \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "fixedCommissionFee": 10000,
    "minimumCommissionBalance": 20000
  }'
```

2. **Get Commission Settings**:
```bash
curl -X GET http://localhost:8080/api/admin/commission-settings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

### Test Cases

1. **Valid commission update**:
   - Send valid request with positive decimal values
   - Verify response includes `updatedAt` and `updatedBy`
   - Verify `autoLockEnabled` is NOT in response

2. **Invalid request - missing required fields**:
   - Omit `fixedCommissionFee` or `minimumCommissionBalance`
   - Should return 400 validation error

3. **Invalid request - negative values**:
   - Send negative values for fee or balance
   - Should return 400 validation error

4. **Invalid request - including autoLockEnabled**:
   - Send request with `autoLockEnabled` field
   - Should return 400 validation error (field not recognized)

5. **Get commission settings**:
   - Call GET endpoint
   - Verify response includes all four fields: `fixedCommissionFee`, `minimumCommissionBalance`, `autoLockEnabled`, `updatedAt`

## Database Impact

No database schema changes required. The system continues to store:
- `fixed_commission_fee` in `system_setting` table
- `minimum_commission_balance` in `system_setting` table
- `auto_lock_enabled` in `system_setting` table

These values persist and are used by other services without modification.

## Deprecation Timeline

- **Effective**: Immediately upon deployment
- **Deprecation Period**: 3 months
- **Sunset Date**: 3 months after deployment
- During deprecation period, requests with `autoLockEnabled` in PATCH payload will return 400 validation error

## Support and Questions

For questions regarding this API refactor:
1. Refer to the AdminController class documentation
2. Check CommissionUpdateRequest and CommissionSettingsResponse DTOs
3. Review AdminServiceImpl implementation for business logic details
4. Contact the API team for clarification
