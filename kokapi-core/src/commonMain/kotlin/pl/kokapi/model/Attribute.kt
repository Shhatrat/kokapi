package pl.kokapi.model

/**
 * A cache attribute tag (e.g. "dog-friendly", "no-swimming-required").
 *
 * @property acode OKAPI attribute code, e.g. "A1".
 * @property isYes `true` for positive attributes (allowed/present), `false` for negative (not allowed/absent).
 * @property iconUrl URL to the attribute's icon image, if available.
 * @property description Human-readable description of what this attribute means.
 */
data class Attribute(
    val acode: String,
    val name: String,
    val isYes: Boolean,
    val iconUrl: String? = null,
    val description: String? = null,
)
