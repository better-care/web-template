# Upgrading to 5.0

This release aligns the FLAT and STRUCTURED converters with the openEHR
[simplified format](https://specifications.openehr.org/releases/ITS-REST/development/simplified_formats.html#_composition).
Many RM attributes that were previously dropped are now emitted on `RAW → FLAT` / `RAW → STRUCTURED`
and accepted on `FLAT → RAW` / `STRUCTURED → RAW`. A few existing keys changed, and locale-based value
parsing was removed.

If you persist, diff, or assert on flat/structured payloads, read **Breaking changes** first, as
existing compositions produce additional keys, and a couple of keys changed shape.

Examples use flat-format `key = value` pairs. All changes apply symmetrically to STRUCTURED.

---

## Breaking changes

### `DV_IDENTIFIER` — output value moved from the bare key to `|id`

```
Before: .../identifier       = "12345"
Now:    .../identifier|id    = "12345"
```

On input **both** the bare key and `|id` are accepted; the output **always** uses `|id`.

### `DV_PARSABLE` — output value moved from the bare key to `|value`

```
Before: .../timing           = "R3/..."
Now:    .../timing|value     = "R3/..."
        .../timing|formalism = "timing"   (unchanged)
```

On input **both** the bare key and `|value` are accepted; the output always uses `|value`.

### `DV_INTERVAL` and `REFERENCE_RANGE` — boundary attributes no longer omitted

`DV_INTERVAL` and `REFERENCE_RANGE` now emit boundary attributes when they carry **non-default** 
information (previously dropped):

```
.../range|upper_unbounded = "true"    (emitted when true)
.../range|lower_included  = "false"   (emitted when false)
```

---

## New fields

Previously dropped, now accepted, saved and emitted.

### Compositions and entries

| Node | New keys                                                                       |
|------|--------------------------------------------------------------------------------|
| `COMPOSITION` | `/category`, `/composer`                                                       |
| `ADMIN_ENTRY` | `/language`                                                                    |
| `EVALUATION` | `/language`, `/_guideline_id`                                                  |
| `INSTRUCTION` | `/language`, `/narrative`, `/expiry_time`, `/_guideline_id`, `/_wf_definition` |
| `ACTION` | `/language`, `/time`, `/ism_transition`, `/subject`, `/_guideline_id`          |
| `OBSERVATION` | `/language`, `/_guideline_id`                                                  |

### Structural nodes

| Node | New keys                                                        |
|------|-----------------------------------------------------------------|
| `ACTIVITY` | `/timing`, `/action_archetype_id`                               |
| `INTERVAL_EVENT` | `/width`, `/math_function`, `\|sample_count`                    |
| `ISM_TRANSITION` | `/current_state`, `/transition`, `/careflow_step`, `/_reason:i` |
| `PARTY_SELF` | `\|id`, `\|id_scheme`, `\|id_namespace`                         |
| `PARTY_RELATED` | `/relationship`                                                 |

### Data values

| Type | New keys                                                                                                                         |
|------|----------------------------------------------------------------------------------------------------------------------------------|
| `DV_TEXT` | `/_language`, `/_encoding`                                                                                                       |
| `DV_CODED_TEXT` | `/_language`, `/_encoding`                                                                                                       |
| `DV_QUANTITY`, `DV_COUNT`, `DV_DURATION` | `\|accuracy`, `\|accuracy_is_percent`                                                             |
| `DV_PROPORTION` | `\|accuracy`, `\|accuracy_is_percent`, `\precision` |                                                                                                                                
| `DV_DATE`, `DV_TIME`, `DV_DATE_TIME` | `/_accuracy`                                                                                                                     |
| `DV_MULTIMEDIA` | `\|data`, `\|compression_algorithm`, `\|integrity_check`, `\|integrity_check_algorithm`, `/_thumbnail`, `/_charset`, `/_language` |

---

## Notes & decisions

- **`territory` is not added at entry level.** `territory` is not a field on the `ENTRY` class, as it
  belongs to the `COMPOSITION`, so it is intentionally not emitted on `ADMIN_ENTRY`/`INSTRUCTION`/
  `ACTION`/`EVALUATION`/`OBSERVATION`.
- **`language` *is* emitted at entry level.** `language` is a real field on `ENTRY`, so every entry
  can carry its own language, which may differ from the composition's, and it is emitted per entry.
- **`ism_transition` default.** When an `ACTION` has no ISM transition, one is created with
  `current_state` code `532`, terminology `openehr`, value `completed`. This default is pre-existing;
  it is now surfaced in flat/structured because `ACTION` now emits `ism_transition`.
- **`expiry_time`, not `_expiry_time`.** The spec table shows `_expiry_time`, but the examples use
  `expiry_time` on `INSTRUCTION`; the examples were followed.
- **`relationship`, not `_relationship`.** The spec table shows `/_relationship` under `PARTY_RELATED`,
  but the example uses `/relationship`; the example was followed.
- **`POINT_EVENT` needs no dedicated mapper** — its `time` is already set by the surrounding handling,
  so a `PointEvent` mapper would only re-write the same value.
- **STRUCTURED mirrors FLAT** for all of the above.
