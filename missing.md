# KoKAPI — brakujące elementy względem OKAPI

Porównanie przeprowadzone względem OKAPI ver. 1962 (opencaching.pl).
Data porównania: 2026-10-06.

---

## SPIS TREŚCI

1. [Brakujące endpointy](#brakujące-endpointy)
2. [Istniejące endpointy z brakującymi parametrami](#istniejące-endpointy-z-brakującymi-parametrami)
3. [Brakujące pola w modelach](#brakujące-pola-w-modelach)
4. [Brakujące DTO / mapowania](#brakujące-dto--mapowania)

---

## BRAKUJĄCE ENDPOINTY

### caches/edit
**OKAPI:** `POST services/caches/edit`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `cache_code` (wymagany) — kod skrytki do edycji
- `passwd` (opcjonalny) — nowe hasło do logowania "Found it"/"Attended"; pusty string = usuń hasło
- `langpref` (opcjonalny, default: `en`) — preferowany język komunikatów zwrotnych
**Opis:** Pozwala właścicielowi skrytki zmieniać jej właściwości (np. hasło do znalezienia).
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/mark
**OKAPI:** `POST services/caches/mark`
**Auth:** Level 3 (OAuth token)
**Parametry:** brak (poza standardowymi OAuth)
**Opis:** Oznaczanie/odznaczanie skrytki jako obserwowanej lub ignorowanej przez zalogowanego użytkownika.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/save_personal_notes
**OKAPI:** `POST services/caches/save_personal_notes`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `cache_code` (wymagany) — kod skrytki
- `new_value` (wymagany) — nowa treść notatki (pusty string = usuń notatkę)
- `old_value` (opcjonalny, default: `""`) — poprzednia treść notatki (optimistic locking)
**Opis:** Zapisuje prywatne notatki użytkownika do skrytki. Zwraca `saved_value` i `replaced`.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/search/all
**OKAPI:** `GET services/caches/search/all`
**Auth:** Level 1
**Parametry (wszystkie opcjonalne):**
- `type` — pipe-separated lista typów skrytek (np. `Traditional|Multi`)
- `status` (default: `Available`) — pipe-separated lista statusów (`Available`, `Temporarily unavailable`, `Archived`)
- `owner_uuid` — pipe-separated lista UUID właścicieli
- `name` — wzorzec nazwy z wildcard `*` i `_`
- `terrain` (default: `1-5`) — zakres trudności terenu, format `X-Y`
- `difficulty` (default: `1-5`) — zakres trudności znalezienia, format `X-Y`
- `size2` — pipe-separated lista rozmiarów (`none`, `nano`, `micro`, `small`, `regular`, `large`, `xlarge`, `other`)
- `rating` (OCPL, default: `1-5`) — zakres oceny, format `X-Y`
- `min_rcmds` — minimalna liczba rekomendacji (int lub `N%`)
- `min_founds` — minimalna liczba znalezień
- `max_founds` — maksymalna liczba znalezień
- `modified_since` — ISO 8601, skrytki zmodyfikowane od tej daty
- `found_status` (default: `either`, wymaga Level 3) — `found_only` / `notfound_only` / `either`
- `found_by` — pipe-separated lista UUID użytkowników
- `not_found_by` — pipe-separated lista UUID użytkowników
- `recommended_only` (default: `false`, wymaga Level 3) — tylko rekomendowane przez zalogowanego
- `recommended_by` — pipe-separated lista UUID użytkowników
- `watched_only` (default: `false`, wymaga Level 3) — tylko obserwowane przez zalogowanego
- `ignored_status` (default: `either`, wymaga Level 3) — `ignored_only` / `notignored_only` / `either`
- `exclude_my_own` (default: `false`, wymaga Level 3) — wyklucz własne skrytki
- `with_trackables_only` (default: `false`) — tylko skrytki z trackables
- `ftf_hunter` (default: `false`) — tylko skrytki nieznalezione przez nikogo
- `powertrail_only` (OCPL, default: `false`) — tylko skrytki z aktywnych geopath
- `powertrail_ids` (OCPL, deprecated) — pipe-separated lista ID geopath
- `set_and` — ID zbioru z search/save do filtrowania AND
- `limit` (default: `100`) — max liczba wyników (1..500)
- `offset` (default: `0`) — paginacja (offset + limit <= 500)
- `order_by` — pipe-separated sortowanie, np. `-rcmds%|+name`
**Opis:** Ogólne wyszukiwanie skrytek z wieloma filtrami. Zwraca listę kodów + flaga `more`.
**Brakuje w:** KoKAPI nie ma tej funkcji w ogóle. `search()` używa tylko `nearest`, bez żadnych filtrów.

---

### caches/search/by_urls
**OKAPI:** `GET services/caches/search/by_urls`
**Auth:** Level 1
**Parametry:**
- `urls` (wymagany) — pipe-separated lista URLi stron skrytek
**Opis:** Zwraca kody skrytek odpowiadające podanym URLom strony OC.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/search/save
**OKAPI:** `GET services/caches/search/save`
**Auth:** Level 1
**Parametry:**
- Przyjmuje te same parametry co `search/all` (pełen zestaw filtrów)
**Opis:** Zapisuje zbiór wyników wyszukiwania po stronie serwera i zwraca ID zestawu (`set_id`). Zestaw może być następnie używany z parametrem `set_and` w innych metodach wyszukiwania.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/shortcuts/search_and_retrieve
**OKAPI:** `GET services/caches/shortcuts/search_and_retrieve`
**Auth:** Level 1
**Parametry:**
- `retr_method` (wymagany) — metoda pobierania danych po wyszukaniu (np. `services/caches/geocaches`)
- `retr_params` (wymagany) — parametry przekazywane do `retr_method` (JSON)
- `wrap_retr_params` (opcjonalny, default: `false`) — czy owijać parametry
- Dziedziczy parametry z `search/all` (typ, status, teren, trudność, itp.)
**Opis:** Skrót łączący wyszukiwanie i pobieranie danych w jednym żądaniu. Znacznie efektywniejsze niż osobne wywołania `search` + `geocaches`.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/formatters/gpx
**OKAPI:** `GET services/caches/formatters/gpx`
**Auth:** Level 1
**Parametry:**
- `cache_codes` (wymagany) — pipe-separated lista kodów skrytek
- `langpref` (opcjonalny, default: `en`)
- `ns_ground` (opcjonalny) — czy dołączyć Groundspeak namespace
- `ns_gsak` (opcjonalny) — czy dołączyć GSAK namespace
- `ns_ox` (opcjonalny) — czy dołączyć OpenCaching namespace
- `images` (opcjonalny) — czy dołączyć obrazki
- `attrs` (opcjonalny) — format atrybutów
- `trackables` (opcjonalny) — czy dołączyć trackables
- `alt_wpts` (opcjonalny) — czy dołączyć dodatkowe punkty
- `location_source` (opcjonalny, default: `default-coords`)
- `mark_found` (opcjonalny) — oznacz znalezione skrytki
**Opis:** Eksportuje skrytki do formatu GPX (do urządzeń GPS, np. Garmin).
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/formatters/ggz
**OKAPI:** `GET services/caches/formatters/ggz`
**Auth:** Level 1
**Parametry:** podobne do `formatters/gpx`
**Opis:** Eksportuje skrytki do formatu GGZ (skompresowany GPX dla Garmin).
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/formatters/garmin
**OKAPI:** `GET services/caches/formatters/garmin`
**Auth:** Level 1
**Parametry:** podobne do `formatters/gpx`
**Opis:** Eksportuje skrytki do formatu zoptymalizowanego pod urządzenia Garmin.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### caches/map/tile
**OKAPI:** `GET services/caches/map/tile`
**Auth:** Level 1
**Parametry:**
- `z` (wymagany) — zoom level
- `x` (wymagany) — X tile coordinate
- `y` (wymagany) — Y tile coordinate
- `status` (opcjonalny) — filtr statusów
- `type` (opcjonalny) — filtr typów
- (pozostałe filtry jak w `search/all`)
**Opis:** Zwraca kafelek mapy PNG z zaznaczonymi skrytkami (protokół map tiles, np. do wyświetlania na mapach Leaflet/OSM).
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/edit
**OKAPI:** `POST services/logs/edit`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `log_uuid` (wymagany) — UUID wpisu do edycji (musi być autorem)
- `logtype` (opcjonalny) — nowy typ wpisu
- `password` (opcjonalny) — hasło (wymagane przy zmianie na "Found it"/"Attended")
- `comment` (opcjonalny) — nowa treść komentarza (wymaga `comment_format`)
- `comment_format` (opcjonalny) — `html` lub `plaintext`
- `when` (opcjonalny) — nowa data wpisu (ISO 8601)
- `recommend` (opcjonalny, default: `unchanged`) — `true`/`false`/`unchanged`
- `rating` (OCPL, opcjonalny, default: `unchanged`) — ocena 1-5 lub `false` (wycofaj ocenę)
- `langpref` (opcjonalny, default: `en`)
**Opis:** Edycja istniejącego wpisu dziennika. Wymaga bycia autorem wpisu.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/entries
**OKAPI:** `GET services/logs/entries`
**Auth:** Level 1
**Parametry:**
- `log_uuids` (wymagany) — pipe-separated lista UUID wpisów (max 500, może być pusta)
- `fields` (opcjonalny, default: `date|user|type|comment`) — jakie pola zwrócić
- `user_fields` (opcjonalny, default: `uuid|username|profile_url`)
**Opis:** Pobiera wiele wpisów dziennika naraz (bulk). Odpowiednik `logs/entry` dla wielu UUID jednocześnie.
**Brakuje w:** KoKAPI nie ma tej funkcji (jest tylko `logs/entry` dla jednego wpisu).

---

### logs/logs
**OKAPI:** `GET services/logs/logs`
**Auth:** Level 1
**Parametry:**
- `cache_code` (wymagany) — kod skrytki
- `fields` (opcjonalny, default: `uuid|date|user|type|comment`)
- `user_fields` (opcjonalny, default: `uuid|username|profile_url`)
- `offset` (opcjonalny, default: `0`)
- `limit` (opcjonalny, default: brak — wszystkie wpisy)
**Opis:** Pobiera listę wpisów dziennika dla konkretnej skrytki z paginacją. Zwraca listę uporządkowaną po dacie.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/userlogs
**OKAPI:** `GET services/logs/userlogs`
**Auth:** Level 1
**Parametry:**
- `user_uuid` (wymagany) — UUID użytkownika
- `fields` (opcjonalny, default: `uuid|date|cache_code|type|comment`)
- `limit` (opcjonalny, default: `20`, max: `1000`)
- `offset` (opcjonalny, default: `0`)
**Opis:** Pobiera wpisy dziennika danego użytkownika (najnowsze pierwsze). Do paginacji wszystkich wpisów użytkownika.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/capabilities
**OKAPI:** `GET services/logs/capabilities`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `cache_code` (opcjonalny) — kod skrytki do sprawdzenia możliwości logowania
- `log_uuid` (opcjonalny) — UUID wpisu do sprawdzenia możliwości edycji
- `logtype` (opcjonalny) — typ logu do sprawdzenia
**Opis:** Zwraca dostępne możliwości logowania: jakie typy logów można dodać, czy można rekomendować, czy wymagane hasło, czy można wystawić ocenę, itp. Jeden z `cache_code` lub `log_uuid` jest wymagany.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/images/add
**OKAPI:** `POST services/logs/images/add`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `log_uuid` (wymagany) — UUID wpisu dziennika (musi być autorem)
- `image` (wymagany) — plik zakodowany Base64 (JPEG, PNG lub GIF)
- `caption` (opcjonalny) — podpis obrazka (plain text)
- `is_spoiler` (opcjonalny, default: `false`) — czy obrazek jest spoilerem
- `position` (opcjonalny, OCDE) — pozycja obrazka na liście
- `langpref` (opcjonalny, default: `en`)
**Opis:** Dodaje nowy obrazek do wpisu dziennika. Zwraca `success`, `image_uuid`, `message`.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/images/delete
**OKAPI:** `POST services/logs/images/delete`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `image_uuid` (wymagany) — UUID obrazka (musi być autorem wpisu)
**Opis:** Usuwa obrazek z wpisu dziennika.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### logs/images/edit
**OKAPI:** `POST services/logs/images/edit`
**Auth:** Level 3 (OAuth token)
**Parametry:**
- `image_uuid` (wymagany) — UUID obrazka do edycji (musi być autorem wpisu)
- `caption` (opcjonalny) — nowy podpis (pusty string = usuń podpis)
- `is_spoiler` (opcjonalny) — `true`/`false`
- `position` (opcjonalny, OCDE) — nowa pozycja na liście
- `langpref` (opcjonalny, default: `en`)
**Opis:** Edytuje właściwości istniejącego obrazka w wpisie dziennika.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### users/by_internal_id
**OKAPI:** `GET services/users/by_internal_id`
**Auth:** Level 1
**Parametry:**
- `internal_id` (wymagany) — wewnętrzny numeryczny ID użytkownika
- `fields` (wymagany) — jakie pola zwrócić
**Opis:** Pobiera dane użytkownika po wewnętrznym ID (nie UUID). Głównie do kompatybilności ze starszymi systemami.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### users/by_internal_ids
**OKAPI:** `GET services/users/by_internal_ids`
**Auth:** Level 1
**Parametry:**
- `internal_ids` (wymagany) — pipe-separated lista wewnętrznych ID (max 500)
- `fields` (wymagany) — jakie pola zwrócić
**Opis:** Bulk odpowiednik `users/by_internal_id`.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### users/by_usernames
**OKAPI:** `GET services/users/by_usernames`
**Auth:** Level 1
**Parametry:**
- `usernames` (wymagany) — pipe-separated lista nazw użytkowników (max 500)
- `fields` (wymagany) — jakie pola zwrócić
**Opis:** Bulk odpowiednik `users/by_username`. Zwraca słownik `{username: user_data}`.
**Brakuje w:** KoKAPI nie ma tej funkcji (jest tylko `UsersClient.byUsername()` dla jednego użytkownika).

---

### users/users
**OKAPI:** `GET services/users/users`
**Auth:** Level 1
**Parametry:**
- `user_uuids` (wymagany) — pipe-separated lista UUID (max 500)
- `fields` (wymagany) — jakie pola zwrócić
**Opis:** Bulk odpowiednik `users/user`. Zwraca słownik `{uuid: user_data}`.
**Brakuje w:** KoKAPI nie ma tej funkcji (jest tylko `UsersClient.byUuid()` dla jednego UUID).

---

### attrs/attribute_index
**OKAPI:** `GET services/attrs/attribute_index`
**Auth:** Level 1
**Parametry:**
- `langpref` (opcjonalny, default: `en`)
- `fields` (opcjonalny, default: `name`) — pola do zwrócenia
- `only_locally_used` (opcjonalny, default: `false`) — zwróć tylko atrybuty używane lokalnie
**Opis:** Zwraca słownik wszystkich zdefiniowanych atrybutów OKAPI (A-codes → dane atrybutu). Przydatne do jednorazowego załadowania całego słownika atrybutów.
**Brakuje w:** KoKAPI nie ma tej funkcji (jest tylko `attribute()` dla jednego atrybutu i `attributes()` dla listy).

---

### replicate/fulldump
**OKAPI:** `GET services/replicate/fulldump`
**Auth:** Level 1
**Parametry:** brak (poza `consumer_key`)
**Opis:** Pobiera pełny zrzut bazy danych OKAPI jako skompresowane archiwum TGZ/TBZ2 z plikami JSON. Zawiera wszystkie skrytki z polami jak w `geocache`. Używane do inicjalizacji lokalnej kopii bazy danych.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### replicate/info
**OKAPI:** `GET services/replicate/info`
**Auth:** Level 1
**Parametry:** brak (poza `consumer_key`)
**Opis:** Zwraca informacje o aktualnym stanie changelog i fulldump: aktualny revision, czas ostatniego fulldump, etc.
**Brakuje w:** KoKAPI nie ma tej funkcji. `ReplicateClient.currentRevision()` jest obejściem (queryuje changelog z `since=0`).

---

### apisrv/installation
**OKAPI:** `GET services/apisrv/installation`
**Auth:** Level 0 (bez klucza)
**Parametry:** brak
**Opis:** Zwraca informacje o instalacji OKAPI: `site_url`, `okapi_base_url`, `site_name`, `okapi_revision`, `registration_url`, dane licencyjne, itp.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### apisrv/installations
**OKAPI:** `GET services/apisrv/installations`
**Auth:** Level 0 (bez klucza)
**Parametry:** brak
**Opis:** Zwraca listę wszystkich publicznych instalacji OKAPI (OC.PL, OC.DE, OC.US, itp.) z ich adresami bazowymi.
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### apisrv/stats
**OKAPI:** `GET services/apisrv/stats`
**Auth:** Level 0 (bez klucza)
**Parametry:** brak
**Opis:** Zwraca podstawowe statystyki instalacji: `cache_count` (przybliżona liczba skrytek).
**Brakuje w:** KoKAPI nie ma tej funkcji.

---

### apiref/issue
**OKAPI:** `GET services/apiref/issue`
**Auth:** Level 0
**Opis:** Zgłoś problem z OKAPI.
**Brakuje w:** KoKAPI nie ma tej funkcji (mało istotne).

---

### apiref/method
**OKAPI:** `GET services/apiref/method`
**Auth:** Level 0
**Parametry:** `method_path` (wymagany)
**Opis:** Zwraca dokumentację konkretnej metody OKAPI w formacie JSON.
**Brakuje w:** KoKAPI nie ma tej funkcji (mało istotne).

---

### apiref/method_index
**OKAPI:** `GET services/apiref/method_index`
**Auth:** Level 0
**Parametry:** brak
**Opis:** Zwraca listę wszystkich dostępnych metod OKAPI.
**Brakuje w:** KoKAPI nie ma tej funkcji (mało istotne).

---

## ISTNIEJĄCE ENDPOINTY Z BRAKUJĄCYMI PARAMETRAMI

### caches/geocache — brakujące parametry w `CachesClient.get()`

**OKAPI:** `GET services/caches/geocache`
**Brakuje w:** `CachesClient.get(code: String)`

Brakujące parametry:
- `langpref` (opcjonalny, default: `en`) — preferowany język dla name, description, hint
- `attribution_append` (opcjonalny, default: `full`) — `full`/`none`/`static` — jak dołączać notę atrybucyjną do opisu
- `oc_team_annotation` (opcjonalny, OCPL, default: `description`) — `description`/`separate` — jak zwracać adnotacje OC Team
- `owner_fields` (opcjonalny, default: `uuid|username|profile_url`) — jakie pola użytkownika dołączyć do `owner`
- `lpc` (opcjonalny, default: `10`) — liczba ostatnich wpisów dziennika w polu `latest_logs`
- `log_fields` (opcjonalny, default: `uuid|date|user|type|comment`) — jakie pola wpisów w `latest_logs`
- `log_user_fields` (opcjonalny, default: `uuid|username|profile_url`) — pola użytkownika wewnątrz wpisów
- `user_logs_only` (opcjonalny, default: `false`) — filtruj `latest_logs` tylko do wpisów zalogowanego użytkownika
- `my_location` (opcjonalny) — `lat|lon` dla obliczenia `distance` i `bearing`
- `user_uuid` (opcjonalny) — UUID użytkownika do pól `is_found`, `is_recommended`, itp.

Brakujące pola w `defaultFields` (nie są pobierane):
- `names` — wielojęzyczna mapa nazw
- `gc_code` — kod Geocaching.com
- `distance` — odległość (wymaga `my_location`)
- `bearing`, `bearing2`, `bearing3` — azymut (wymaga `my_location`)
- `is_found` — czy znaleziony przez użytkownika
- `is_not_found` — czy użytkownik zgłosił DNF
- `is_watched` — czy obserwowany (Level 3)
- `is_ignored` — czy ignorowany (Level 3)
- `willattends` — liczba "Will attend" (dla Event cache)
- `watchers` — liczba obserwujących
- `size2` — rozmiar skrytki (string: `nano`, `micro`, `small`, `regular`, `large`, `xlarge`, `other`, `none`)
- `oxsize` — rozmiar kompatybilny z Garmin/OpenCaching.com (float)
- `trip_time` — szacowany czas wycieczki w godzinach
- `trip_distance` — szacowana odległość wycieczki w km
- `rating` (OCPL) — ogólna ocena (float 1-5)
- `rating_votes` (OCPL) — liczba głosów oceny
- `my_rating` (OCPL, Level 3) — ocena wystawiona przez zalogowanego
- `recommendations` — liczba rekomendacji
- `is_recommended` — czy zalogowany rekomendował
- `req_passwd` — czy wymagane hasło do znalezienia
- `short_description` — krótki opis (plain text)
- `short_descriptions` — wielojęzyczna mapa krótkich opisów
- `descriptions` — wielojęzyczna mapa opisów HTML
- `hints2` — wielojęzyczna mapa wskazówek (plain text)
- `images` — lista obrazków skrytki (uuid, url, thumb_url, caption, is_spoiler)
- `attr_acodes` — lista A-codes atrybutów
- `attrnames` — lista nazw atrybutów
- `oc_team_annotation` (OCPL) — adnotacje OC Team
- `attribution_note` — nota atrybucyjna
- `latest_logs` — ostatnie wpisy dziennika
- `my_notes` — prywatne notatki zalogowanego (Level 3)
- `trackables_count` — liczba trackables
- `trackables` — lista trackables (code, name, url)
- `alt_wpts` — alternatywne punkty (parking, skrytka końcowa, itp.)
- `country2` — nazwa kraju
- `region` — nazwa regionu/województwa
- `protection_areas` — lista obszarów chronionych
- `last_modified` — data ostatniej modyfikacji (ISO 8601)
- `date_hidden` — data ukrycia/wydarzenia (ISO 8601)
- `internal_id` — wewnętrzne ID (do celów replikacji)
- `needs_maintenance` (OCDE) — czy skrytka wymaga konserwacji

---

### caches/search/nearest — brakujące parametry w `CachesClient.search()`

**OKAPI:** `GET services/caches/search/nearest`
**Brakuje w:** `CachesClient.search(lat, lon, radiusKm, limit, offset, type)`

Parametr `center` i `radius` są obsługiwane. Brakuje wszystkich filtrów dziedziczonych z `search/all`:
- `status` (opcjonalny, default: `Available`) — filtowanie po statusie
- `owner_uuid` — skrytki konkretnego właściciela
- `name` — filtr po nazwie z wildcard
- `terrain` (default: `1-5`) — zakres trudności terenu
- `difficulty` (default: `1-5`) — zakres trudności znalezienia
- `size2` — filtr po rozmiarze
- `rating` (OCPL) — filtr po ocenie
- `min_rcmds` — minimalna liczba rekomendacji
- `min_founds` / `max_founds` — zakres liczby znalezień
- `modified_since` — skrytki zmodyfikowane od daty
- `found_status` (wymaga Level 3) — `found_only`/`notfound_only`/`either`
- `found_by` / `not_found_by` — filtr po historii znalezień użytkownika
- `recommended_only` / `recommended_by` — filtr po rekomendacjach
- `watched_only` — tylko obserwowane (Level 3)
- `ignored_status` — filtr ignorowanych (Level 3)
- `exclude_my_own` — wyklucz własne (Level 3)
- `with_trackables_only` — tylko ze trackables
- `ftf_hunter` — tylko nieznalezione przez nikogo
- `powertrail_only` (OCPL) — tylko z geopath
- `set_and` — filtrowanie AND z zapisanym zbiorem
- `order_by` — sortowanie wyników
- `location_source` (default: `default-coords`) — źródło lokalizacji (np. `alt_wpt:user-coords`)

---

### caches/search/bbox — brakujące parametry w `CachesClient.bbox()`

**OKAPI:** `GET services/caches/search/bbox`
**Brakuje w:** `CachesClient.bbox(southLat, westLon, northLat, eastLon, limit)`

Brakuje wszystkich filtrów jak w `search/all` powyżej, plus:
- `offset` (opcjonalny, default: `0`) — paginacja
- `order_by` — sortowanie
- `location_source` (opcjonalny, default: `default-coords`)

---

### logs/submit — brakujące parametry w `LogsClient.submit()`

**OKAPI:** `POST services/logs/submit`
**Brakuje w:** `LogsClient.submit(cacheCode, type, comment, date, password)`

Brakujące parametry:
- `comment_format` (opcjonalny) — `html` lub `plaintext` — format komentarza (OKAPI przyjmuje HTML domyślnie)
- `on_duplicate` (opcjonalny, default: `silent_success`) — `silent_success`/`user_error`/`continue` — obsługa duplikatów
- `rating` (OCPL, opcjonalny) — ocena skrytki 1-5 (tylko dla "Found it"/"Attended")
- `recommend` (opcjonalny, default: `false`) — czy rekomendować skrytkę (tylko dla "Found it"/"Attended")
- `needs_maintenance2` (opcjonalny, default: `null`) — `null`/`true`/`false` — raport o stanie skrytki
- `langpref` (opcjonalny, default: `en`) — język komunikatów zwrotnych

---

### logs/delete — brakujące dane w `LogsClient.delete()`

**OKAPI:** `POST services/logs/delete`
**Brakuje w:** `LogsClient.delete(logUuid)` — implementacja poprawna, ale brak zwracanej wartości (`success`)

---

### users/by_username — brakujące parametry w `UsersClient.byUsername()`

**OKAPI:** `GET services/users/by_username`
**Brakuje w:** `UsersClient.byUsername(username)`

Brakujące pola w `defaultFields`:
- `rcmds_given` — liczba wystawionych rekomendacji
- `rcmds_left` — ile rekomendacji zostało (może być null)
- `rcmd_founds_needed` — ile znalezień do kolejnej rekomendacji
- `home_location` — lokalizacja domowa w `lat|lon` (Level 3, tylko własny profil)

---

### users/user — brakujące parametry w `UsersClient.me()`

**OKAPI:** `GET services/users/user`
**Brakuje w:** `UsersClient.me()`

Brakujące pola w `defaultFields` (te same jak wyżej):
- `rcmds_given`
- `rcmds_left`
- `rcmd_founds_needed`
- `home_location` — dostępne tylko przez Level 3 i tylko dla własnego profilu

---

### replicate/changelog — niepełna implementacja w `ReplicateClient`

**OKAPI:** `GET services/replicate/changelog`
**Brakuje w:** `ReplicateClient.changelog(sinceRevision)`

- Parametr `limit` (opcjonalny) — istnieje w OKAPI (używany w `currentRevision()`)
- Zwracane pole `more` — jest w `Changelog`, ale `currentRevision()` nie obsługuje sytuacji gdy `more=true` (nie pobiera kolejnych stron)
- Zwracane pole `changelog[].object_type` — obsługiwane tylko `geocache` (brak obsługi `log_entry`)
- Zwracane pole `changelog[].object_key` — w DTO jest `cacheCode`, ale dla `log_entry` powinno być `uuid`
- `Changelog.changeType` jest `String?` zamiast `enum` — brak rozróżnienia `replace`/`delete`

---

### attrs/attribute i attrs/attributes — brakujące pola

**OKAPI:** `GET services/attrs/attribute` i `GET services/attrs/attributes`
**Brakuje w:** `AttrsClient.attribute()` i `AttrsClient.attributes()`

W `AttributeFullDto` są pola `description`, `isLocallyUsed`, `localIconUrl` — ale `AttributeDto` (używany w `attributes()`) zawiera tylko `acode` i `name`.

Brakujące pola w `defaultFields` dla `attrs/attributes`:
- `names` — wielojęzyczna mapa nazw
- `description` — opis atrybutu
- `is_locally_used` — czy atrybut używany lokalnie
- `local_icon_url` — URL ikony atrybutu

W modelu `Attribute` brakuje:
- `iconUrl: String?` — URL ikony atrybutu (z pola `local_icon_url`)
- `description: String?` — opis atrybutu

---

## BRAKUJĄCE POLA W MODELACH

### model/Geocache.kt

Brakujące pola (z OKAPI `services/caches/geocache`):
- `gcCode: String?` — kod Geocaching.com (`gc_code`)
- `size2: String?` — rozmiar skrytki (`none`/`nano`/`micro`/`small`/`regular`/`large`/`xlarge`/`other`)
- `recommendations: Int` — liczba rekomendacji
- `reqPasswd: Boolean` — czy wymagane hasło
- `shortDescription: String?` — krótki opis (plain text)
- `images: List<CacheImage>` — lista obrazków skrytki
- `trackablesCount: Int` — liczba trackables
- `trackables: List<Trackable>` — lista trackables
- `altWpts: List<AltWaypoint>` — alternatywne punkty nawigacyjne
- `country: String?` — kraj
- `region: String?` — region/województwo
- `rating: Float?` (OCPL) — ocena ogólna
- `tripTime: Float?` — szacowany czas wycieczki (godziny)
- `tripDistance: Float?` — szacowana odległość wycieczki (km)
- `dateHidden: String?` — data ukrycia/wydarzenia
- `lastModified: String?` — data ostatniej modyfikacji
- `latestLogs: List<GeocacheLog>?` — ostatnie wpisy dziennika
- `myNotes: String?` — prywatne notatki (Level 3)
- `isFound: Boolean?` — czy znaleziony przez zalogowanego
- `isWatched: Boolean?` — czy obserwowany (Level 3)
- `needsMaintenance: Boolean?` (OCDE)
- `willattends: Int` — liczba "Will attend" (Event cache)

### model/GeocacheLog.kt

Brakujące pola (z OKAPI `services/logs/entry`):
- `images: List<LogImage>?` — obrazki dołączone do wpisu
- `wasRecommended: Boolean` — czy zawierał rekomendację
- `needsMaintenance2: Boolean?` — raport o stanie skrytki (`true`/`false`/`null`)
- `dateCreated: String?` — data utworzenia wpisu (ISO 8601)
- `lastModified: String?` — data ostatniej edycji
- `location: String?` — lokalizacja podana przez logującego (dla "Moved")
- `ocTeamEntry: Boolean` — czy wpis OC Team

### model/User.kt

Brakujące pola (z OKAPI `services/users/user`):
- `rcmdsGiven: Int` — liczba wystawionych rekomendacji
- `rcmdsLeft: Int?` — pozostałe rekomendacje (null = brak limitu)
- `rcmdFoundsNeeded: Int` — ile znalezień do kolejnej rekomendacji
- `homeLocation: LatLon?` — lokalizacja domowa (Level 3, tylko własny profil)

### model/Attribute.kt

Brakujące pola:
- `iconUrl: String?` — URL ikony atrybutu (`local_icon_url`)
- `description: String?` — opis atrybutu

### Brakujące modele

```kotlin
// Brakujący model CacheImage
data class CacheImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String,
    val caption: String?,
    val isSpoiler: Boolean,
)

// Brakujący model Trackable
data class Trackable(
    val code: String,
    val name: String,
    val url: String?,
)

// Brakujący model AltWaypoint
data class AltWaypoint(
    val name: String,
    val location: LatLon,
    val type: String,      // "parking", "final", "stage", "user-coords", itp.
    val typeName: String,
    val description: String?,
)

// Brakujący model LogImage
data class LogImage(
    val uuid: String,
    val url: String,
    val thumbUrl: String?,
    val caption: String?,
    val isSpoiler: Boolean,
)
```

---

## BRAKUJĄCE DTO / MAPOWANIA

### http/dto/GeocacheDto.kt

Brakujące pola w `GeocacheDto`:
- `gc_code: String?`
- `size2: String?`
- `recommendations: Int`
- `req_passwd: Boolean`
- `short_description: String?`
- `images: List<CacheImageDto>?`
- `trackables_count: Int`
- `trackables: List<TrackableDto>?`
- `alt_wpts: List<AltWaypointDto>?`
- `country2: String?`
- `region: String?`
- `rating: Float?`
- `trip_time: Float?`
- `trip_distance: Float?`
- `date_hidden: String?`
- `last_modified: String?`
- `latest_logs: List<GeocacheLogDto>?`
- `my_notes: String?`
- `is_found: Boolean?`
- `is_watched: Boolean?`
- `needs_maintenance: Boolean?`
- `willattends: Int`

### http/dto/LogDto.kt

Brakujące pola w `GeocacheLogDto`:
- `images: List<LogImageDto>?`
- `was_recommended: Boolean`
- `needs_maintenance2: Boolean?`
- `date_created: String?`
- `last_modified: String?`
- `location: String?`
- `oc_team_entry: Boolean`

### http/dto/UserDto.kt

Brakujące pola w `UserDto`:
- `rcmds_given: Int`
- `rcmds_left: Int?`
- `rcmd_founds_needed: Int`
- `home_location: String?`

### Brakujące DTO

```kotlin
// Brakujące DTO
@Serializable
data class CacheImageDto(
    val uuid: String,
    val url: String,
    @SerialName("thumb_url") val thumbUrl: String,
    val caption: String? = null,
    @SerialName("is_spoiler") val isSpoiler: Boolean = false,
)

@Serializable
data class TrackableDto(
    val code: String,
    val name: String,
    val url: String? = null,
)

@Serializable
data class AltWaypointDto(
    val name: String,
    val location: String,
    val type: String,
    @SerialName("type_name") val typeName: String = "",
    val description: String? = null,
)

@Serializable
data class LogImageDto(
    val uuid: String,
    val url: String,
    @SerialName("thumb_url") val thumbUrl: String? = null,
    val caption: String? = null,
    @SerialName("is_spoiler") val isSpoiler: Boolean = false,
)

@Serializable
data class SubmitLogResponseFullDto(
    val success: Boolean,
    val message: String,
    @SerialName("log_uuid") val logUuid: String? = null,
    @SerialName("log_uuids") val logUuids: List<String> = emptyList(),
)
```

---

## PODSUMOWANIE STATYSTYK

| Kategoria | Liczba |
|---|---|
| Brakujące endpointy (zupełnie) | 24 |
| Istniejące endpointy z brakującymi parametrami | 7 |
| Brakujące pola w modelu Geocache | 18 |
| Brakujące pola w modelu GeocacheLog | 7 |
| Brakujące pola w modelu User | 4 |
| Brakujące pola w modelu Attribute | 2 |
| Brakujące modele domeny | 4 |
| Brakujące DTO | 4 |

**Uwaga:** Endpointy `apiref/*` (issue, method, method_index) są pominięte jako mało istotne dla biblioteki klienckiej.
