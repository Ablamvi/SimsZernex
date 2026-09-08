package com.maisonvie.game.engine

/**
 * Thin JNI declarations for the native (C++ / OpenGL ES 3.0) game engine.
 * All simulation (movement, needs, collisions) and all rendering happen in
 * app/src/main/cpp - this object only forwards calls across the JNI boundary.
 */
internal object NativeGameBridge {

    val libraryLoaded: Boolean = try {
        System.loadLibrary("maisonviegame")
        true
    } catch (t: UnsatisfiedLinkError) {
        false
    } catch (t: Throwable) {
        false
    }

    external fun nativeCreate(assetManager: android.content.res.AssetManager): Long
    external fun nativeDestroy(handle: Long)
    external fun nativeOnSurfaceCreated(handle: Long)
    external fun nativeOnSurfaceChanged(handle: Long, width: Int, height: Int)
    external fun nativeOnDrawFrame(handle: Long)

    // Touch input: floor tap = move, furniture tap = move + interact.
    external fun nativeSetMoveInput(handle: Long, dx: Float, dz: Float)
    external fun nativeTriggerAction(handle: Long)
    external fun nativeTap(handle: Long, x: Float, y: Float)
    external fun nativeCameraDrag(handle: Long, dx: Float, dy: Float)
    external fun nativeCameraZoom(handle: Long, scaleFactor: Float)
    external fun nativeIsReady(handle: Long): Boolean

    // State, polled from the UI thread to drive the HUD.
    external fun nativeGetNeeds(handle: Long, outNeeds: FloatArray)
    external fun nativeGetNearbyZone(handle: Long): Int
    external fun nativeGetState(handle: Long): Int
}

/** Mirrors ZoneType in Game.h. */
enum class Zone(val code: Int) {
    NONE(0), BED(1), SHOWER(2), KITCHEN(3), SOFA(4),
    SINK(5), TOILET(6), DESK(7), FRIDGE(8);
    companion object {
        fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: NONE
    }
}

/** Mirrors CharState in Game.h. */
enum class CharacterState(val code: Int) {
    IDLE(0), WALKING(1), APPROACHING(2), BUSY(3);
    companion object {
        fun fromCode(code: Int) = entries.firstOrNull { it.code == code } ?: IDLE
    }
}
