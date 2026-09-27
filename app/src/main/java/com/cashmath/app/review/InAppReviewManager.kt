package com.cashmath.app.review

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewManager {
    fun launchReviewFlow(activity: Activity) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(activity, reviewInfo)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
