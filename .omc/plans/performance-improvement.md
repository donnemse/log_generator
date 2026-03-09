# Log Generator Performance Improvement Plan

## Context

### Original Request
Improve the performance of the log generation system. The current system has multiple critical bottlenecks that limit throughput and cause correctness issues under concurrent load.

### System Overview
Spring Boot 2.6.3 / Java 8 log generator application that reads YAML config, generates structured log events at configurable EPS rates, and fans out to multiple output sinks (file, Kafka, TCP/Netty). Uses @Async thread pool (50 threads), per-output LinkedBlockingQueue consumers, and a 3-second EPS monitoring cycle.

### Research Findings
11 bottlenecks identified across concurrency safety, I/O efficiency, serialization overhead, and configuration errors. The top 4 (QueueService thread-safety, OutputService.cache thread-safety, calcObjectSize serialization, parallelStream contention) dominate production impact.

---

## Work Objectives

### Core Objective
Eliminate correctness bugs and increase sustainable throughput by 5-10x for the log generation pipeline, targeting stable operation at 50,000+ EPS per generator.

### Deliverables
1. Thread-safe queue infrastructure with no data corruption under concurrent writes
2. Thread-safe output cache with no data corruption under concurrent access
3. Efficient queue size monitoring without full serialization
4. Buffered file I/O for output writers
5. Corrected Kafka and Netty configurations
6. Thread-safe field generators (counters and date formatters)
7. Performance benchmark script to validate improvements

### Definition of Done
- All P0 and P1 tasks completed and verified
- No race conditions detectable under 50-thread concurrent stress test
- EPS monitoring overhead reduced from seconds to sub-millisecond
- File output throughput increased measurably (target: 3x+)
- All existing tests pass; no regressions

---

## Guardrails

### MUST Have
- Backward compatibility with existing YAML config format
- All existing output types (file, Kafka, TCP) continue to function
- No Java version upgrade (stay on Java 8)
- No Spring Boot version upgrade in this plan
- Each phase independently deployable

### MUST NOT Have
- Changes to the REST API contract
- Changes to the frontend UI behavior
- New external dependencies unless strictly necessary
- Removal of any output type

---

## Acceptance Criteria (Measurable)

| ID | Criterion | Measurement |
|----|-----------|-------------|
| AC-1 | Zero ConcurrentModificationException under 50-thread load | Stress test with 50 generators, 0 exceptions in 60s run |
| AC-2 | Queue size calculation completes in <1ms | Timed measurement in EpsMonitorService log |
| AC-3 | File write throughput >= 30,000 lines/sec per writer | Benchmark with 100K line file output |
| AC-4 | Kafka buffer memory <= 256MB | Config verification |
| AC-5 | EPS monitoring cycle completes in <50ms total | Timed measurement across all queues |
| AC-6 | No thread-safety warnings from static analysis | SpotBugs or manual audit |
| AC-7 | SparrowOutput watchdog does not unnecessarily recreate handlers | Integration test: verify healthy outputs are not reset |
| AC-8 | OutputService.cache concurrent access safe | Stress test: concurrent REST + scheduled access, 0 exceptions |

---

## Task Flow and Dependencies

```
Phase P0 (Critical Bugs - must fix first)
  T1: QueueService thread-safety ──────┐
  T1b: OutputService.cache safety ─────┤
  T2: calcObjectSize removal ──────────┤
                                       ├──► Phase P1 (High-Impact Perf)
Phase P1                               │
  T3: Remove parallelStream ───────────┤
  T4: BufferedWriter for file I/O ─────┤
  T5: Kafka buffer config fix ─────────┘
                                       │
Phase P2 (Medium Impact)               │
  T6: Thread-safe TimeField ───────────┤
  T7: Thread-safe IDField (counter     │
      + SimpleDateFormat) ─────────────┤
  T8: Netty serialization ────────────┤
  T9: SparrowOutput.isReady fix ───────┘
                                       │
Phase P3 (Nice-to-Have)                │
  T10: generateLog() cleanup ──────────┤
  T11: Benchmark harness ─────────────┤
  T12: EpsMonitorService CME fix ──────┘
```

---

## Phase P0: Critical Bugs (Fix First)

### T1: Replace QueueService LinkedHashMap with ConcurrentHashMap

**Priority:** P0 - Critical
**Complexity:** M
**Files:**
- `src/main/java/com/yuganji/generator/queue/QueueService.java`

