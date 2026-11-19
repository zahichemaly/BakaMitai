package com.zc.bakamitai.common.ui.resource

// The only way code names a resource: never `import com.zc.bakamitai.R` (Detekt ForbiddenImport) and never
// `R.string.*` / `R.plurals.*`. Text resources are always wrapped in `UiText` (conventions skill §2.5).

/** String resources, e.g. `UiText.StringResource(StringResource.app_name)`. */
typealias StringResource = com.zc.bakamitai.R.string

/** Plurals resources, e.g. `UiText.StringPlural(PluralResource.home_section_upcoming_count, count, listOf(count))`. */
typealias PluralResource = com.zc.bakamitai.R.plurals

/** Bundled fonts (res/font), used by `common/ui/theme/Type.kt`. */
typealias FontResource = com.zc.bakamitai.R.font

/** Drawables, e.g. a notification's small icon: `DrawableResource.ic_notification`. */
typealias DrawableResource = com.zc.bakamitai.R.drawable
