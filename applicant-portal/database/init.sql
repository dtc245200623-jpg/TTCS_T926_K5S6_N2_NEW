CREATE DATABASE IF NOT EXISTS applicant_portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE applicant_portal;

CREATE TABLE IF NOT EXISTS applications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tracking_code VARCHAR(32) NOT NULL UNIQUE,
  full_name VARCHAR(150) NOT NULL,
  email VARCHAR(254) NOT NULL,
  phone VARCHAR(40) NOT NULL,
  source VARCHAR(100) NOT NULL,
  introduction TEXT NOT NULL,
  position VARCHAR(150) NOT NULL,
  cv_original_name VARCHAR(255) NOT NULL,
  cv_stored_name VARCHAR(255) NOT NULL,
  cv_mime_type VARCHAR(120) NOT NULL,
  cv_size_bytes INT UNSIGNED NOT NULL,
  ip_hash CHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_application_email_position (email, position),
  KEY idx_application_created_at (created_at),
  KEY idx_application_ip_created (ip_hash, created_at)
) ENGINE=InnoDB;