**Problem:**
`QueueService.queue` is a `LinkedHashMap<Integer, QueueObject>` (line 20) accessed concurrently by multiple @Async generator threads. The map uses `Integer` keys (output IDs). This causes silent data corruption, lost log events, and potential ConcurrentModificationException.

**Solution:**
1. Replace `Map<Integer, QueueObject> queue` (initialized as `LinkedHashMap<>`) with `ConcurrentHashMap<Integer, QueueObject>`
2. Replace any compound check-then-act operations (e.g., `putIfAbsent` is already atomic on ConcurrentHashMap -- verify no other patterns)
3. If iteration order matters for display, use a separate synchronized snapshot for reads

**Acceptance Criteria:**
- AC-1: Zero exceptions under 50-thread concurrent push for 60 seconds
- `QueueObject` instances never silently disappear from the map

**Verification:**
- Write a concurrent stress test: 50 threads calling `push()` simultaneously for 60s
- Verify entry count matches expected count
- Run with `-ea` (assertions enabled) and no errors

---

### T1b: Replace OutputService.cache HashMap with ConcurrentHashMap

**Priority:** P0 - Critical
**Complexity:** M
**Files:**
- `src/main/java/com/yuganji/generator/output/OutputService.java`

**Problem:**
`OutputService.cache` (line 29) is a raw `HashMap<Integer, OutputDto>` that is accessed concurrently from multiple threads:
- The `@Scheduled schedule()` method (line 52) iterates and mutates the map every 20 seconds
- REST controller threads call `add()`, `modify()`, `remove()`, `start()`, `stop()` which all read/write the map
- `EpsMonitorService.monitorEps()` (line 46) calls `outputService.get()` to read from the map every 3 seconds

This is the same class of bug as T1: a non-thread-safe `HashMap` under concurrent access causes silent data corruption, lost entries, and potential ConcurrentModificationException or infinite loops (HashMap rehash bug).

**Solution:**
1. Change `private Map<Integer, OutputDto> cache` to `ConcurrentHashMap<Integer, OutputDto>`
2. In `init()` (line 42), collect into a `ConcurrentHashMap` instead of the default `HashMap` from `Collectors.toMap()`:
   ```java
   this.cache = outputRepository.findAll().stream()
       .collect(Collectors.toMap(Output::getId, Output::toDto, (a, b) -> a, ConcurrentHashMap::new));
   ```
3. In `schedule()` (line 53), the `this.cache.values().forEach(...)` iteration with `this.cache.put()` inside is safe on ConcurrentHashMap (weakly consistent iterator)
4. Review `modify()` which does `remove()` then `put()` -- these are not atomic as a pair; consider using `compute()` or accepting the brief inconsistency window since modify requires status==0 (not running)

**Acceptance Criteria:**
- AC-8: Zero exceptions under concurrent REST API calls + scheduled task for 60 seconds
- No lost OutputDto entries during concurrent add/modify/remove operations

**Verification:**
- Concurrent stress test: multiple threads calling add/modify/remove/start/stop while schedule() runs
- Verify cache size matches database record count after test

---

### T2: Replace calcObjectSize with Atomic Counter

**Priority:** P0 - Critical
**Complexity:** M
**Files:**
- `src/main/java/com/yuganji/generator/util/CommonUtil.java` (remove or deprecate `calcObjectSize`)
- `src/main/java/com/yuganji/generator/queue/QueueObject.java` (add AtomicLong byte counter)
- `src/main/java/com/yuganji/generator/queue/QueueService.java` (update push/poll to track bytes)
- `src/main/java/com/yuganji/generator/monitor/EpsMonitorService.java` (use new counter)

**Problem:**
`CommonUtil.calcObjectSize(queue)` (called at EpsMonitorService line 50) serializes the entire LinkedBlockingQueue (up to 100K Map entries) via Java ObjectOutputStream every 3 seconds. This blocks the monitoring thread for seconds, causes GC pressure from temporary byte arrays, and contends with producer/consumer threads.

**Solution:**
1. Add an `AtomicLong totalBytes` field to `QueueObject`
2. On `push()` in QueueService: estimate entry size as sum of key.length + value string lengths (or a fixed estimate per field type, e.g., 200 bytes/entry). Increment `totalBytes`
3. On `poll()` in QueueService: note that `drainTo(list, maxBuffer)` drains multiple elements at once (line 68). Decrement `totalBytes` by `estimate * count` (where `count` is the return value of `drainTo`), NOT by a single-element estimate
4. On the eviction path in `push()` (line 56, when `remainingCapacity() == 0` and an element is polled): also decrement by one element estimate
5. In `EpsMonitorService`: read `totalBytes.get()` instead of calling `calcObjectSize()`
6. Deprecate `calcObjectSize()` -- do not delete yet in case other code references it

