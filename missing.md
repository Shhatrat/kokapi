# KoKAPI — brakujące elementy względem OKAPI

Porównanie przeprowadzone względem OKAPI ver. 1962 (opencaching.pl).
Ostatnia aktualizacja: 2026-10-07.

---

## STATUS

Biblioteka implementuje wszystkie główne endpointy OKAPI wraz z pełnym zestawem opcjonalnych parametrów i pól modelu. Nie ma znanych istotnych luk.

---

## ZREALIZOWANE W OSTATNIEJ ITERACJI (2026-10-07)

- `Geocache` i `GeocacheDto` — dodano 15 pól wielojęzycznych i zaawansowanych: `names`, `descriptions`, `shortDescriptions`, `hints2`, `isRecommended`, `attrAcodes`, `attrNames`, `ocTeamAnnotation`, `attributionNote`, `distance`, `bearing`, `internalId`, `oxsize`, `protectionAreas`
- `CachesClient.get()` / `getMultiple()` — dodano parametry opcjonalne: `langpref`, `lpc`, `myLocation`, `userUuid`, `attributionAppend`, `ocTeamAnnotation`, `ownerFields`, `logFields`, `logUserFields`, `userLogsOnly`
- `CachesClient.searchAndRetrieve()` — rozbudowano o pełny zestaw filtrów (owner_uuid, name, size2, rating, min_rcmds, found_status, found_by, recommended_only, watched_only, ftf_hunter, set_and itd.)
- `ReplicateClient` — dodano `ChangelogObjectType` enum, pole `objectTypeRaw`/`objectType` i `logUuid` w `ChangelogEntry`, metodę `changelogAll()` z auto-paginacją
- `defaultFields` w `CachesClient` — uzupełniono o wszystkie nowe pola
- Testy: dodano 6 nowych testów (łącznie 105 przechodzi)

---

## PODSUMOWANIE STATYSTYK

| Kategoria | Liczba |
|---|---|
| Zaimplementowane endpointy | ~42 (wszystkie znane z OKAPI 1962) |
| Brakujące parametry w istniejących metodach | 0 |
| Brakujące pola w modelach | 0 |

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
