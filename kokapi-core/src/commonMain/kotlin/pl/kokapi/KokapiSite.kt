package pl.kokapi

/**
 * Supported Opencaching OKAPI installations.
 *
 * Each entry holds the base URL for the site's OKAPI services endpoint.
 *
 * @property baseUrl Root URL for all OKAPI service calls on this site.
 */
enum class KokapiSite(val baseUrl: String) {
    /** Opencaching Poland — opencaching.pl */
    OPENCACHING_PL("https://opencaching.pl/okapi/services"),

    /** Opencaching Germany — opencaching.de */
    OPENCACHING_DE("https://www.opencaching.de/okapi/services"),

    /** Opencaching USA — opencaching.us */
    OPENCACHING_US("https://www.opencaching.us/okapi/services"),

    /** Opencaching Netherlands — opencaching.nl */
    OPENCACHING_NL("https://www.opencaching.nl/okapi/services"),

    /** Opencaching Romania — opencaching.ro */
    OPENCACHING_RO("https://www.opencaching.ro/okapi/services"),

    /** Opencache UK — opencache.uk */
    OPENCACHE_UK("https://opencache.uk/okapi/services"),
}
