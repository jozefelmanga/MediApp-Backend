-- ==========================================
-- Database Reset Script
-- ==========================================
-- Run this script to drop all existing MediApp databases
-- and let the applications recreate them with the fixed schemas
-- ==========================================

-- WARNING: This will DELETE ALL DATA in these databases!
-- Make sure to backup any important data before running this script.

DROP DATABASE IF EXISTS mediapp_user;
DROP DATABASE IF EXISTS mediapp_security;
DROP DATABASE IF EXISTS mediapp_doctor;
DROP DATABASE IF EXISTS mediapp_booking;

-- The applications will automatically recreate these databases
-- on next startup due to the createDatabaseIfNotExist=true parameter
-- in their datasource URLs.

-- To verify the databases were dropped, run:
-- SHOW DATABASES;
