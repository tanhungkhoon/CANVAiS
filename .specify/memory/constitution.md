<!--
Sync Impact Report:
- Version change: Unversioned Template → 1.0.0
- Principles established:
  * I. Rigorous Code Quality & Clean Architecture
  * II. Comprehensive Testing & Test-First Verification
  * III. Comprehensive & Living Documentation
  * IV. Idiomatic Gradle & Groovy Tooling
  * V. Context-Aware Extensibility & Modular Architecture
- Added sections:
  * Architecture & Technology Standards
  * Quality Gates & Review Workflow
  * Governance
- Removed sections: None (template placeholders replaced)
- Deferred items: None
-->

# CANVAiS Constitution

## Core Principles

### I. Rigorous Code Quality & Clean Architecture
Every module and component MUST uphold clean architecture with explicit separation of concerns (presentation, application services, domain models, and infrastructure/data access).
- Classes MUST adhere to SOLID design principles, maintain high cohesion, and minimize cyclomatic complexity.
- Production code MUST pass all static analysis checks (e.g., Checkstyle, SpotBugs, CodeNarc) with zero critical or blocker violations.
- Code smells, premature abstractions, and dead code are strictly prohibited; maintainability, self-documenting naming, and readability MUST take precedence over clever or obscure constructs.

### II. Comprehensive Testing & Test-First Verification
Testing is non-negotiable and foundational to the software lifecycle.
- All business logic MUST be verified through unit, slice, and integration tests before merging. Test-driven verification (TDD / BDD) is strongly required.
- Groovy and Spock Framework (alongside standard Spring Boot Test slices such as `@WebMvcTest` and `@DataJpaTest`) MUST be leveraged for readable, expressive, behavioral specifications using `given-when-then` blocks.
- Code coverage MUST maintain a minimum threshold of 80% line and branch coverage as measured by JaCoCo. Tests MUST be deterministic, isolated from external network dependencies (via testcontainers or mocks), and execute rapidly within the CI pipeline.

### III. Comprehensive & Living Documentation
Every codebase artifact MUST be completely and accurately documented to preserve domain knowledge and ensure seamless maintainability.
- All public and protected Java/Groovy classes, interfaces, records, and methods MUST have thorough Javadoc/GroovyDoc explaining their contract, parameter semantics, return values, exception scenarios, and side effects.
- REST and external interfaces MUST be fully documented with OpenAPI/Swagger specifications, including schema models, HTTP response codes, and descriptive usage examples.
- Non-obvious design decisions, architectural trade-offs, and complex algorithms MUST be documented inline or via Architectural Decision Records (ADRs) within the repository.

### IV. Idiomatic Gradle & Groovy Tooling
The build system is a first-class citizen and MUST remain reproducible, performant, and standardized.
- The project MUST utilize Gradle with the official Groovy plugin for compilation, testing, and task execution.
- Dependency management MUST be centralized, strictly versioned, and free of circular or transitive version conflicts.
- Build scripts, Groovy test specifications, and utility scripts MUST follow standard Groovy idioms and adhere to CodeNarc linting rules.

### V. Context-Aware Extensibility & Modular Architecture
CANVAiS architecture MUST preserve loose coupling between core geospatial plotting mechanisms, vibe/natural-intent processing, and external extension points.
- Integration points—including AI skill plugins, MCP (Model Context Protocol) servers, and custom map layers—MUST be implemented behind well-defined interfaces and adapters.
- Extensions MUST NOT pollute the core domain layer or introduce unhandled failure cascades; resilient error handling, input validation, and structured logging (SLF4J) are mandatory across all boundary interfaces.

## Architecture & Technology Standards

The technical stack and system constraints for CANVAiS are defined as follows:
- **Core Platform**: Java 21 LTS (or compatible modern LTS release) with Spring Boot 3.x.
- **Build & Scripting**: Gradle wrapper (`gradlew`) utilizing Groovy DSL/plugin for build configuration and test harnesses.
- **Testing Stack**: Spock Framework / Groovy Specifications, JUnit 5, Mockito, and Spring Boot Test starters, integrated with JaCoCo for coverage reporting.
- **Observability & Logging**: Structured logging via SLF4J and Logback; meaningful contextual diagnostics MUST be included without logging sensitive data.
- **Extensibility**: Plugin and adapter models supporting MCP servers and layer customization hooks with strict schema validation.

## Quality Gates & Review Workflow

All contributions MUST satisfy strict quality gates before being accepted into the codebase:
- **Automated CI Validation**: Every Pull Request MUST automatically pass `./gradlew check` (including compilation, CodeNarc/Checkstyle linting, unit tests, integration tests, and JaCoCo coverage thresholds).
- **Documentation Verification**: Any PR adding or altering public APIs, domain logic, or configuration MUST include matching Javadoc/GroovyDoc and updated documentation. PRs missing documentation MUST be blocked.
- **Peer Code Review**: At least one maintainer approval is required. Reviewers MUST verify architecture alignment, test adequacy, code readability, and documentation completeness.
- **Trunk Protection**: Direct commits to `main` (or protected default branch) are prohibited; all changes land via reviewed and passing PRs.

## Governance

This Constitution represents the authoritative agreement on engineering standards and architectural integrity for the CANVAiS project.
- **Precedence**: This document supersedes informal practices, unwritten agreements, and legacy code patterns. In the event of conflicting guidelines, the Constitution governs.
- **Amendments**: Modifications to this Constitution require an explicit RFC/PR with clear rationale, team consensus, and an updated version number.
- **Compliance**: All team members and automated agentic workflows MUST enforce these rules during specification, planning, and implementation.

**Version**: 1.0.0 | **Ratified**: 2026-11-02 | **Last Amended**: 2026-11-02
