package com.maisonvie.game.ui

import android.content.Context
import android.opengl.GLSurfaceView
import com.maisonvie.game.engine.NativeGameBridge
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GLSurfaceView whose renderer callbacks simply forward into the native C++
 * engine through JNI (see app/src/main/cpp/Renderer.cpp + Game.cpp). One
 * native Renderer/Game instance is created per view and destroyed when the
 * view is torn down.
 */
class GameSurfaceView(context: Context) : GLSurfaceView(context) {

    private var downX = 0f
    private var downY = 0f
    private var moved = false
    private var pinchStartDistance = 0f

    /** Exposed so Compose can push input and poll gameplay state via JNI. */
    var nativeHandle: Long = 0
        private set

    init {
        setEGLContextClientVersion(3)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)

        nativeHandle = if (NativeGameBridge.libraryLoaded) {
            NativeGameBridge.nativeCreate(context.assets)
        } else {
            0
        }

        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                if (nativeHandle != 0L) NativeGameBridge.nativeOnSurfaceCreated(nativeHandle)
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                if (nativeHandle != 0L) NativeGameBridge.nativeOnSurfaceChanged(nativeHandle, width, height)
            }

            override fun onDrawFrame(gl: GL10?) {
                if (nativeHandle != 0L) NativeGameBridge.nativeOnDrawFrame(nativeHandle)
            }
        })
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                moved = false
                pinchStartDistance = 0f
                return true
            }
            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount >= 2) {
                    pinchStartDistance = distance(event)
                    moved = true
                }
                return true
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                if (nativeHandle == 0L) return true
                if (event.pointerCount >= 2) {
                    val d = distance(event)
                    if (pinchStartDistance > 1f && d > 1f) {
                        NativeGameBridge.nativeCameraZoom(nativeHandle, d / pinchStartDistance)
                        pinchStartDistance = d
                    }
                    moved = true
                } else {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (dx * dx + dy * dy > 64f) moved = true
                    if (moved) {
                        NativeGameBridge.nativeCameraDrag(nativeHandle, event.x - downX, event.y - downY)
                        downX = event.x
                        downY = event.y
                    }
                }
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                if (!moved && nativeHandle != 0L) {
                    NativeGameBridge.nativeTap(nativeHandle, event.x, event.y)
                }
                pinchStartDistance = 0f
                performClick()
                return true
            }
            android.view.MotionEvent.ACTION_POINTER_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                pinchStartDistance = 0f
                return true
            }
        }
        return true
    }

    private fun distance(event: android.view.MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(0) - event.getX(1)
        val dy = event.getY(0) - event.getY(1)
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun destroyNative() {
        val handleToFree = nativeHandle
        if (handleToFree != 0L) {
            nativeHandle = 0
            queueEvent { NativeGameBridge.nativeDestroy(handleToFree) }
        }
    }
}
