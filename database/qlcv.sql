CREATE DATABASE IF NOT EXISTS qlcv
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qlcv;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(60) NOT NULL,
    fullname VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    user_id INT NOT NULL,
    CONSTRAINT uq_category_user UNIQUE (category_name, user_id),
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    deadline DATE NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    category_id INT NULL,
    user_id INT NOT NULL,
    CONSTRAINT fk_task_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task_user (user_id),
    INDEX idx_task_deadline (deadline),
    INDEX idx_task_status (status)
);
