# UniFind – Honest App Review and Rating (1 to 10)

**Project Context**: Offline Android Campus Classroom Locator for Batangas State University TNEU ARASOF - Nasugbu. Built in Kotlin + Jetpack Compose for an OOP class project.

---

## 1. Functionality and Correctness: 8.5 / 10
- **Strengths**:
  - Full offline routing pipeline using Dijkstra (`DijkstraRouteFinder.kt`) and alternative path penalization (`AlternativeRouteFinder.kt`).
  - Search engine (`SearchEngine.kt`) handles polymorphic matching for Gates, Buildings, Facilities, and Rooms with query normalization.
  - "I'm here" flow (`CurrentLocationHolder.kt`, `MapScreen.kt`) allows setting user location on the map and routing directly from "My location".
  - Nearest Gate auto-detection (`NearestGateFinder.kt`) correctly ranks all 4 campus gates by walking distance.
  - Interactive map graph editor (`GraphEditorState.kt`, `EditorUI.kt`) and graph validation tool built into `MapScreen.kt`.
- **Weaknesses**:
  - In `DirectionsScreen.kt`: Swap button resets `targetRoom` to `null`. Swapping when "From" is "My location" or "Nearest gate (auto)" sets "From" to `toLocation` and clears "To".
  - In `DirectionsViewModel.kt`: If `toLocation` is an `UNDER_CONSTRUCTION` building (`sto_tomas`), Dijkstra still routes to its entrance node `n9`.
- **Most Valuable Improvement**: Refactor `swapLocations()` in `DirectionsViewModel.kt` to properly exchange "From" and "To" locations when special location modes ("My location", "Nearest gate") are active.

---

## 2. OOP Design: 9.0 / 10
- **Strengths**:
  - **Hierarchy**: `abstract class Location` (`Location.kt`) extended by `Gate`, `Facility`, `Building`. Implements `Searchable` and `Navigable` interfaces.
  - **Composition**: `Building` composed of `Floor`, composed of `Room` (`Building.kt`, `Floor.kt`, `Room.kt`).
  - **Encapsulation**: Backing private fields with getters (`Location.mapX`, `Location.mapY` with `private set`), ViewModels exposing read-only `StateFlow`.
  - **Polymorphism**: `describe()` abstract method in `Location.kt` overridden by subclasses. `Searchable.matches()` called polymorphically in `SearchEngine.kt`.
  - **Strategy & Memento Patterns**: `RouteFinder` interface strategy; `GraphEditorState.kt` memento undo deque.
- **Weaknesses**:
  - `BuildingDetailViewModel.kt` duplicates room search normalization logic (`getFilteredRooms()`) rather than delegating room matching to `Room.matches()`.
  - `Location.kt` implements `Searchable` and `Navigable`, but `Room.kt` implements `Searchable` directly without extending `Location`.
- **Most Valuable Improvement**: Refactor `BuildingDetailViewModel.getFilteredRooms()` to delegate to `Room.matches(query)` to eliminate duplicated search normalization.

---

## 3. Code Quality and Maintainability: 8.5 / 10
- **Strengths**:
  - Strict compliance with AGENTS.md rule: One class per file, clean package structure (`data`, `data/dto`, `model`, `routing`, `search`, `ui`).
  - Explicit KDoc headers on every class identifying the specific OOP concept used.
  - No dead code, unused imports, or unresolved TODOs found across source files.
- **Weaknesses**:
  - Magic numbers: `0.2568` meters per pixel, image dimensions `941` x `1171` hardcoded across multiple files (`NearestLocationFinder.kt`, `NearestNodeFinder.kt`, `GraphEditorState.kt`, `ZoomableMap.kt`, `MapCoordinates.kt`).
  - Search string normalization (`replace(Regex("[ .-]"), "").lowercase()`) repeated in 8+ files.
- **Most Valuable Improvement**: Extract map constants (`IMAGE_WIDTH = 941`, `IMAGE_HEIGHT = 1171`, `METERS_PER_PIXEL = 0.2568`) and string normalization (`String.normalizeSearch()`) into a shared utility file.

