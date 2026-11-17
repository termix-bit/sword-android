package com.sword.security.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appLaunchesAndNavigatesToDashboardAndModules() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("Acknowledge & Enter Toolkit"))
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodes(androidx.compose.ui.test.hasText("Quick Checks"))
                    .fetchSemanticsNodes().isNotEmpty()
        }

        val onboardingButtons = composeRule.onAllNodes(
            androidx.compose.ui.test.hasText("Acknowledge & Enter Toolkit")
        ).fetchSemanticsNodes()

        if (onboardingButtons.isNotEmpty()) {
            composeRule.onNodeWithText("Acknowledge & Enter Toolkit").performClick()
        }

        composeRule.onNodeWithText("Quick Checks").assertIsDisplayed()
        composeRule.onNodeWithText("Tools").performClick()
        composeRule.onNodeWithText("Security Tools").assertIsDisplayed()
    }
}
