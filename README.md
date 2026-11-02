# Mochi

A small mint-green pet that shows up when you spend too long on Instagram.

If you've been scrolling for an hour (or it's the middle of the night), Mochi walks across your screen five times,
each time with a different sarcastic message. After the fifth visit Instagram gets locked, and to get back in you
have to answer three of Mochi's questions.

Everything runs on the phone. The app has no internet permission.

## What it does

- **Doomscroll check:** Instagram open for 60 minutes straight and Mochi shows up. A break of two minutes or less
  doesn't reset the timer.
- **Bedtime check:** Instagram open between 11 PM and 5 AM and Mochi shows up sleepy. This wins over the doomscroll check.
- **Five visits:** each visit has its own message and they get meaner as they go. Lines already used are saved,
  so you don't see the same ones again until a whole set has run out.
- **Lock:** after the fifth visit Instagram is locked. Mochi opens a lock screen and asks three random questions.
  Answers need at least three words, and Mochi comments on every one. Answer all three and the lock goes away.
- **Settings:** doomscroll limit, bedtime, wake time, cooldown (30 minutes by default), and a switch to turn the
  lock off.
- **Preview button** on the home screen so you can see Mochi without waiting an hour.

The lock is a speed bump, not a real block. Android doesn't let one app disable another, so anyone can still
revoke Mochi's permissions or uninstall it.

## How it works

1. A foreground service (`DoomscrollWatchService`) checks every 30 seconds while Instagram is open and every 60
   seconds otherwise. While the lock is on it checks every 2 seconds. It stops while the screen is off.
2. `InstagramSessionTracker` reads the activity resumed/paused events from `UsageStatsManager` for the last three
   hours. `SessionCalculator` turns them into the length of the current session.
3. `NudgeRules.decide` returns bedtime, doomscroll or nothing, based on the session, the time and the cooldown.
4. `NudgeWatcher` runs the five visits through `FloatingMochiOverlay`, then locks the app and opens `LockActivity`.

No accessibility service is used. Mochi only knows when Instagram comes to the foreground, not what is in it.
The downside of polling is that a nudge can be up to about 30 seconds late.

The overlay is a small window that doesn't take touches, so Instagram keeps working underneath. Mochi is drawn
with Compose `Canvas`, there are no image files.

## Code layout

One Gradle module, `:app`, split by package under `com.mochi`:

```
app/             Application, MainActivity, navigation
core/model/      Nudge, NudgeSettings, messages, unlock questions
core/rules/      NudgeRules, SessionCalculator, MessagePicker (plain Kotlin, no Android)
core/data/       DataStore settings, usage stats, permission checks, Hilt module
core/designsystem/  theme and the Mochi drawing
feature/         onboarding, home, settings, overlay, lock
```

Each screen has a ViewModel exposing a `StateFlow` of its UI state. Anything that depends on time takes a `Clock`,
so the tests can control it.

## Tech

Kotlin, Jetpack Compose with Material 3, Navigation Compose, Hilt, Coroutines/Flow, DataStore. Tests use JUnit,
Truth, Turbine and Robolectric. ktlint (through Spotless) and detekt for style. minSdk 29, compileSdk 37.

## Permissions

| Permission | Used for |
| --- | --- |
| Usage access | knowing when Instagram is in the foreground |
| Display over other apps | drawing Mochi, and opening the lock screen on top of Instagram |
| Notifications (Android 13+) | the quiet notification a foreground service needs; optional |
| Foreground service (special use) | the watcher service |
| Boot completed | restarting the watcher after a reboot, if it was on |

Nothing leaves the device. There is no analytics, no ads and no backup of app data.

## Build and run

You need Android Studio (or the Android SDK) and JDK 17 or newer. Put your SDK path in `local.properties`
(Android Studio does this for you).

```bash
./gradlew assembleDebug     # APK in app/build/outputs/apk/debug/
./gradlew installDebug      # with a device or emulator connected
./gradlew test              # unit tests
./gradlew spotlessCheck detekt
```

On Windows use `gradlew.bat`.

### Trying it on a phone

1. Install the debug build, open Mochi and grant usage access and overlay permission.
2. Press **Preview** on the home screen to see Mochi walk across.
3. Turn on **Floating Mochi**, then go to Settings, Debug tools, and pick **Doomscroll after 1 minute**.
4. Open Instagram and wait. The five visits take about 40 seconds, then the lock screen appears.

## Ideas for later

- Check the unlock answers with an on-device model instead of only counting words
- More apps besides Instagram (the list is already in `WatchedApps`)
- Home screen widget
- Weekly summary

## License

MIT, see [LICENSE](LICENSE).