---

## 4. Data Quality: 9.0 / 10
- **Strengths**:
  - `campus.json` contains 20 locations (4 gates, 4 facilities, 12 buildings) and 166 rooms across 7 colleges.
  - `graph.json` contains 49 waypoints and 50 edges. Every location in `campus.json` resolves to at least 1 entrance node in `graph.json`.
  - Complete graph connectivity: All 49 nodes form 1 connected component with 0 isolated nodes or unreachable locations.
- **Weaknesses**:
  - Node IDs in `graph.json` skip n18, n32, n50 (non-sequential string IDs).
  - Gate 1 `campus.json` coordinates vs `graph.json` node `n49` have a slight 4-meter offset snapped by `fromPoint()`.
- **Most Valuable Improvement**: Add penalty weighting or status check in `CampusGraph` / `RoutePlanner` when routing to buildings with `status == UNDER_CONSTRUCTION`.

---

## 5. UI/UX and Visual Polish: 8.0 / 10
- **Strengths**:
  - Consistent Material 3 theme (`UniFindTheme` in `Theme.kt`) with brand palette (Ultramarine Blue `#2B3FD6`, Teal `#00A6A6`) and `PlusJakartaSans` font family.
  - Smooth vector canvas overlays for routes (glowing cyan line for primary route, dashed gray line for alternative route).
  - Clear empty states in search and directions, automatic soft keyboard hiding via `LocalSoftwareKeyboardController`.
- **Weaknesses**:
  - Touch targets: Small text action buttons in `EditorToolbar.kt` and `DirectionsScreen.kt` may fall below 48dp on smaller screens.
  - Map view height in `DirectionsScreen.kt` shrinks significantly on short screens (<640dp height) when top selection and bottom summary cards expand.
- **Most Valuable Improvement**: Make the bottom route summary card collapsible or wrap `DirectionsScreen.kt` in a responsive layout so the map canvas retains at least 50% screen height on short devices.

---

## 6. Navigation and State Handling: 7.5 / 10
- **Strengths**:
  - Centralized `AppNavigation.kt` with Jetpack Navigation Compose and URL-encoded arguments.
  - `CurrentLocationHolder` Activity-scoped ViewModel provides single source of truth for user location.
  - `launchSingleTop = true` and `popUpTo(findStartDestination().id)` prevent back stack accumulation.
- **Weaknesses**:
  - Map zoom level (`scale`) and pan (`offset`) in `ZoomableMap.kt` use `remember { mutableStateOf(...) }`, resetting map zoom to 1x on device rotation.
  - Search state (`query`, `filter`) resets when tapping a result; navigating back from Building Detail clears previous search query.
- **Most Valuable Improvement**: Use `rememberSaveable` for `scale` and `offset` state in `ZoomableMap.kt` so zoom/pan positions survive device rotation.

---

## 7. Performance and Smoothness: 8.5 / 10
- **Strengths**:
  - `LazyColumn` and `LazyRow` with explicit `key` parameters used across list screens.
  - `CampusRepository` uses `by lazy` for JSON parsing to avoid main-thread re-decoding.
  - `ZoomableMap.kt` uses `graphicsLayer` for hardware-accelerated transforms (scale, translation, rotation) with zero object allocation during touch gestures.
- **Weaknesses**:
  - Map pins in `MapScreen.kt` recompose during map zoom/pan gestures.
- **Most Valuable Improvement**: Wrap pin position calculations in `derivedStateOf` or filter visible pins by viewport bounds in `MapScreen.kt`.

---

## 8. Robustness and Edge Cases: 8.0 / 10
- **Strengths**:
  - Under-construction building handling: `Sto. Tomas Building` displays "Coming soon" chip in `BuildingDetailScreen.kt`.
  - Empty search queries display grouped category catalog.
  - Coordinate clamping (`[0, 1]`) in `Location.updateCoordinates()`.
  - 100% offline execution with zero network dependency.
- **Weaknesses**:
  - Extremely long custom "My location" text labels can overflow canvas bounds in `MyLocationDot.kt`.
