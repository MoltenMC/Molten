# Molten Roadmap

Molten is a Kotlin, Java 25, multi-module Minecraft server engine targeting a shared internal game model with independent Java and Bedrock protocol stacks.

## Current Foundation

- Gradle Kotlin DSL multi-module layout is established.
- Core modules exist: `molten-api`, `molten-common`, `molten-server`, `molten-java`, `molten-bedrock`, and `molten-translator`.
- Java Edition protocol 776 has early handshake, status, login, configuration, play-entry, disconnect, and system chat packet support.
- Common `ChatComponent` and outbound message models are available.
- Java sessions now own an outbound queue that can flush queued messages after PLAY entry.

## Phase 1: Java Play Session Loop

- Add a repeatable tick/flush hook for PLAY sessions after initial join.
- Route command feedback and server messages through `OutboundMessage`.
- Add basic inbound PLAY packet handling for movement and chat intents.
- Add session lifecycle cleanup for disconnect and channel close events.

## Phase 2: Internal Intent Routing

- Connect Java packet handlers to common `ServerIntent` models.
- Add region-aware routing placeholders for player movement, chat, command, and disconnect intents.
- Ensure network IO handlers only enqueue intents and never mutate gameplay state directly.

## Phase 3: World & Entity Bootstrap

- Create a minimal world bootstrap path in `molten-server`.
- Attach a player ECS entity during Java PLAY entry.
- Introduce basic chunk view state and placeholder chunk streaming contracts.
- Keep storage adapters isolated in `molten-java` and `molten-bedrock`.

## Phase 4: Command Pipeline

- Define common command execution result models.
- Convert command results into `OutboundMessage.CommandFeedback`.
- Add permission checks through the existing permission service contracts.
- Prepare Java Brigadier export after internal command tree contracts stabilize.

## Phase 5: Bedrock Protocol Foundation

- Mirror Java session concepts for Bedrock sessions.
- Add RakNetty listener skeleton and Bedrock packet registry contracts.
- Keep Bedrock packets isolated inside `molten-bedrock`.
- Convert Bedrock inbound packets into the same common intent models.

## Phase 6: Translator Core

- Add registry key mapping tables for blocks, items, entities, and biomes.
- Implement Java-to-internal and internal-to-Java mapping first.
- Add Bedrock mapping once Bedrock packet/session foundations are in place.
- Represent unsupported behavior through explicit capability flags.

## Quality Gates

- Run `./gradlew --no-configuration-cache clean build` before merging.
- Add focused tests for every packet codec, session transition, queue, adapter, and common model.
- Preserve module boundaries: protocol-specific classes stay out of `molten-common`.
- Treat client traffic as untrusted; validate before creating intents.
