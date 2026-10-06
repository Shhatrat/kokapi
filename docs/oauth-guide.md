# OAuth Guide

KoKAPI uses OAuth 1.0a — the same protocol required by the OKAPI spec.

## Auth Levels

| Level | Requirement | What you can access |
|---|---|---|
| 0 | Nothing | Fully public data |
| 1 | `consumer_key` | Public data with app identification |
| 2 | OAuth consumer signature | Signed requests |
| 3 | OAuth consumer + user token | Private user data, log submission |

Levels 0 and 1 work without login. KoKAPI automatically includes `consumer_key` on every request.

## Full OAuth Flow

### Step 1: Start login

```kotlin
val step = kokapi.auth.startLogin()
// step.authorizeUrl — open this URL in a browser or WebView
```

### Step 2: User authorizes

The user logs in at `step.authorizeUrl` and approves access. The browser
redirects to your `callbackUrl` with query parameters:
```
yourapp://oauth?oauth_token=REQTOKEN&oauth_verifier=VERIFIER
```

### Step 3: Complete login

```kotlin
val callbackUrl = "yourapp://oauth?oauth_token=xxx&oauth_verifier=yyy"
val success = kokapi.auth.completeLogin(callbackUrl)
// Tokens are stored in the TokenStorage you configured
```

### Step 4: All subsequent requests are authenticated

```kotlin
val me = kokapi.users.me()         // Level 3 ✓
val uuid = kokapi.logs.submit(...)  // Level 3 ✓
```

### Logout

```kotlin
kokapi.auth.logout()
// Clears stored tokens; next request reverts to Level 1
```

## Token Persistence

Tokens are stored in the `TokenStorage` you provide:

- **`EncryptedPrefsTokenStorage(context)`** — Android. Uses `EncryptedSharedPreferences` with AES-256-GCM.
- **`KeychainTokenStorage()`** — iOS. Uses the system Keychain.
- **`FileTokenStorage(dir)`** — JVM/Desktop. Stores in `~/.kokapi/tokens.properties`.
- **`InMemoryTokenStorage()`** — All platforms. For testing; tokens are lost when the process ends.

OKAPI OAuth 1.0a tokens do not expire, so tokens persist across app restarts.

## Out-of-Band (OOB) Flow

If your app doesn't have a custom URL scheme, use `callbackUrl = "oob"`:

```kotlin
val kokapi = KokapiClient(
    site = KokapiSite.OPENCACHING_PL,
    consumerKey = "key",
    callbackUrl = "oob",
    ...
)

val step = kokapi.auth.startLogin()
// Show step.authorizeUrl — user sees a PIN code on the page
print("Enter PIN: ")
val pin = readLine()!!
val success = kokapi.auth.completeLogin("oob?oauth_verifier=$pin")
```
