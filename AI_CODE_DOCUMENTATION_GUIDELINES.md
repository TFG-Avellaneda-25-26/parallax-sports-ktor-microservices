AI Code Documentation Guidelines (Parallax Project)
This document establishes the standards for AI-assisted and manual documentation. The goal is to ensure high-signal, low-noise comments for an event-driven microservices architecture.

1. The Core Mission
"Document the data flow and side effects, not the Kotlin syntax."

2. Document Architecture
2.1 Section Banners (Grouping)
Use decorated banners to partition logic within a file (e.g., separating Authentication, Stream Processing, and Private Helpers).

Kotlin
/*============================================================
  REDIS STREAM CONSUMPTION
  Logic for fetching, processing, and ACK of stream messages
============================================================*/
2.2 Scanner Line (Required for Handlers)
Every function that interacts with infrastructure (Redis, Discord, Gmail, Spring Callback) must have a one-line scanner comment.
Format: // -> Source: <Origin> || Action: <Side Effect> || Strategy: <Retry/Drop/Ack>

Examples:

// -> Source: Redis Stream || Action: Send Gmail Alert || Strategy: Retry on 5xx

// -> Source: HTTP Post || Action: Refresh OAuth2 Token || Strategy: TTL 55m

2.3 Ktor & API Contracts
For external API calls (Google/Discord/Spring):
// -> API: <Endpoint> || Auth: <Type> || Scope: <Permission>

3. Style Rules (Strict)
Domain Vocabulary: Use specific terms like Stream, Consumer Group, Artifact, Embed, Idempotency, TTL.

Active Voice & Intent: Avoid "This loop iterates...". Use "Dispatches each pending alert to the provider."

Coroutines & Nullability: * State why a method returns null (e.g., "Returns null if Discord channel ID is invalid").

Clarify if a suspend function has specific Dispatcher requirements.

No Code Narration: Do not explain standard Kotlin features (e.g., apply, let, map).

4. Specific Standards
4.1 Redis Stream Consumers
Every class extending RedisStreamConsumer must include a header defining its topology:

Kotlin
/**
 * [Worker Name]
 * * Stream: [stream.name.v1]
 * Group:  [group-name]
 * Role:   [Brief description of what this worker achieves]
 */
4.2 Javadoc Quality Bar
Minimum shape for public methods:

Kotlin
/**
 * One-line purpose using domain meaning.
 * * @param x description of impact on behavior
 * @return contract-level output (e.g., Google Message ID)
 * @throws Exception category of failure (e.g., Network, Auth)
 */
5. What NOT to Document (Noise)
Obvious Control Flow: if/else logic that is readable at a glance.

Dependency Injection: Do not document constructor parameters injected by Koin unless they have non-obvious configurations.

Logger implementation: Avoid // Logging error before a logger.error() call.

Standard Library: No comments explaining java.util.Base64 or kotlinx.serialization.

6. Maintenance Rule
If the code behavior changes (e.g., switching from a Hash to a String in Redis), update the comments in the same commit. Documentation and code must never diverge.