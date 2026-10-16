# Records & Achievements review evidence

English review records for woshi777's assigned PRs #10 and #11 in `clover0409/Invaders-SDP-24592`, dated 2026-10-16 (Asia/Shanghai). This branch contains review documentation and helpers only; it does not implement or merge the game features.

| PR | Exact reviewed commit | Build | Records | Gameplay | Report | Comment text |
| --- | --- | --- | --- | --- | --- | --- |
| #10 | `7560acd276bbbff602c8de42aefc0240e62444a9` | 59 source files | 92 passed | 18 passed | [Report](pr10/report.md) | [English comment](PR10-comment.md) |
| #11 | `485fd5a05ba90e3a6e282ee367e4ad7ce12ffda2` | 60 source files | 104 passed | 35 passed | [Report](pr11/report.md) | [English comment](PR11-comment.md) |

The existing wave-list checks also passed on each PR. Separate GUI runs passed 28 notification/game-flow assertions on each exact PR; PR #11 additionally passed 13 Records/menu/settings assertions. Each runner also decoded the 12 WAV assets; these repeats are not counted as new unique coverage.

Synthetic profiles, achievement events and zero-lives fixtures make the GUI checks repeatable. These are not manual full-game completions. Audible BGM/SFX and natural difficulty calibration remain outstanding. See the reports for the `java.exe` test portability limitation and the exact evidence boundaries.

The two `PR*-comment.md` files are publication-ready comment bodies. Both API publication attempts failed with HTTP 403, so **the comments have not been posted**. See [publication status and the exact remaining action](publication-status.md).

## Reproduce the existing suites

Use the existing isolated cloud checkout; do not create a Git worktree. Select the intended PR commit in a clean checkout, preserving any local changes first. The helper verifies the exact SHA and never switches, resets or edits the reviewed repository.

```bash
python3 helpers/run_checks.py --repo /path/to/team/Invaders-SDP-24592 --expected-sha 7560acd276bbbff602c8de42aefc0240e62444a9 --output /tmp/pr10-review
python3 helpers/run_checks.py --repo /path/to/team/Invaders-SDP-24592 --expected-sha 485fd5a05ba90e3a6e282ee367e4ad7ce12ffda2 --output /tmp/pr11-review
```

Run each command only after selecting its matching checkout commit. The runner compiles with the installed JDK compiler module, enables assertions, preserves exit status and writes separate logs/result metadata. Its Linux `java.exe` alias does not bypass the shutdown subprocess.

## Reproduce GUI checks

A full Java 21 runtime with AWT/X11 libraries and a working graphical display is required. Compile `helpers/LiveUiReview.java` against the class directory produced above, and run it with the game classes and the selected checkout's `res` directory on the classpath. All output directories and profile preferences should be separate from real user data.

```bash
java -m jdk.compiler/com.sun.tools.javac.Main -cp /tmp/pr11-review/classes:/path/to/team/Invaders-SDP-24592/res -d /tmp/pr11-review/helpers helpers/LiveUiReview.java
java -Djava.awt.headless=false -Duser.home=/tmp/pr11-review/gui-home -Djava.util.prefs.userRoot=/tmp/pr11-review/gui-prefs -Dreview.res=/path/to/team/Invaders-SDP-24592/res -ea -cp /tmp/pr11-review/helpers:/tmp/pr11-review/classes:/path/to/team/Invaders-SDP-24592/res LiveUiReview /tmp/pr11-review/gui-evidence records-only
java -Djava.awt.headless=false -Duser.home=/tmp/pr11-review/notifications-home -Djava.util.prefs.userRoot=/tmp/pr11-review/notifications-prefs -Dreview.res=/path/to/team/Invaders-SDP-24592/res -ea -cp /tmp/pr11-review/helpers:/tmp/pr11-review/classes:/path/to/team/Invaders-SDP-24592/res LiveUiReview /tmp/pr11-review/notifications-evidence notifications
```

For PR #10, use its own classes/resources and the `notifications` mode. Each GUI invocation uses a fresh output/profile directory. Linux virtual-display captures were additionally checked with `-Dsun.java2d.xrender=false -Dsun.java2d.pmoffscreen=false`; all final PNG files retain the original pixels. The supplied buffer capture helper permits comparison without an X11 window.

`manifest.json` records SHA-256 hashes of the evidence files. No credentials or real player profiles are included.
