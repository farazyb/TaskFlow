# TF-21 Task Plan - JwtAuthFilter for Secured Endpoints

## Scope and Goal
- [x] Confirm target behavior for this phase:
- [x] Access token from `Authorization: Bearer <token>` authenticates request user.
- [x] Public routes stay open: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `GET /actuator/health`.
- [x] All non-public routes require a valid access token.
- [x] Keep architecture aligned with prior decision: JWT HS256 secret from Vault/runtime config.

## Filter Design Checklist
- [x] Create `JwtAuthFilter` extending `OncePerRequestFilter`.
- [x] Read and validate `Authorization` header format safely.
- [x] Skip filter auth setup when header is missing or not `Bearer`.
- [x] Parse token with `JwtService` using access-token-specific validation.
- [x] Build `Authentication` object with principal data (`userId`, `email`).
- [x] Set `SecurityContextHolder` only on successful token validation.
- [x] Ensure invalid/expired/malformed access token results in `401` via auth entrypoint.
- [x] Prevent filter from overriding an already-authenticated security context.

## JWT Service Updates
- [x] Add dedicated access-token parsing method (separate from refresh parsing).
- [x] Enforce `type=access` claim for secured endpoint authentication.
- [x] Reuse centralized JWT parsing/claim validation logic to avoid duplication.
- [x] Keep refresh behavior unchanged and backward-compatible.

## Security Configuration Updates
- [x] Register `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`.
- [x] Keep `csrf` disabled for stateless API.
- [x] Set stateless session policy.
- [x] Replace `anyRequest().permitAll()` with `anyRequest().authenticated()`.
- [x] Keep explicit `permitAll` matchers for auth endpoints and health.
- [x] Configure consistent `401` JSON response for unauthenticated/invalid token requests.

## Test Plan
- [x] Unit tests for `JwtService` access parsing:
- [x] Valid access token returns principal.
- [x] Refresh token rejected by access parser.
- [x] Expired/invalid signature token rejected.
- [x] Filter unit tests:
- [x] No header keeps request unauthenticated.
- [x] Invalid token keeps request unauthenticated.
- [x] Valid token populates security context.
- [x] Existing authentication is not overridden.
- [x] Validate response shape for unauthorized errors (consistent API error contract if configured).

## Done Criteria
- [ ] `mvn test` passes.
- [ ] Existing TF-20 auth tests remain green (no refresh/login regression).
- [x] No hardcoded secrets added.
- [x] Security behavior documented through tests for future tasks (TF-22+).