**Acceptance Criteria:**
- AC-2: Queue size read completes in <1ms (was seconds)
- AC-5: Full monitoring cycle <50ms
- Memory estimate within 2x of actual (order-of-magnitude accurate is sufficient for monitoring)

**Verification:**
- Log timing of `EpsMonitorService` monitoring cycle before and after
- Compare reported size with actual serialized size on a sample queue (one-time validation)

---

## Phase P1: High-Impact Performance

### T3: Remove parallelStream from QueueService.push()

**Priority:** P1 - High
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/queue/QueueService.java`

**Problem:**
`push()` (line 43) uses `this.entry().parallelStream()` to fan out each log event to all output queues. At 10K EPS this spawns ForkJoinPool tasks for every single event, creating massive thread contention and context switching. The fan-out target is typically 1-5 outputs -- parallelStream overhead far exceeds any benefit.

**Solution:**
1. Replace `this.entry().parallelStream().forEach(...)` with a plain `for` loop or `this.entry().stream().forEach(...)`
2. The inner `queue.offer()` on LinkedBlockingQueue is already O(1) and thread-safe

**Acceptance Criteria:**
- ForkJoinPool thread count stays at baseline (no spike during push)
- Throughput improvement measurable at >10K EPS

**Verification:**
- Thread dump before/after: no ForkJoinPool.commonPool threads spawned by push()
- Benchmark: measure push() latency p99 at 10K EPS (should drop significantly)

---

### T4: Add BufferedWriter to File Output

**Priority:** P1 - High
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/output/file/FileWriterObject.java`
- `src/main/java/com/yuganji/generator/output/file/RawOutputWriter.java`

**Problem:**
FileWriter writes directly to OS without buffering. Each `write()` call is a syscall. Current code flushes every 100 lines, but without BufferedWriter, every write is already unbuffered.

**Solution:**
1. Wrap `FileWriter` in `BufferedWriter` with 64KB buffer (or 128KB for high EPS)
2. Keep the existing flush-every-100-lines logic as a safety net
3. Ensure `close()` properly flushes and closes the BufferedWriter

**Acceptance Criteria:**
- AC-3: File write throughput >= 30,000 lines/sec
- No data loss on normal shutdown

**Verification:**
- Benchmark: write 100K lines, measure wall-clock time before and after
- Verify file completeness: line count matches expected

---

### T5: Fix Kafka BUFFER_MEMORY_CONFIG and Tune LINGER_MS_CONFIG

**Priority:** P1 - High
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/output/model/OutputKafkaProducer.java`

**Problem:**
`BUFFER_MEMORY_CONFIG` is set to ~5GB (`1024L * 1024 * 1024 * 5` at line 123). This allows the Kafka producer buffer to consume the entire JVM heap, leading to OOM or extreme GC pauses. Additionally, `LINGER_MS_CONFIG` is already set to 1000ms (line 122), which is excessively high -- it means the producer waits a full second before sending a batch, adding 1 second of latency to every message batch.

**Solution:**
1. Reduce `BUFFER_MEMORY_CONFIG` to 256MB (`268435456`) or make configurable via YAML/properties
2. Add `MAX_BLOCK_MS_CONFIG` (e.g., 5000ms) so producers fail fast when buffer is full rather than blocking indefinitely
3. Reduce `LINGER_MS_CONFIG` from 1000ms to 5-10ms for a better balance between batching efficiency and latency. The current 1000ms value causes unnecessary latency; 5-10ms still allows effective batching at high EPS while keeping latency low

**Acceptance Criteria:**
- AC-4: Buffer memory <= 256MB
- Kafka producer does not cause OOM under sustained load
- Kafka message delivery latency reduced from ~1s to ~10ms

**Verification:**
- JMX or heap dump: verify Kafka buffer allocation stays within bounds
- Load test: sustained 10K EPS to Kafka for 5 minutes, no OOM
- Measure end-to-end Kafka message delivery latency before/after

---

## Phase P2: Medium Impact

### T6: Thread-Safe TimeField

**Priority:** P2 - Medium
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/field/TimeField.java`

