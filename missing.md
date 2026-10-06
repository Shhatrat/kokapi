# KoKAPI — brakujące elementy względem OKAPI

Porównanie przeprowadzone względem OKAPI ver. 1962 (opencaching.pl).
Ostatnia aktualizacja: 2026-10-06.

---

## STATUS

Biblioteka implementuje wszystkie główne endpointy OKAPI. Pozostałe luki to zaawansowane parametry opcjonalne oraz kilka ograniczeń w modelach.

---

## ISTNIEJĄCE ENDPOINTY Z BRAKUJĄCYMI PARAMETRAMI

### caches/geocache — brakujące parametry w `CachesClient.get()` i `getMultiple()`

Brakujące opcjonalne parametry zapytania:
- `langpref` (default: `en`) — preferowany język dla name, description, hint
- `attribution_append` (default: `full`) — `full`/`none`/`static` — jak dołączać notę atrybucyjną
- `oc_team_annotation` (OCPL, default: `description`) — `description`/`separate`
- `owner_fields` (default: `uuid|username|profile_url`) — pola użytkownika w `owner`
- `lpc` (default: `10`) — liczba logów w `latest_logs`
- `log_fields` (default: `uuid|date|user|type|comment`) — pola logów w `latest_logs`
- `log_user_fields` (default: `uuid|username|profile_url`) — pola użytkownika wewnątrz logów
- `user_logs_only` (default: `false`) — filtruj `latest_logs` tylko do wpisów zalogowanego
- `my_location` — `lat|lon` dla obliczenia `distance` i `bearing`
- `user_uuid` — UUID użytkownika do pól `is_found`, `is_recommended`, itp.

Brakujące pola w modelu `Geocache` (dostępne z OKAPI, ale nie requestowane ani nie mapowane):
- `names: Map<String, String>?` — wielojęzyczna mapa nazw
- `descriptions: Map<String, String>?` — wielojęzyczna mapa opisów HTML
- `shortDescriptions: Map<String, String>?` — wielojęzyczna mapa krótkich opisów
- `hints2: Map<String, String>?` — wielojęzyczna mapa wskazówek
- `attrAcodes: List<String>` — lista A-codes atrybutów (zamiast pełnych obiektów `Attribute`)
- `attrNames: List<String>` — lista nazw atrybutów w preferowanym języku
- `ocTeamAnnotation: String?` (OCPL) — adnotacja OC Team
- `attributionNote: String?` — nota atrybucyjna
- `internalId: Int?` — wewnętrzne ID (do replikacji)
- `isRecommended: Boolean?` — czy zalogowany rekomendował tę skrytkę
- `oxsize: Float?` — rozmiar w formacie Garmin/OC.com
- `protectionAreas: List<String>?` — lista obszarów chronionych
- `distance: Double?` — odległość od `my_location` (wymaga parametru `my_location`)
- `bearing: String?` — azymut do skrytki (wymaga `my_location`)

---

### caches/shortcuts/search_and_retrieve — ograniczone parametry w `CachesClient.searchAndRetrieve()`

Obsługuje tylko podstawowe filtry (type, status, terrain, difficulty, limit, offset).
Brakuje filtrów: owner_uuid, name, size2, rating, min_rcmds, found_status, found_by, recommended_only, watched_only, ftf_hunter, set_and, itp.

---

### replicate/changelog — niepełna obsługa w `ReplicateClient`

- `changelog[].object_type` — obsługiwane tylko `geocache`, brak obsługi `log_entry`
- `changelog[].change_type` — pole jest `String?` zamiast enum — brak rozróżnienia `replace`/`delete`
- `currentRevision()` nie obsługuje sytuacji gdy `more=true` (nie pobiera kolejnych stron)

---

## BRAKUJĄCE POLA W MODELACH

### model/Geocache.kt

Patrz sekcja wyżej — pola wielojęzyczne i zaawansowane.

---

## PODSUMOWANIE STATYSTYK

| Kategoria | Liczba |
|---|---|
| Zaimplementowane endpointy | ~40 (wszystkie główne) |
| Brakujące parametry w istniejących metodach | ~15 (zaawansowane/opcjonalne) |
| Brakujące pola w modelach | ~14 (głównie wielojęzyczne warianty i rzadko używane) |

## WSZYSTKIE ZAIMPLEMENTOWANE ENDPOINTY

| Endpoint OKAPI | Metoda KoKAPI |
|---|---|
| caches/geocache | `CachesClient.get()` |
| caches/geocaches | `CachesClient.getMultiple()` |
| caches/search/nearest | `CachesClient.search()` |
| caches/search/bbox | `CachesClient.bbox()` |
| caches/search/all | `CachesClient.searchAll()` |
| caches/search/save | `CachesClient.searchSave()` |
| caches/search/by_urls | `CachesClient.searchByUrls()` |
| caches/shortcuts/search_and_retrieve | `CachesClient.searchAndRetrieve()` |
| caches/mark | `CachesClient.mark()` |
| caches/save_personal_notes | `CachesClient.savePersonalNotes()` |
| caches/edit | `CachesClient.edit()` |
| caches/formatters/gpx | `FormattersClient.gpx()` |
| caches/formatters/garmin | `FormattersClient.garmin()` |
| caches/formatters/ggz | `FormattersClient.ggz()` |
| caches/map/tile | `FormattersClient.mapTile()` |
| logs/entry | `LogsClient.get()` |
| logs/entries | `LogsClient.getEntries()` |
| logs/logs | `LogsClient.getForCache()` |
| logs/userlogs | `LogsClient.getUserLogs()` |
| logs/submit | `LogsClient.submit()` |
| logs/delete | `LogsClient.delete()` |
| logs/edit | `LogsClient.edit()` |
| logs/capabilities | `LogsClient.capabilities()` |
| logs/images/add | `LogsClient.addImage()` |
| logs/images/edit | `LogsClient.editImage()` |
| logs/images/delete | `LogsClient.deleteImage()` |
| users/by_username | `UsersClient.byUsername()` |
| users/by_uuid | `UsersClient.byUuid()` |
| users/users | `UsersClient.byUuids()` |
| users/by_usernames | `UsersClient.byUsernames()` |
| users/by_internal_id | `UsersClient.byInternalId()` |
| users/by_internal_ids | `UsersClient.byInternalIds()` |
| users/user | `UsersClient.me()` |
| attrs/attribute | `AttrsClient.attribute()` |
| attrs/attributes | `AttrsClient.attributes()` |
| attrs/attribute_index | `AttrsClient.attributeIndex()` |
| replicate/changelog | `ReplicateClient.changelog()` |
| replicate/fulldump | `ReplicateClient.fullDump()` |
| replicate/info | `ReplicateClient.info()` |
| apisrv/installation | `ApiSrvClient.installation()` |
| apisrv/installations | `ApiSrvClient.installations()` |
| apisrv/stats | `ApiSrvClient.stats()` |
| apiref/method | `ApiRefClient.method()` |
| apiref/method_index | `ApiRefClient.methodIndex()` |
| apiref/issue | `ApiRefClient.reportIssue()` |
