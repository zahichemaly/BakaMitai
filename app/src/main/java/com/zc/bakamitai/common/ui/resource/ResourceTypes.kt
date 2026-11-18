package com.zc.bakamitai.common.ui.resource

// The only way code names a resource: never `import com.zc.bakamitai.R` (Detekt ForbiddenImport) and never
// `R.string.*` / `R.plurals.*`. Text resources are always wrapped in `UiText` (conventions skill §2.5).
//
// `PluralResource` (`typealias PluralResource = com.zc.bakamitai.R.plurals`) is added together with the first
// <plurals> resource: AGP doesn't generate `R.plurals` while there are none, so the alias wouldn't compile.

/** String resources, e.g. `UiText.StringResource(StringResource.app_name)`. */
typealias StringResource = com.zc.bakamitai.R.string

/** Bundled fonts (res/font), used by `common/ui/theme/Type.kt`. */
typealias FontResource = com.zc.bakamitai.R.font
