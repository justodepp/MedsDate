package com.medsdate.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/** Set by NavGraph so list and detail can share the medicine image without threading scopes through. */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/** Shared-element transition of a medicine photo between Home card and Detail; no-op outside NavGraph (previews). */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.medicineImageTransition(medicineId: Int): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val visibility = LocalAnimatedVisibilityScope.current ?: return this
    return with(shared) {
        this@medicineImageTransition.sharedElement(
            sharedContentState = rememberSharedContentState(key = "medicine-image-$medicineId"),
            animatedVisibilityScope = visibility
        )
    }
}