- **Most Valuable Improvement**: Truncate custom user location labels in `MyLocationDot.kt` (`take(25) + "..."`).

---

## 9. Testing: 9.0 / 10
- **Strengths**:
  - 12 comprehensive unit test files covering graph routing, Dijkstra, alternative routes, nearest gate finder, nearest location finder, search engine, destination ranker, campus repository, graph editor state, map coordinates math, and summary formatting.
  - 100% test pass rate (`gradle_build("test")` passes cleanly in < 2 seconds).
  - Asset verification test (`CampusRepositoryUnitTest.kt`) verifies every location in `campus.json` resolves to itself by ID and matches `graph.json` entrance nodes.
- **Weaknesses**:
  - No ViewModel-level unit tests (`DirectionsViewModelTest`, `MapViewModelTest`).
- **Most Valuable Improvement**: Add `DirectionsViewModelTest.kt` to unit test ViewModel state updates during location swapping and auto-nearest gate selection.

---

## 10. Accessibility: 7.5 / 10
- **Strengths**:
  - `contentDescription` provided for all standard icons.
  - High color contrast between text and background in both light and dark themes.
  - Font scaling capped to 1.15f in bottom navigation bar to prevent text wrapping on large system font sizes.
- **Weaknesses**:
  - Custom canvas map pins in `MapScreen.kt` lack Compose `semantics` for TalkBack screen reader accessibility.
- **Most Valuable Improvement**: Add `.semantics { contentDescription = location.name; role = Role.Button }` to `MapPin`.

---

## 11. Presentation Readiness: 8.5 / 10
- **Strengths**:
  - 100% offline capability with zero risk of network failure during live presentation.
  - Smooth animation transitions between tabs and screens.
- **5 Riskiest Live Demo Actions**:
  1. Tapping Sto. Tomas Building and expecting rooms (status is Under Construction).
  2. Selecting "From: My location" without placing a location dot on map first.
  3. Tapping "Swap" when "From" is "Nearest gate (auto)".
  4. Rotating phone during map calibration or graph editing.
  5. Live-editing graph nodes in front of teacher and tapping "Clear All".
- **Safe Demo Path**:
  1. Home Screen -> Quick Access Cards.
  2. Colleges -> CICS -> Sto. Tomas (Under Construction) & CTEB.
  3. Search -> Type "HEB101" -> Open Building Detail screen.
  4. Directions -> Gate 1 to Library -> Show fastest vs alternative route.
  5. Map -> "I'm here" -> Place dot -> Route to Joson Gymnasium.
- **Most Valuable Improvement**: Add a toast/banner in Directions screen when an under-construction building is selected.

---

## 12. Documentation Alignment: 8.5 / 10
- **Strengths**:
  - Code maps directly to standard class diagrams and OOP concepts.
  - KDoc comments on every class explicitly name OOP concepts (Abstraction, Inheritance, Encapsulation, Polymorphism, Composition, Strategy, Facade, Memento).
- **Weaknesses**:
  - `CurrentLocationHolder.kt` naming differs slightly from standard `UserLocationViewModel`.
- **Most Valuable Improvement**: Update KDoc in `CurrentLocationHolder.kt` to clarify its role as an Activity-Scoped State ViewModel.

---

## Overall Score: 8.4 / 10

**Justification**: UniFind is an exceptionally well-engineered, 100% offline Android campus navigation app built specifically for Batangas State University TNEU ARASOF - Nasugbu. It satisfies all core academic and technical requirements: robust OOP architecture (abstract base class `Location`, interfaces `Searchable`/`Navigable`/`RouteFinder`, composition `Building`/`Floor`/`Room`, polymorphism, and encapsulation), a full Dijkstra pathfinding and alternative route engine, a rich interactive campus map with custom coordinates snapping, live graph editing/validation tools, and an extensive unit test suite with 100% passing tests.

---

### Category Summary Table

