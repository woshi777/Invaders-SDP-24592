# PR #11: Records screen and UI regression

Reviewed for woshi777 with Codex assistance on 2026-10-16 (Asia/Shanghai).

- Target: https://github.com/clover0409/Invaders-SDP-24592/pull/11
- Tested commit: `485fd5a05ba90e3a6e282ee367e4ad7ce12ffda2`
- Feature delta inspected: `7560acd276bbbff602c8de42aefc0240e62444a9...485fd5a05ba90e3a6e282ee367e4ad7ce12ffda2`
- Result: the executed feature checks passed; manual playthrough and audible audio remain outstanding.

## Executed checks

| Check | Observed result |
| --- | --- |
| Production compilation | PASS: 60 Java source files |
| Existing RecordsSmokeTest | PASS: 104 checks |
| Existing GameplayIntegrationTest | PASS: 35 checks |
| Existing GameScreenWavesTest | PASS: existing wave-list checks completed |
| Live Campaign/Infinite UI checks | PASS: 28 assertions; 12 additional WAV decode checks |
| FIFO notification timing | PASS: Infinite 3015 ms; Campaign 3015 ms |
| Input during notifications | PASS: ship movement and shooting continued |
| Result summary | PASS: three seeded gameplay unlocks plus First Flight retained |
| Result controls | PASS: high-score cursor/character editing, Space replay in the same mode, Esc to menu |
| Records/menu/settings GUI checks | PASS: 13 UI assertions; WAV checks repeated in this runner |
| Main-menu route | PASS: Records uses 9; Settings uses 4; I retains Infinite route 8 |
| Records navigation | PASS: Right/Left switch tabs; Esc and Space return to menu |
| Populated profile | PASS: highest score 1900, most enemies 29, longest survival 69s; all ten fixture runs shown |
| Achievement presentation | PASS: five names/conditions/statuses and persisted unlock times; locked state also checked |
| Empty profile | PASS: personal-best/ranking empty states and locked achievements shown |
| Legacy settings | PASS: audio volume +/-5, left-key rebind/restore, all three subpages return |

## Method and limits

The checks were run on Linux with OpenJDK 21.0.12.1. Production source, tests, dependency declarations and lockfiles of the reviewed PRs were left unchanged. GUI checks ran the actual `engine.Core` in a real AWT window on an isolated X11 display and sent OS keyboard events with `java.awt.Robot`.

The GUI review uses synthetic profiles, achievement events and a forced zero-lives condition to reach result screens deterministically. These screenshots do not demonstrate a natural 20-kill/60-second run or a complete Campaign playthrough. The existing gameplay integration suite independently exercises actual collision handlers. A five-achievement victory image is a rendering stress fixture, not a claim that all five can unlock naturally in one run.

All 12 bundled WAV files decoded successfully. The machine has no supported audio output line; BGM/SFX playback logged `No line matching interface Clip`. Audible playback, a full manual playthrough and threshold/difficulty calibration remain unverified.

The original `RecordsSmokeTest.testShutdownHookFlush` hard-codes `bin/java.exe`. A native Linux invocation initially fails at that subprocess launch. A local Java-home compatibility directory supplies a `java.exe` alias for the real Java launcher; the unchanged shutdown test then launches its child process and verifies all 80 pending records survive restart. This is a test portability limitation, not a confirmed defect in the reviewed UI behavior. The supplied runner preserves assertions and subprocess exit status.

No blocking application defect was confirmed in the reviewed feature delta. No application code fix or merge was made. Follow the team's prerequisite and merge order; this review does not replace final team acceptance.

## Screenshot evidence

These are actual-window screenshots with synthetic profiles. Empty/locked window captures were also compared pixel-for-pixel with complete offscreen render buffers and matched exactly.

![Records selected in the original menu](screenshots/menu-records-selected.png)
![Three personal bests and ten runs](screenshots/records-populated.png)
![All five unlocked achievements](screenshots/achievements-unlocked.png)
![Empty records](screenshots/records-empty.png)
![All five locked achievements](screenshots/achievements-locked.png)
![Legacy settings](screenshots/settings-menu.png)

The following is an offscreen rendering stress fixture produced by the existing gameplay integration suite, using a Campaign-victory state and all five achievement names. It is not a manual playthrough.

![Campaign victory rendering stress fixture](screenshots/offscreen-campaign-cleared-results.png)

Logs: [suite results](results.json), [live notifications](logs/notifications-live.log).
