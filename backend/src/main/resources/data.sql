-- TestPilot AI Demo Data
-- ShopSphere Demo Project

-- Insert demo user (password: admin123)
INSERT INTO users (username, password, email, display_name, enabled, created_at, updated_at)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@testpilot.com', 'Admin', true, NOW(), NOW());

-- Insert demo project
INSERT INTO test_projects (name, description, user_id, base_url, active, created_at, updated_at)
VALUES ('ShopSphere', 'Demo e-commerce system for testing', 1, 'http://localhost:3001', true, NOW(), NOW());

-- Insert requirements
INSERT INTO requirements (project_id, title, description, source, status, risk_level, coverage_score, created_at, updated_at)
VALUES (1, 'User Login', 'User authentication with username and password. Account locks after 5 failed attempts for 30 minutes.', 'MANUAL', 'ACTIVE', 'HIGH', 85.0, NOW(), NOW());

INSERT INTO requirements (project_id, title, description, source, status, risk_level, coverage_score, created_at, updated_at)
VALUES (1, 'Product Search', 'Search products by name, category, price range with pagination.', 'MANUAL', 'ACTIVE', 'MEDIUM', 70.0, NOW(), NOW());

INSERT INTO requirements (project_id, title, description, source, status, risk_level, coverage_score, created_at, updated_at)
VALUES (1, 'Shopping Cart', 'Add/remove/update items, apply coupons, calculate totals.', 'MANUAL', 'ACTIVE', 'HIGH', 60.0, NOW(), NOW());

-- Insert requirement rules
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R1', 'User must provide username', true, 3, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R2', 'User must provide password', true, 3, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R3', 'Correct credentials allow login', true, 4, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R4', 'Wrong password increments error count', true, 2, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R5', '5th wrong password triggers account lock', true, 2, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R6', 'Locked account rejects login', true, 2, NOW());
INSERT INTO requirement_rules (requirement_id, rule_code, rule_description, covered, coverage_count, created_at)
VALUES (1, 'R7', 'Account auto-unlocks after 30 minutes', false, 0, NOW());

-- Insert test scenarios
INSERT INTO test_scenarios (project_id, requirement_id, title, description, test_type, risk_level, priority, created_at, updated_at)
VALUES (1, 1, 'Normal Login Flow', 'Test successful login with valid credentials', 'API', 'HIGH', 1, NOW(), NOW());

INSERT INTO test_scenarios (project_id, requirement_id, title, description, test_type, risk_level, priority, created_at, updated_at)
VALUES (1, 1, 'Invalid Login Attempts', 'Test login with wrong password, empty fields, SQL injection', 'API', 'HIGH', 2, NOW(), NOW());

INSERT INTO test_scenarios (project_id, requirement_id, title, description, test_type, risk_level, priority, created_at, updated_at)
VALUES (1, 1, 'Account Locking', 'Test account locks after 5 failed attempts', 'API', 'CRITICAL', 1, NOW(), NOW());

INSERT INTO test_scenarios (project_id, requirement_id, title, description, test_type, risk_level, priority, created_at, updated_at)
VALUES (1, 1, 'Security Tests', 'Test SQL injection, XSS, brute force protection', 'API', 'HIGH', 2, NOW(), NOW());

-- Insert test cases
INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 1, 'TC_LOGIN_001', 'Valid Login', 'Login with correct username and password', 'User exists in database', '1. Send POST /api/auth/login with valid credentials', '{"status": 200, "token": "not_null", "user": "not_null"}', '{"username": "testuser", "password": "Test@123"}', 'API', 'EQUIVALENCE_PARTITION', 1, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 1, 'TC_LOGIN_002', 'Empty Username', 'Login with empty username', 'None', '1. Send POST /api/auth/login with empty username', '{"status": 400, "error": "Username required"}', '{"username": "", "password": "Test@123"}', 'API', 'BOUNDARY_VALUE', 2, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 1, 'TC_LOGIN_003', 'Empty Password', 'Login with empty password', 'None', '1. Send POST /api/auth/login with empty password', '{"status": 400, "error": "Password required"}', '{"username": "testuser", "password": ""}', 'API', 'BOUNDARY_VALUE', 2, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 2, 'TC_LOGIN_004', 'Wrong Password', 'Login with incorrect password', 'User exists', '1. Send POST /api/auth/login with wrong password', '{"status": 401, "error": "Invalid credentials"}', '{"username": "testuser", "password": "wrongpass"}', 'API', 'ERROR_GUESSING', 1, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 3, 'TC_LOGIN_010', 'Account Lock After 5 Failures', 'Account should lock after 5 consecutive failed login attempts', 'User account is active', '1. Login with wrong password 5 times', '{"status": 423, "error": "Account locked"}', '{"username": "testuser", "password": "wrongpass", "attempts": 5}', 'API', 'STATE_TRANSITION', 1, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 3, 'TC_LOGIN_011', 'Login During Lock Period', 'Login should fail when account is locked', 'Account is locked', '1. Login with correct credentials during lock period', '{"status": 423, "error": "Account locked"}', '{"username": "testuser", "password": "Test@123"}', 'API', 'STATE_TRANSITION', 1, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 4, 'TC_LOGIN_020', 'SQL Injection in Username', 'Test SQL injection vulnerability', 'None', '1. Send POST with SQL injection payload', '{"status": 400, "error": "Invalid input"}', '{"username": \"admin'' OR 1=1--\", "password": "any"}', 'API', 'ERROR_GUESSING', 1, true, true, NOW(), NOW());

INSERT INTO test_cases (project_id, scenario_id, case_code, title, description, precondition, steps, expected_result, test_data, test_type, strategy, priority, automated, ai_generated, created_at, updated_at)
VALUES (1, 4, 'TC_LOGIN_021', 'XSS in Username', 'Test XSS vulnerability in login', 'None', '1. Send POST with XSS payload', '{"status": 400, "error": "Invalid input"}', '{"username": "<script>alert(1)</script>", "password": "any"}', 'API', 'ERROR_GUESSING', 2, true, true, NOW(), NOW());

-- Insert agent task
INSERT INTO agent_tasks (project_id, user_id, goal, context, status, plan_json, result_summary, current_step_index, total_steps, max_steps, max_retry, current_retry, total_tokens, total_latency_ms, waiting_for_user, created_at, updated_at)
VALUES (1, 1, '全面测试用户登录接口', 'Test the POST /api/auth/login endpoint comprehensively including normal flow, error handling, boundary values, and security', 'COMPLETED',
 '{"strategy":"Comprehensive API testing using equivalence partitioning, boundary value analysis, state transition, and error guessing","scenarios":[{"title":"Normal Login","testType":"API"},{"title":"Invalid Login","testType":"API"},{"title":"Account Locking","testType":"API"},{"title":"Security Tests","testType":"API"}],"steps":[{"stepName":"Analyze OpenAPI","stepType":"analyze"},{"stepName":"Generate Test Cases","stepType":"plan"},{"stepName":"Execute HTTP Tests","stepType":"execute","tool":"executeHttpRequest"},{"stepName":"Analyze Results","stepType":"assert"}]}',
 'Completed 8 steps. Passed: 6, Failed: 2', 7, 8, 50, 3, 0, 15420, 12500, false, NOW(), NOW());