| # | Category | Score | Key Strengths & Known Gaps |
|---|---|---|---|
| 1 | Functionality & Correctness | 8.5 | Full offline Dijkstra + alt routes, 4 gates ranking, "I'm here" flow. Gap: Swap logic with auto-gate/my-location resets "To". |
| 2 | OOP Design | 9.0 | Strong `Location` hierarchy, `Searchable`/`Navigable`/`RouteFinder` interfaces, composition, encapsulation. Gap: Duplicated search normalization in `BuildingDetailViewModel`. |
| 3 | Code Quality & Maintainability | 8.5 | 1 class per file, clean packages, 0 dead code/TODOs. Gap: Magic numbers (map dimensions & mpp) duplicated across files. |
| 4 | Data Quality | 9.0 | 20 campus locations, 166 rooms, 49 graph waypoints, 100% graph connectivity. Gap: Node IDs skip n18/n32/n50. |
| 5 | UI/UX & Visual Polish | 8.0 | Material 3 theme, PlusJakartaSans typography, glowing route canvas drawings. Gap: Map canvas height shrinks on short screens. |
| 6 | Navigation & State Handling | 7.5 | SingleTop navigation, Activity-scoped location holder. Gap: Map zoom/pan in `ZoomableMap` resets on device rotation (`remember` vs `rememberSaveable`). |
| 7 | Performance & Smoothness | 8.5 | Hardware-accelerated `graphicsLayer` transforms, fast unit tests (<2s), `LazyColumn` key usage. Gap: Map pins recompose during pan/zoom. |
| 8 | Robustness & Edge Cases | 8.0 | "Under construction" building status handled, coordinate clamping `[0,1]`, offline math. Gap: Long custom "My location" text labels overflow map canvas. |
| 9 | Testing | 9.0 | 12 test files, 100% test pass rate, comprehensive graph/routing/search tests. Gap: Missing ViewModel-level unit tests. |
| 10 | Accessibility | 7.5 | Clear `contentDescription` on standard icons, high text contrast. Gap: Map canvas pins lack explicit TalkBack accessibility semantics. |
| 11 | Presentation Readiness | 8.5 | Fully offline, zero network risk, smooth demo flow. Gap: Demo risk if user swaps locations from "Nearest gate (auto)". |
| 12 | Documentation Alignment | 8.5 | Clean match between code, KDoc headers, and class diagram OOP concepts. Gap: `CurrentLocationHolder` name vs typical ViewModel naming. |

---

### Top 10 Issues (Ranked by Severity)

1. **[Medium - Functionality/UX] Directions Screen Location Swap Bug**
   - **File**: `DirectionsViewModel.kt` (`swapLocations()`)
   - **Description**: Swapping when "From" is "Nearest gate (auto)" or "My location" sets "From" to `toLocation` and clears "To".
   - **Suggested Fix**: Update `swapLocations()` to swap `_fromLocation` with `_toLocation` cleanly in all modes.

2. **[Medium - State Handling] Map Zoom and Pan Resets on Screen Rotation**
   - **File**: `ZoomableMap.kt`
   - **Description**: Map scale and offset use `remember { mutableStateOf(...) }`, resetting map zoom to 1x on rotation.
   - **Suggested Fix**: Use `rememberSaveable` or store zoom/pan state in `MapViewModel`.

3. **[Medium - Code Quality] Duplicated Search String Normalization**
   - **File**: `Location.kt`, `Room.kt`, `SearchEngine.kt`, `CampusRepository.kt`, `BuildingDetailViewModel.kt`
   - **Description**: The regex normalization `.replace(Regex("[ .-]"), "").lowercase()` is duplicated across 8+ source files.
   - **Suggested Fix**: Create a shared extension function `fun String.normalizeSearch(): String`.

4. **[Medium - Code Quality] Magic Numbers for Map Dimensions and Scale**
   - **File**: `NearestLocationFinder.kt`, `NearestNodeFinder.kt`, `GraphEditorState.kt`, `ZoomableMap.kt`, `MapCoordinates.kt`
   - **Description**: Image dimensions `941`, `1171` and meters-per-pixel `0.2568` are hardcoded across multiple domain classes.
   - **Suggested Fix**: Define a `CampusMapDefaults` object containing `IMAGE_WIDTH = 941`, `IMAGE_HEIGHT = 1171`, `METERS_PER_PIXEL = 0.2568`.