**Problem:**
`SimpleDateFormat` is not thread-safe. Currently each generator thread likely creates its own `LoggerDetailDto` with its own `TimeField`, so this is safe today. But if the architecture changes to share field generators, this becomes a silent data corruption bug.

**Solution:**
1. Replace `SimpleDateFormat` with `DateTimeFormatter` (Java 8, thread-safe, immutable)
2. Or use `ThreadLocal<SimpleDateFormat>` if DateTimeFormatter's output format differs

**Acceptance Criteria:**
- AC-6: No thread-safety warnings for date formatting
- Output format unchanged

**Verification:**
- Compare formatted output before and after for 1000 samples
- Concurrent test: 50 threads formatting simultaneously, no garbled output

---

### T7: Thread-Safe IDField (Counter + SimpleDateFormat)

**Priority:** P2 - Medium
**Complexity:** M
**Files:**
- `src/main/java/com/yuganji/generator/field/IDField.java`

**Problem:**
IDField has TWO thread-safety issues:

1. **Non-atomic counter** (line 16): Uses plain `long count`. If shared across threads, `count++` (line 42) is not atomic and can produce duplicate IDs.

2. **Non-thread-safe SimpleDateFormat instances** (lines 12-13): Two `SimpleDateFormat` instance fields (`yyyyMMddHH` and `yyyyMMddHHmmssSSS`) are used in `get()` (lines 33, 40). `SimpleDateFormat.format()` is not thread-safe -- concurrent calls corrupt internal `Calendar` state, producing garbled date strings and incorrect IDs.

**Solution:**
1. Replace `long count` with `AtomicLong count`
2. Use `count.getAndIncrement()` instead of `count++`
3. Replace `SimpleDateFormat yyyyMMddHH` and `SimpleDateFormat yyyyMMddHHmmssSSS` with `DateTimeFormatter` instances (Java 8, thread-safe, immutable):
   - `private static final DateTimeFormatter YYYY_MM_DD_HH = DateTimeFormatter.ofPattern("yyyyMMddHH");`
   - `private static final DateTimeFormatter YYYY_MM_DD_HH_MM_SS_SSS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");`
4. Update `get()` to use `LocalDateTime.now()` with the new formatters
5. Update `currentTime` comparison logic to use the new formatter

**Acceptance Criteria:**
- AC-6: No duplicate IDs under concurrent generation
- AC-6: No garbled date strings under concurrent generation
- Zero overhead (AtomicLong.getAndIncrement is a single CAS instruction; DateTimeFormatter is lock-free)

**Verification:**
- Concurrent test: 50 threads generating 10K IDs each, collect into Set, verify size == 500K (no duplicates)
- Concurrent test: 50 threads formatting dates simultaneously, verify no garbled output
- Compare ID format output before and after for 1000 samples (format must match)

---

### T8: Replace Netty Java Serialization with JSON/Protobuf

**Priority:** P2 - Medium
**Complexity:** L
**Files:**
- `src/main/java/com/yuganji/generator/output/sparrow/TCPSocketServerInstance.java`
- Related Netty pipeline configuration files

**Scope Note:** The TCP server code is in this repository (`TCPSocketServer`, `TCPSocketServerInstance`). TCP clients that connect to this server may be external systems. The worker implementing this task should first check whether any client code exists in this repo (search for `ObjectDecoder`, Netty bootstrap client patterns). If clients are external, the protocol change is a breaking change that requires coordination -- document the new wire format and consider a versioned protocol or transition period.

**Problem:**
Netty channel uses `ObjectEncoder`/`ObjectDecoder` (Java serialization). This is slow, produces large payloads, and is a known security risk (deserialization vulnerabilities).

**Solution:**
1. Replace with JSON serialization using Jackson (already a Spring Boot dependency)
2. Use `StringEncoder`/`StringDecoder` in the Netty pipeline with JSON string messages
3. Or use a length-prefixed JSON protocol: 4-byte length header + JSON bytes
4. Update both server and any client code that reads from this channel (if client code is in this repo)

**Acceptance Criteria:**
- Payload size reduced by 50%+ compared to Java serialization
- No deserialization security vulnerabilities
- If clients are in-repo: updated to match new protocol
- If clients are external: migration guide documented

**Verification:**
- Compare payload sizes: serialize same Map object with both methods
- Integration test: send 1000 messages through Netty pipeline, verify all received correctly

---

