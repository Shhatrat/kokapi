# KoKAPI — brakujące elementy względem OKAPI

Porównanie przeprowadzone względem OKAPI ver. 1962 (opencaching.pl).
Ostatnia aktualizacja: 2026-10-07.

---

## STATUS

Biblioteka implementuje wszystkie główne endpointy OKAPI wraz z pełnym zestawem opcjonalnych parametrów i pól modelu. Nie ma znanych istotnych luk.

---

## ZREALIZOWANE W OSTATNIEJ ITERACJI (2026-10-07)

- **Refactor:** metody search/discovery wydzielone z `CachesClient` do nowego `CacheSearchClient` (dostępny jako `KokapiClient.search`)
- `CacheSearchClient.bboxFlow()` — auto-paginujący Flow dla wyszukiwania bbox
- `CacheSearchClient.allFlow()` — auto-paginujący Flow dla wyszukiwania site-wide
- `Callbacks.kt` — rozbudowany z 5 do 16 wrapperów (caches, search, logs, users)
- 145 testów, 0 błędów

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
| caches/search/nearest | `CacheSearchClient.nearest()` + `flow()` |
| caches/search/bbox | `CacheSearchClient.bbox()` + `bboxFlow()` |
| caches/search/all | `CacheSearchClient.all()` + `allFlow()` |
| caches/search/save | `CacheSearchClient.save()` |
| caches/search/by_urls | `CacheSearchClient.byUrls()` |
| caches/shortcuts/search_and_retrieve | `CacheSearchClient.andRetrieve()` |
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
