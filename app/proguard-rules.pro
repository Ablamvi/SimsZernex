# Keep native JNI entry points (methods called from C++ via JNIEnv->CallXxx or
# looked up by name) so R8 does not strip or rename them.
-keepclasseswithmembers class com.maisonvie.game.engine.NativeGameBridge {
    native <methods>;
}