### T9: Fix SparrowOutput.isReadyForRunning() Unconditional False Return

**Priority:** P2 - Medium
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/output/model/SparrowOutput.java`
- `src/main/java/com/yuganji/generator/output/OutputService.java` (for context on how it is called)

**Problem:**
`isReadyForRunning()` (line 58) always returns `false`. The watchdog in `OutputService.schedule()` (lines 55-58) DOES handle auto-restart: when `isRunning()` returns false for a status==1 output, it checks `isReadyForRunning()`. If that returns false, it calls `resetHandler()` (which creates a new handler instance), then calls `startOutput()`. So the watchdog CAN auto-restart -- the diagnosis that it "can never auto-restart" is incorrect.

The ACTUAL problem is the opposite: because `isReadyForRunning()` unconditionally returns `false`, the watchdog calls `resetHandler()` EVERY time it encounters a non-running output, even when the handler is perfectly healthy and just needs `startOutput()` called. This means:
- Unnecessary object allocation (new handler created when existing one is fine)
- For thread-based handlers like `OutputKafkaProducer`, `isReadyForRunning()` returns `Thread.State.NEW` (line 68 of OutputKafkaProducer), meaning a handler that has been started once can never be "ready" again without reset. SparrowOutput returning false forces the reset path, which is actually the CORRECT workaround for thread-based handlers but for the WRONG reason.
- The real fix is to implement proper readiness logic so the watchdog only resets when truly necessary.

**Solution:**
1. In `SparrowOutput.isReadyForRunning()`: implement actual readiness check. Return `true` if the server is null or not running and the port is available (no existing binding). Return `false` if the port is already bound by another process.
2. In `OutputKafkaProducer.isReadyForRunning()`: it correctly checks `Thread.State.NEW`, but a used thread can never return to NEW state. Consider restructuring to use a `Runnable` + `new Thread()` pattern in `startOutput()` instead, or keep the current `resetHandler()` approach and document it.
3. Add a log statement in `OutputService.schedule()` when `resetHandler()` is called, so restarts are visible in logs.

**Acceptance Criteria:**
- AC-7: Watchdog does not unnecessarily recreate handlers for healthy outputs
- Watchdog successfully restarts genuinely failed outputs within 20 seconds
- Log message emitted on every handler reset

**Verification:**
- Integration test: start output, verify watchdog does NOT reset a healthy handler
- Integration test: kill output's underlying resource, verify watchdog restarts it
- Check logs: resetHandler() calls logged with output name and reason

---

## Phase P3: Nice-to-Have

### T10: Clean Up generateLog() Stream Side-Effects

**Priority:** P3 - Low
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/model/LoggerDetailDto.java`

**Problem:**
`generateLog()` uses `Collectors.toMap` while also mutating an external map via side-effects. This is fragile and violates stream API contracts (side-effect-free operations).

**Solution:**
1. Use a simple `for` loop to build the result map, or
2. Use `Collectors.toMap()` purely (no external mutation) and return the collected result

**Acceptance Criteria:**
- Identical output for all field types
- Cleaner code; no external mutation in stream operations

**Verification:**
- Unit test: compare output of old vs new implementation for 1000 generated logs

---

### T11: Performance Benchmark Harness

**Priority:** P3 - Low
**Complexity:** M
**Files:**
- New file: `src/test/java/com/yuganji/generator/benchmark/PerformanceBenchmark.java`
- Or a simple shell script with `curl` + timing

**Problem:**
No automated way to measure EPS throughput, latency percentiles, or regression between changes.

**Solution:**
1. Create a JUnit-based benchmark that:
   - Configures N generators at target EPS
   - Runs for 60 seconds
   - Reports: actual EPS achieved, p50/p95/p99 push latency, queue depth over time, memory usage
2. Alternatively, a shell script that starts the app, sends config via REST API, and measures output file line count over time

**Acceptance Criteria:**
- Reproducible benchmark that can run in CI
- Reports key metrics: EPS, latency percentiles, memory

**Verification:**
- Run benchmark on current code (baseline), then after each phase, compare metrics

---

### T12: Fix EpsMonitorService ConcurrentModificationException on producerEps

**Priority:** P3 - Low
**Complexity:** S
**Files:**
- `src/main/java/com/yuganji/generator/monitor/EpsMonitorService.java`

