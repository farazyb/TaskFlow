# TF-20 Task Plan - JWT Login and Refresh Token Logic

## Scope and Goals
- [x] Confirm endpoint contract for authentication:
- [x] `POST /api/v1/auth/login` returns access token in JSON response body.
- [x] `POST /api/v1/auth/refresh` reads refresh token from HttpOnly cookie and returns new access token.
- [x] Keep architecture aligned with project decisions:
- [x] Access token stays in frontend memory (response payload only).
- [x] Refresh token stays in HttpOnly cookie (not exposed to JavaScript).
- [x] JWT must use HS256 with runtime-injected secret.

## Security and Token Design
- [x] Define token claims and metadata:
- [x] `sub` = user ID.
- [x] `email` claim = normalized user email.
- [x] `type` claim = `access` or `refresh`.
- [x] `iat` and `exp` included for both tokens.
- [x] Define lifetimes:
- [x] Access token short TTL (minutes).
- [x] Refresh token longer TTL (days).
- [x] Define refresh cookie attributes:
- [x] `HttpOnly=true`.
- [x] `Path=/api/v1/auth/refresh`.
- [x] `SameSite` explicit value.
- [x] `Secure` configurable for local vs production.
- [x] `Max-Age` matches refresh token expiration.

## Backend Implementation Checklist
- [x] Add JWT dependencies in `backend/pom.xml` for:
- [x] token creation/signing.
- [x] token parsing/verification.
- [x] JSON serialization module.
- [x] Add auth configuration properties class:
- [x] secret.
- [x] access TTL.
- [x] refresh TTL.
- [x] cookie options.
- [x] Add JWT service component:
- [x] generate access token.
- [x] generate refresh token.
- [x] parse and validate signed token.
- [x] verify token type for refresh flow.
- [x] normalize and centralize JWT parsing exceptions.
- [x] Add login request/response DTOs:
- [x] login request validation (`email`, `password`).
- [x] token response DTO with access token + token metadata.
- [x] Add authentication service:
- [x] load user by normalized email.
- [x] verify password with existing `PasswordEncoder`.
- [x] throw dedicated exception on invalid credentials.
- [x] issue access + refresh tokens on success.
- [x] Add/extend auth controller:
- [x] implement `POST /login`.
- [x] implement `POST /refresh`.
- [x] write refresh cookie in response headers.
- [x] on refresh, rotate refresh token and return new access token.
- [x] Update security config:
- [x] allow anonymous access to `register`, `login`, `refresh`.
- [x] keep other endpoints ready for next phase (TF-21 filter work).
- [x] Update global exception handling:
- [x] map invalid credentials to `401`.
- [x] map invalid/expired refresh token to `401`.
- [x] keep response shape consistent with `ApiErrorResponse`.

## Testing and Coverage Checklist
- [x] Unit tests for JWT service:
- [x] access token contains expected claims.
- [x] refresh token contains expected `type=refresh`.
- [x] expired token and invalid signature paths.
- [x] Unit tests for authentication service:
- [x] success path (correct password).
- [x] failure path (unknown email).
- [x] failure path (wrong password).
- [x] Integration tests for auth endpoints:
- [x] `/login` returns `200` and sets refresh cookie.
- [x] `/login` returns `401` with invalid credentials.
- [x] `/refresh` with valid cookie returns new access token and rotated refresh cookie.
- [x] `/refresh` without cookie returns `401`.
- [x] `/refresh` with malformed/expired token returns `401`.
- [x] Run test commands and capture results:
- [x] targeted tests for quick feedback.
- [x] full test suite to detect regressions.
- [x] generate coverage report and verify >= 80%.

## Validation and Done Criteria
- [x] Build passes (`mvn test` or equivalent CI command).
- [x] Coverage report generated and coverage >= 80%.
- [x] No plaintext secrets hardcoded in code or tests.
- [x] API behavior documented via tests and response assertions.
- [x] Ready for TF-21 integration (JWT auth filter consuming access token).