5. **[Low - Data Persistence] Calibrated Location Coordinates Do Not Persist Across App Restarts**
   - **File**: `MapViewModel.kt`
   - **Description**: Dragging pins in calibration mode updates in-memory `Location` instances without saving to persistent storage.
   - **Suggested Fix**: Store updated coordinates in `SharedPreferences` or `DataStore`.

6. **[Low - Robustness] Canvas Text Truncation for "My Location" Dot**
   - **File**: `MyLocationDot.kt`
   - **Description**: Long custom user location labels can render past screen boundaries on the map canvas.
   - **Suggested Fix**: Add text truncation (`take(25) + "..."`) in `MyLocationDot.kt`.

7. **[Low - OOP Cleanliness] Logic Duplication in BuildingDetailViewModel**
   - **File**: `BuildingDetailViewModel.kt` (`getFilteredRooms()`)
   - **Description**: Re-implements string matching for room names and owner college instead of calling `Room.matches(query)`.
   - **Suggested Fix**: Delegate room filtering to `room.matches(query)`.

8. **[Low - Accessibility] Map Pins Lack Screen Reader Accessibility Semantics**
   - **File**: `MapPin.kt`
   - **Description**: Interactive canvas pins lack explicit Compose `semantics` for TalkBack screen readers.
   - **Suggested Fix**: Add `.semantics { contentDescription = location.name; role = Role.Button }` to `MapPin`.

9. **[Low - UI Layout] Directions Screen Map Height Shrinks on Small Devices**
   - **File**: `DirectionsScreen.kt`
   - **Description**: On small vertical height devices (<640dp), top selection and bottom summary cards leave minimal height for the map.
   - **Suggested Fix**: Make the bottom route summary card collapsible.

10. **[Low - Testing] Missing ViewModel Unit Tests**
    - **File**: `app/src/test/java/com/example/unifind/ui/`
    - **Description**: No direct unit tests exist for `DirectionsViewModel` or `MapViewModel`.
    - **Suggested Fix**: Add `DirectionsViewModelTest.kt` to unit test ViewModel state updates.

---

### Quick Wins (Top 5 Changes Under 30 Mins Each)

1. **Fix Directions Swap Logic**: Update `swapLocations()` in `DirectionsViewModel.kt`. (10 mins)
2. **Extract `normalizeSearch()` Helper**: Create `StringUtils.kt` with `fun String.normalizeSearch(): String`. (15 mins)
3. **Add `rememberSaveable` to Map Zoom/Pan**: Update `ZoomableMap.kt` to preserve scale/offset across rotations. (15 mins)
4. **Delegate Room Matching in `BuildingDetailViewModel`**: Refactor `getFilteredRooms()` to call `room.matches(query)`. (10 mins)
5. **Truncate Long "My Location" Label**: Add text truncation in `MyLocationDot.kt`. (10 mins)

*What is NOT worth doing before Nov 23*:
- Do NOT rewrite navigation using a third-party library.
- Do NOT replace JSON assets with Room database.
- Do NOT replace `campus_map.png` with complex OpenGL vector rendering.

---

### Freeze Checklist (Manual Testing Before Submission)

- [ ] **1. Offline Mode Check**: Enable Airplane Mode. Verify Home, Search, Map, Colleges, and Directions work offline.
- [ ] **2. Search Engine Verification**: Search for `HEB101`, `Library`, `Gate 1`, `Higher Education`. Verify exact matches rank first.
- [ ] **3. Directions Flow**: Select Gate 1 to Library -> Check cyan route -> Toggle Fastest vs Alternative route.
- [ ] **4. "I'm Here" User Location**: Map -> "I'm here" -> Place dot -> Directions -> "From: My location" -> "To: Joson Gymnasium".
- [ ] **5. Under Construction Building**: Colleges -> CICS -> Sto. Tomas Building -> Verify "Coming soon" chip.
- [ ] **6. Map Zoom & Rotation**: Pinch-to-zoom, pan, two-finger rotate -> Tap "Reset North" compass FAB -> Rotate device.
