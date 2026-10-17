package com.example.azkarapp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.navigation.NavBackStackEntry

typealias NavEnter = AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
typealias NavExit = AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

object NavTransitions {

    private const val SLIDE_MS = 340
    private const val TAB_SLIDE_MS = 280
    private const val FADE_IN_MS = 220
    private const val FADE_OUT_MS = 140


    val enter: NavEnter = {
        slideIntoContainer(
            towards = SlideDirection.Start,
            animationSpec = tween(SLIDE_MS, easing = FastOutSlowInEasing),
            initialOffset = { it / 6 }
        ) + fadeIn(tween(FADE_IN_MS, delayMillis = 40))
    }

    val exit: NavExit = {
        slideOutOfContainer(
            towards = SlideDirection.Start,
            animationSpec = tween(SLIDE_MS, easing = FastOutSlowInEasing),
            targetOffset = { it / 8 }
        ) + fadeOut(tween(FADE_OUT_MS))
    }

    // الرجوع: نفس الحركة بالعكس
    val popEnter: NavEnter = {
        slideIntoContainer(
            towards = SlideDirection.End,
            animationSpec = tween(SLIDE_MS, easing = FastOutSlowInEasing),
            initialOffset = { it / 8 }
        ) + fadeIn(tween(FADE_IN_MS, delayMillis = 40))
    }

    val popExit: NavExit = {
        slideOutOfContainer(
            towards = SlideDirection.End,
            animationSpec = tween(SLIDE_MS, easing = FastOutSlowInEasing),
            targetOffset = { it / 6 }
        ) + fadeOut(tween(FADE_OUT_MS))
    }


    val launchEnter: NavEnter = {
        fadeIn(tween(380)) + scaleIn(
            initialScale = 0.96f,
            animationSpec = tween(380, easing = LinearOutSlowInEasing)
        )
    }

    val launchExit: NavExit = { fadeOut(tween(220)) }


    private val tabRoutes = listOf(
        Screen.Home.route,
        Screen.PrayerTimes.route,
        Screen.Misbaha.route,
        Screen.Settings.route
    )

    private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabDirection(): SlideDirection {
        val from = tabRoutes.indexOf(initialState.destination.route)
        val to = tabRoutes.indexOf(targetState.destination.route)
        return if (to >= from) SlideDirection.Start else SlideDirection.End
    }

    val tabEnter: NavEnter = {
        slideIntoContainer(
            towards = tabDirection(),
            animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
            initialOffset = { it / 6 }
        ) + fadeIn(tween(FADE_IN_MS, delayMillis = 60))
    }

    val tabExit: NavExit = {
        slideOutOfContainer(
            towards = tabDirection(),
            animationSpec = tween(TAB_SLIDE_MS, easing = FastOutSlowInEasing),
            targetOffset = { it / 6 }
        ) + fadeOut(tween(120))
    }
}