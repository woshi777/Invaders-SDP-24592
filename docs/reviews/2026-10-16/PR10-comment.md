Reviewed requirement 3.6 at commit `7560acd276bbbff602c8de42aefc0240e62444a9` (Codex-assisted review for woshi777).

- Build: 59 production Java files compiled.
- Existing tests: 92 Records checks, 18 gameplay integration checks, and the upstream wave-list checks passed.
- Real AWT-window/Robot checks: queued notices remained FIFO for about three seconds in Campaign and Infinite Mode; movement/shooting continued; result summaries retained the run's new unlocks; high-score name editing, same-mode replay and return to menu passed.

No blocking application defect was confirmed in the reviewed delta. The Linux test harness hard-codes `java.exe`; I ran the unchanged subprocess test through a local compatibility alias, including its 80-record shutdown/restart check. No application source was changed.

The GUI used synthetic profiles/events and forced result states. A complete natural Campaign playthrough, threshold calibration and audible BGM/SFX remain unverified. All 12 WAV files decoded, but this cloud machine has no audio output device.

Detailed steps, results, logs and labelled screenshots: [PR #10 review evidence](https://github.com/woshi777/Invaders-SDP-24592/tree/review/woshi777-pr10-pr11-20261016/docs/reviews/2026-10-16/pr10/report.md).