**Problem:**
In `monitorEps()` (lines 32-34), the code iterates `entryInfo.getValue().getProducerEps().entrySet()` and calls `producerEps.remove(entryEps.getKey())` inside the loop. Even though `producerEps` is a `ConcurrentHashMap`, calling `remove()` on the map while iterating its `entrySet()` via a for-each loop can cause `ConcurrentModificationException` because the for-each uses the iterator, and `remove()` is called on the map directly (not via `Iterator.remove()`).

**Solution:**
1. Use `Iterator` explicitly and call `iterator.remove()` instead of `map.remove()`, OR
2. Use `entrySet().removeIf(entry -> ...)` which is safe on ConcurrentHashMap, OR
3. Collect keys to remove into a separate list, then remove after iteration

**Acceptance Criteria:**
- No ConcurrentModificationException during EPS monitoring under load
- Stopped loggers are still properly cleaned up from producerEps

**Verification:**
- Stress test: start/stop loggers rapidly while monitorEps runs, verify no exceptions
- Verify producerEps entries are cleaned up for stopped loggers

---

## Commit Strategy

| Commit | Contents | Phase |
|--------|----------|-------|
| 1 | T1: ConcurrentHashMap for QueueService (`Map<Integer, QueueObject>`) | P0 |
| 2 | T1b: ConcurrentHashMap for OutputService.cache (`Map<Integer, OutputDto>`) | P0 |
| 3 | T2: AtomicLong byte counter, deprecate calcObjectSize (with batch drainTo accounting) | P0 |
| 4 | T3: Remove parallelStream from push() | P1 |
| 5 | T4: BufferedWriter for file output | P1 |
| 6 | T5: Fix Kafka buffer config + reduce LINGER_MS from 1000ms to 5-10ms | P1 |
| 7 | T6: Thread-safe TimeField (DateTimeFormatter) | P2 |
| 8 | T7: Thread-safe IDField (AtomicLong counter + DateTimeFormatter for both formatters) | P2 |
| 9 | T8: Netty JSON serialization | P2 |
| 10 | T9: Fix isReadyForRunning() with proper readiness logic | P2 |
| 11 | T10 + T11: generateLog cleanup + benchmark | P3 |
| 12 | T12: Fix EpsMonitorService iterator remove pattern | P3 |

Each commit should be independently buildable and deployable.

---

## Risk Identification

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| ConcurrentHashMap changes iteration order (T1, T1b) | Medium | Low | UI displays may show different order; verify frontend handles this |
| Byte estimate diverges from reality (T2) | Medium | Low | Use conservative estimate; log actual vs estimated periodically for calibration |
| Batch drainTo accounting drift (T2) | Low | Low | Periodically recalibrate by comparing totalBytes with queue.size() * estimate |
| Netty protocol change breaks external clients (T8) | High | High | Check if clients are in-repo first; version the protocol; support both old and new for transition period |
| Kafka config change causes message loss (T5) | Low | High | Add error callback handler to log dropped messages; test with backpressure |
| BufferedWriter data loss on crash (T4) | Low | Medium | Keep flush-every-100-lines; add shutdown hook for clean flush |
| isReadyForRunning true-positive causes port conflict (T9) | Medium | Medium | Check port availability before returning true; add cooldown period between restart attempts |
| OutputService.cache race during modify() (T1b) | Medium | Medium | The remove-then-put in modify() is not atomic; use compute() or accept brief window since status==0 guard exists |
| DateTimeFormatter format mismatch with SimpleDateFormat (T7) | Low | Medium | Verify format strings produce identical output for edge cases (midnight, DST transitions) |

---

## Success Criteria

| Metric | Before (Estimated) | After (Target) | How to Measure |
|--------|--------------------|--------------------|----------------|
| Max stable EPS | ~5,000-10,000 | 50,000+ | Benchmark harness (T11) |
| calcObjectSize latency | 500ms-5s per call | <1ms | Timed log in EpsMonitorService |
| File write throughput | ~5,000-10,000 lines/s | 30,000+ lines/s | Timed file output benchmark |
| ForkJoinPool threads during push | 10-50+ | 0 | Thread dump / JMX |
| Kafka OOM risk | High (5GB buffer) | None (256MB cap) | JMX heap monitoring |
| Kafka batch latency | ~1000ms (LINGER_MS) | ~5-10ms | End-to-end message timing |
| Data corruption under load | Possible (3 unsafe maps) | None | 60-second stress test |
| Handler unnecessary resets | Every watchdog cycle | Only when needed | Log audit of resetHandler() calls |
