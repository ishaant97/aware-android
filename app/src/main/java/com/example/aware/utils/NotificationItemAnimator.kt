package com.example.aware.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class NotificationItemAnimator : DefaultItemAnimator() {

    init {
        // Set shorter animation durations to reduce problems
        addDuration = 250L
        removeDuration = 200L
        moveDuration = 250L
        changeDuration = 250L

        // Critical: Enable move animations to allow items to shift when one is removed
        supportsChangeAnimations = false
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        // Reset any existing animations/transforms
        holder.itemView.clearAnimation()
        holder.itemView.alpha = 0f
        holder.itemView.translationY = holder.itemView.height * 0.25f

        // Simple fade and slide animation
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(addDuration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationEnd(animation: Animator) {
                    dispatchAddFinished(holder)
                    dispatchFinishedWhenDone()
                }

                override fun onAnimationCancel(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                }
            })
            .start()

        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        // Reset any existing animations
        holder.itemView.clearAnimation()

        // Slide out animation
        holder.itemView.animate()
            .alpha(0f)
            .translationX(-holder.itemView.width.toFloat())
            .setDuration(removeDuration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationEnd(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                    dispatchRemoveFinished(holder)
                    dispatchFinishedWhenDone()
                }

                override fun onAnimationCancel(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                }
            })
            .start()

        return true
    }

    // This is the key method to ensure items move up to fill empty space
    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        holder.itemView.clearAnimation()

        val deltaY = toY - fromY
        val deltaX = toX - fromX

        if (deltaY == 0 && deltaX == 0) {
            dispatchMoveFinished(holder)
            return false
        }

        if (deltaY != 0) {
            holder.itemView.translationY = fromY - toY.toFloat()
        }
        if (deltaX != 0) {
            holder.itemView.translationX = fromX - toX.toFloat()
        }

        // Create move animation
        holder.itemView.animate()
            .translationY(0f)
            .translationX(0f)
            .setDuration(moveDuration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    dispatchMoveStarting(holder)
                }

                override fun onAnimationEnd(animation: Animator) {
                    dispatchMoveFinished(holder)
                    dispatchFinishedWhenDone()
                }

                override fun onAnimationCancel(animation: Animator) {
                    clearAnimatedValues(holder.itemView)
                }
            })
            .start()

        return true
    }

    private fun clearAnimatedValues(view: View) {
        view.alpha = 1f
        view.translationX = 0f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.rotation = 0f
    }

    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimation(holder: RecyclerView.ViewHolder) {
        holder.itemView.animate().cancel()
        clearAnimatedValues(holder.itemView)
        super.endAnimation(holder)
    }

    override fun endAnimations() {

        super.endAnimations()
    }
}
