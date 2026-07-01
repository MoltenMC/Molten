# Repository Guidelines

## Project Structure & Module Organization

Molten is a Kotlin multi-module Gradle project. Source code lives under each module’s `src/main/kotlin`, and tests live under `src/test/kotlin`.

- `molten-api`: stable plugin-facing API contracts.
- `molten-common`: protocol-neutral ECS, world, registry, network intent, scheduler, SPI, and utility types.
- `molten-server`: bootstrap, runtime wiring, plugin lifecycle, permissions, console, and server services.
- `molten-java`: Java Edition protocol 776 stack.
- `molten-bedrock`: Bedrock Edition protocol 1001 stack.
- `molten-translator`: Java/Bedrock/internal registry and data translation.

Keep protocol-specific classes out of `molten-common`.

## Build, Test, and Development Commands

- `./gradlew projects`: list all Gradle modules.
- `./gradlew build`: compile, package, and run tests.
- `./gradlew clean build`: rebuild from scratch.
- `./gradlew :molten-common:test`: run tests for one module.
- `./gradlew :molten-server:run`: run the current server bootstrap.

The project targets Java toolchain 25 and uses the Kotlin JVM plugin.

## Coding Style & Naming Conventions

Use Kotlin for implementation code. Prefer small data classes, sealed interfaces, enums, and explicit contracts over broad mutable abstractions. Use 4-space indentation and keep packages under `io.github.moltenmc.molten`.

Naming examples:

- Types: `EntityId`, `RuntimeDefinition`, `JavaPacketRegistry`
- Packages: `io.github.moltenmc.molten.common.ecs`
- Tests: `EntityIdTest`, `SparseSetTest`

## Testing Guidelines

Tests use Kotlin test with JUnit Platform. Add tests with new subsystem behavior, especially in `molten-common`, `molten-api`, and `molten-translator`. Prefer focused unit tests for contracts and storage behavior before adding broader integration tests.

Run all tests with `./gradlew test` or a full verification with `./gradlew clean build`.

## Commit & Pull Request Guidelines

Current history uses Conventional Commit style, for example `chore: initialize project with base Gradle wrapper and configuration files`. Prefer `feat:`, `fix:`, `test:`, `docs:`, `build:`, and `chore:` prefixes.

Pull requests should include a short summary, affected modules, test results, and linked issues when applicable. For architecture changes, explain module-boundary impact explicitly.

## Security & Architecture Notes

Treat client packets as untrusted. Network handlers should validate and convert packets into intents, not mutate gameplay state directly. Use command buffers, message passing, and region-aware scheduling for state changes.
