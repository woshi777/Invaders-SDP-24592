Reviewed requirement 3.7 at commit `485fd5a05ba90e3a6e282ee367e4ad7ce12ffda2` (Codex-assisted review for woshi777).

- Build: 60 production Java files compiled.
- Existing tests: 104 Records checks, 35 gameplay integration checks, and the upstream wave-list checks passed.
- Real AWT-window/Robot checks: queued notices remained FIFO for about three seconds in Campaign and Infinite Mode; movement/shooting continued; result summaries retained the run's new unlocks; high-score name editing, same-mode replay and return to menu passed.
- Records opened from menu route 9; Left/Right tabs and both Esc/Space returns passed. Three personal bests, ten fixture rankings, empty states, and all five achievements with conditions/status/unlock times were checked. Legacy High scores, Settings subpages, volume adjustment, key rebind/restore and the I shortcut also passed.

No blocking application defect was confirmed in the reviewed delta. The Linux test harness hard-codes `java.exe`; I ran the unchanged subprocess test through a local compatibility alias, including its 80-record shutdown/restart check. No application source was changed.

The GUI used synthetic profiles/events and forced result states. A complete natural Campaign playthrough, threshold calibration and audible BGM/SFX remain unverified. All 12 WAV files decoded, but this cloud machine has no audio output device.

Detailed steps, results, logs and labelled screenshots: [PR #11 review evidence](https://github.com/woshi777/Invaders-SDP-24592/tree/review/woshi777-pr10-pr11-20261016/docs/reviews/2026-10-16/pr11/report.md).
