# Namma-HomeStay — UI/UX Polish (Updates)

## What was fixed / improved
- **Consistent top bar** across all screens: moved to the app-level `Scaffold` (no duplicate/shifted headers).
- **Removed bottom padding hacks** (`padding(bottom = 80.dp)`) and let `Scaffold` handle system insets + bottom navigation correctly.
- **Smoother forms (no “reset while typing”)**
  - `My Home` and `Today’s Menu` fields now use `rememberSaveable` + a first-load sync so Firestore/Flow updates don’t overwrite user typing mid-edit.
- **Fixed “Open in Maps”**
  - Visitor Preview “Open in Maps” now actually opens Google Maps using the saved URL (or a geo search fallback).
  - Local Guide “View on Maps” now has the same robust behavior.
- **Local Guide (Karnataka-focused)**
  - Added **30 preloaded Coastal Karnataka locations** (beaches, islands, estuaries, forts).
  - Fixed **scrolling** (list now properly scrolls).
  - “Add Spot” now shows a clear success/error message and new spots appear at the top (newest first).
- **Removed photo upload UI** (per request)
  - Removed “Add photo” sections from Home Profile and Today’s Menu screens (backend methods remain, but UI is simplified).
- **Save buttons + live updates**
  - “Save Profile” and “Save Today” now show a success/error message (useful when Firestore rules/config blocks saving).
  - Live updates sync when you are not editing; while typing, the app won’t overwrite your text.

## Files changed (high level)
- `app/src/main/java/com/namma/homestay/ui/NammaHomeStayApp.kt`
- `app/src/main/java/com/namma/homestay/ui/screens/HomeProfileScreen.kt`
- `app/src/main/java/com/namma/homestay/ui/screens/DailyMenuScreen.kt`
- `app/src/main/java/com/namma/homestay/ui/screens/InquiryBoxScreen.kt`
- `app/src/main/java/com/namma/homestay/ui/screens/LocalGuideScreen.kt`
- `app/src/main/java/com/namma/homestay/ui/screens/VisitorPreviewScreen.kt`

## Build verification
- `assembleDebug` + `lintDebug` completed successfully (lint HTML report is generated in `app/build/reports/`).
