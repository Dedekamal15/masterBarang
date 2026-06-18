package com.assettrack.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary                  = Color(0xFF051125)
val OnPrimary                = Color(0xFFFFFFFF)
val PrimaryContainer         = Color(0xFF1B263B)
val OnPrimaryContainer       = Color(0xFF828DA7)

val Secondary                = Color(0xFF47607E)
val OnSecondary              = Color(0xFFFFFFFF)
val SecondaryContainer       = Color(0xFFC2DCFF)
val OnSecondaryContainer     = Color(0xFF48617E)

val Tertiary                 = Color(0xFF001225)
val TertiaryContainer        = Color(0xFF0F273F)

val Background               = Color(0xFFFBF8FB)
val OnBackground             = Color(0xFF1B1B1D)
val Surface                  = Color(0xFFFBF8FB)
val OnSurface                = Color(0xFF1B1B1D)
val SurfaceVariant           = Color(0xFFE4E2E4)
val OnSurfaceVariant         = Color(0xFF45474D)

val SurfaceContainerLowest   = Color(0xFFFFFFFF)
val SurfaceContainerLow      = Color(0xFFF5F3F5)
val SurfaceContainer         = Color(0xFFF0EDEF)
val SurfaceContainerHigh     = Color(0xFFEAE7E9)
val SurfaceContainerHighest  = Color(0xFFE4E2E4)

val Outline                  = Color(0xFF75777D)
val OutlineVariant           = Color(0xFFC5C6CD)

val Error                    = Color(0xFFBA1A1A)
val OnError                  = Color(0xFFFFFFFF)
val ErrorContainer           = Color(0xFFFFDAD6)
val OnErrorContainer         = Color(0xFF93000A)

// Semantic status colors
val StatusActiveBackground      = Color(0xFFE6F4EA)
val StatusActiveForeground      = Color(0xFF137333)
val StatusMaintenanceBackground = Color(0xFFFCE8E6)
val StatusMaintenanceForeground = Color(0xFFC5221F)
val StatusBorrowedBackground    = Color(0xFFFFF3E0)
val StatusBorrowedForeground    = Color(0xFFE65100)

private val AssetTrackColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = OnPrimary,
    primaryContainer     = PrimaryContainer,
    onPrimaryContainer   = OnPrimaryContainer,
    secondary            = Secondary,
    onSecondary          = OnSecondary,
    secondaryContainer   = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary             = Tertiary,
    tertiaryContainer    = TertiaryContainer,
    background           = Background,
    onBackground         = OnBackground,
    surface              = Surface,
    onSurface            = OnSurface,
    surfaceVariant       = SurfaceVariant,
    onSurfaceVariant     = OnSurfaceVariant,
    outline              = Outline,
    outlineVariant       = OutlineVariant,
    error                = Error,
    onError              = OnError,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer
)

@Composable
fun AssetTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AssetTrackColorScheme,
        content = content
    )
}
