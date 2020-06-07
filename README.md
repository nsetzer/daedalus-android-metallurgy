# Daedalus-Android-Metallurgy

ORM wrapper for sqlite on android.


## Build

In the project root, run:

```bash
git submodule add 'https://github.com/nsetzer/daedalus-android-metallurgy' metallurgy
```

In `settings.gradle`, include ':metallurgy'

In `build.gradle`, under dependencies add `implementation project(':metallurgy')`