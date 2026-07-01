# Molten

Molten is an enterprise-grade hybrid Minecraft server engine designed around a shared ECS game core and independent Java Edition and Bedrock Edition protocol stacks.

## Targets

- Java Edition protocol 776.
- Bedrock Edition protocol 1001.
- Kotlin implementation targeting Java language/toolchain level 25.
- Multi-module Gradle architecture with platform-independent gameplay logic in `molten-common`.

## Modules

- `molten-common`: ECS, registry, scheduler, memory, math, serialization, and shared world model foundations.
- `molten-api`: public plugin-facing API.
- `molten-server`: server bootstrap, lifecycle, runtime module loading, configuration, and plugin loading entry points.
- `molten-java`: Java Edition protocol pipeline and adapters.
- `molten-bedrock`: Bedrock Edition protocol pipeline and adapters.
- `molten-translator`: registry and protocol translation layer.

## Architecture Rules

- Core gameplay remains protocol-independent.
- Protocol implementations depend inward on API/common contracts.
- Platform storage and transport implementations are replaceable behind SPI-style interfaces.
- ECS structural mutation is deferred through command buffers.

## Runtime Modes

- `JAVA_BASED`: Java semantics with Java and Bedrock clients enabled, Anvil primary storage.
- `JAVA_ONLY`: Java clients only, Anvil primary storage.
- `BEDROCK_BASED`: Bedrock semantics with Java and Bedrock clients enabled, LevelDB primary storage.
- `BEDROCK_ONLY`: Bedrock clients only, LevelDB primary storage.

## ECS Direction

- Entity IDs use a 64-bit layout: 32-bit index, 24-bit generation, 8-bit kind.
- Stable components are modeled for archetype storage.
- Volatile/tag-like components are modeled for sparse-set storage.
- Structural mutation flows through command buffers and deterministic flush points.
- Systems declare read/write access through descriptors so the scheduler can reason about safe parallel execution.

## Network Direction

- Java Edition and Bedrock Edition own independent packet registries, codec contracts, session states, and connection flows.
- Network handlers convert validated packets into platform-neutral server intents; gameplay mutation remains inside simulation regions.
- High-frequency packet paths can use lazy packet views while low-frequency packets can decode into strong packet objects.
- Registry and translation logic maps Java and Bedrock protocol data into platform-neutral Molten models before common gameplay code sees it.
- Network traffic is treated as untrusted and checked through explicit packet validation, rate limit, size limit, and malformed-packet policies.

## API And Plugins

- `molten-api` exposes stable high-level plugin APIs for players, worlds, entities, events, commands, permissions, scheduling, components, and systems.
- Advanced extension points live in `molten-common` SPI contracts for protocol adapters, packet codecs, registries, storage, translators, permissions, scheduler observation, and metrics export.
- Plugins use `molten-plugin.yml` metadata and an isolated-classloader model with shared API contracts.
- State-changing events are synchronous and region-bound; observational events may run asynchronously.
- Commands register once against the Molten command tree and can be exported to Java Brigadier or Bedrock command data.

## Engineering Gates

- GitHub Actions runs `./gradlew --no-configuration-cache clean build` on pull requests and `main`.
- Kotlin tests are enabled across modules.
- Initial unit tests cover entity id packing, sparse-set behavior, and permission resolution.
- Performance and roadmap enums capture the planned benchmark, metrics, and expansion surfaces without leaking implementation dependencies.
