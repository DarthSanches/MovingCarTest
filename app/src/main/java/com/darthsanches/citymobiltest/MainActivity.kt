package com.darthsanches.citymobiltest

import android.animation.ObjectAnimator
import android.graphics.Path
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val CAR_SPEED = 250
        const val FULL_CIRCLE_DEGREES = 360
        const val MAX_TURN_ANGLE = 90
        const val ANGLE_OFFSET = -90
    }

    lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        view = findViewById(R.id.car)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (event != null && event.action == MotionEvent.ACTION_UP) {
            moveCar(event.x, event.y)
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private fun moveCar(toX: Float, toY : Float){
        //set view rotation between 0 and 360 degrees
        view.rotation = view.rotation % FULL_CIRCLE_DEGREES
        if (view.rotation < 0) view.rotation = view.rotation + FULL_CIRCLE_DEGREES
        val startRotation = view.rotation
        //calculate rotation to end point
        val endRotation = Math.toDegrees(atan2((view.x - toX),
                (view.y - toY)).toDouble()).toFloat() * -1
        //calculate positions for cubic's bezier lever points
        val leverLength = hypot(view.x - toX - view.width / 2,
                view.y - toY - view.height / 2) / 3
        val px1 = view.x + leverLength *
                cos(Math.toRadians(startRotation.toDouble() + ANGLE_OFFSET))
        val py1 = view.y + leverLength *
                sin(Math.toRadians(startRotation.toDouble() + ANGLE_OFFSET))

        val rotationDiff = calculateOptimalRotation(view.rotation, endRotation)
        val angToSecondLeverPoint = if (abs(rotationDiff) > MAX_TURN_ANGLE) {
            if (rotationDiff < 0) {
                startRotation - MAX_TURN_ANGLE
            } else {
                startRotation + MAX_TURN_ANGLE
            }
        } else {
            endRotation
        }
        val px2 = px1 + leverLength *
                cos(Math.toRadians(angToSecondLeverPoint.toDouble() + ANGLE_OFFSET))
        val py2 = py1 + leverLength *
                sin(Math.toRadians(angToSecondLeverPoint.toDouble() + ANGLE_OFFSET))
        val afterTurnRotation = Math.toDegrees(atan2(px2 - toX, py2 - toY)) * -1
        val clarifiedRotation = rotationDiff +
                calculateOptimalRotation(view.rotation + rotationDiff,
                        afterTurnRotation.toFloat())
        //calculate approximately for calculate optimal animate duration
        val approximatelyDistance = hypot(px1 - toX - view.width / 2,
                py1 - toY - view.height / 2)
        val animDuration = ((approximatelyDistance / CAR_SPEED) * 1000).toLong()

        val path = Path().apply {
            moveTo(view.x, view.y)
            cubicTo(px1.toFloat(), py1.toFloat(), px2.toFloat(), py2.toFloat(),
                    toX - view.width / 2, toY - view.height / 2)
        }
        
        view.animate().rotationBy(clarifiedRotation).duration = animDuration / 2
        ObjectAnimator.ofFloat(view, View.X, View.Y, path).apply {
            duration = animDuration
            start()
        }
    }

    private fun calculateOptimalRotation(rotationFrom: Float, rotationTo: Float): Float {
        val tmpRotation = if (rotationTo < 0) rotationTo + FULL_CIRCLE_DEGREES else rotationTo
        val rotationDiff = tmpRotation - rotationFrom
        return if (Math.abs(rotationDiff) > FULL_CIRCLE_DEGREES / 2) {
            if (rotationDiff < 0) {
                FULL_CIRCLE_DEGREES + rotationDiff
            }else {
                (FULL_CIRCLE_DEGREES - rotationDiff) * -1
            }
        } else {
            rotationDiff
        }
    }
}
