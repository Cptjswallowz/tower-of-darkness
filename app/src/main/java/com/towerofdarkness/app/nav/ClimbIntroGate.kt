package com.towerofdarkness.app.nav

/**
 * v0.1.36-climbintro — when Title→Climb (or New climb wipe→fresh) should show the intro clip.
 * Resume / continueClimb / live mid-run restore never request intro.
 * Missing asset → silent skip (caller routes straight to beginClimbFresh).
 */
object ClimbIntroGate {
    const val TAG = "v0.1.36-climbintro"
    const val ASSET_NAME = "climb_intro.mp4"
    /** Art CONFIRM md5 for staged raw clip (Engineer slots as-is). */
    const val EXPECTED_MD5 = "ead7b72c2a37c09b82f65ff015bfc29d"

    /**
     * @param freshClimb true for Title Climb / confirmNewClimb only; false for Resume / continue.
     * @param assetPresent true when [ASSET_NAME] opens from assets (no crash path).
     */
    fun shouldShowIntro(freshClimb: Boolean, assetPresent: Boolean): Boolean =
        freshClimb && assetPresent
}
