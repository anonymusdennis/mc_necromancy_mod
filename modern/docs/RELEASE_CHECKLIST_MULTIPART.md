# Necromancy release checklist (multipart bridge)

Use before tagging a build that includes minion multipart + bodypart validation.

1. **Lang / UX**
   - `assets/necromancy/lang/en_us.json` includes `message.necromancy.altar.parts_unconfigured` with `%s` args and bodypart `not_validated` strings.
   - Creative tabs expose placeholder cage + operation table shell without missing-model purple/black cubes.

2. **Telemetry defaults**
   - `multipart.enableMultipartTelemetry` stays **false** in shipped `necromancy-common.toml`.
   - Stress profiling toggles documented for QA only.

3. **Authoring `validated` JSON**
   - Fresh server boots write stubs with `"validated": false`.
   - Missing legacy field still loads as validated (`Boolean` absent ⇒ loader treats as approved).
   - Dev-console **Save to disk** stamps `"validated": true`.

4. **Performance smoke**
   - Idle minions do not rebuild `TransformHierarchy` unless assembly fingerprints change (watch topology revision in debug tooling).
   - 50×50 minion soak optional under telemetry when enabling multipart profiling.

5. **Audit sign-off**
   - Multipart debug overlays align with hierarchy collision boxes when `minionLegacyCompositeCollision=false`.
   - LAN multipart replication noted as follow-up until stable persistent node ids ship.

6. **RC-F8 archive**
   - Keep a short client/server log + screenshot bundle when validating a candidate build (altar refusal lists blocked slots, idle minion topology revision steady, optional F3 multipart overlay sanity).
