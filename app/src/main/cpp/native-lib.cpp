#include <jni.h>
#include <android/asset_manager_jni.h>

#include "Renderer.h"

namespace {
inline Renderer* asRenderer(jlong handle) {
    return reinterpret_cast<Renderer*>(handle);
}
}  // namespace

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeCreate(JNIEnv* env, jclass, jobject assetManager) {
    AAssetManager* assets = assetManager ? AAssetManager_fromJava(env, assetManager) : nullptr;
    return reinterpret_cast<jlong>(new Renderer(assets));
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeDestroy(JNIEnv*, jclass, jlong handle) {
    delete asRenderer(handle);
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeOnSurfaceCreated(JNIEnv*, jclass, jlong handle) {
    if (auto* r = asRenderer(handle)) r->onSurfaceCreated();
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeOnSurfaceChanged(
        JNIEnv*, jclass, jlong handle, jint width, jint height) {
    if (auto* r = asRenderer(handle)) r->onSurfaceChanged(width, height);
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeOnDrawFrame(JNIEnv*, jclass, jlong handle) {
    if (auto* r = asRenderer(handle)) r->onDrawFrame();
}

// --- Gameplay input, called every frame from the UI thread ---

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeSetMoveInput(
        JNIEnv*, jclass, jlong handle, jfloat dx, jfloat dz) {
    if (auto* r = asRenderer(handle)) r->game().setMoveInput(dx, dz);
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeTriggerAction(JNIEnv*, jclass, jlong handle) {
    if (auto* r = asRenderer(handle)) r->game().triggerAction();
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeTap(
        JNIEnv*, jclass, jlong handle, jfloat x, jfloat y) {
    if (auto* r = asRenderer(handle)) r->handleTap(x, y);
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeCameraDrag(
        JNIEnv*, jclass, jlong handle, jfloat dx, jfloat dy) {
    if (auto* r = asRenderer(handle)) r->handleCameraDrag(dx, dy);
}

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeCameraZoom(
        JNIEnv*, jclass, jlong handle, jfloat scaleFactor) {
    if (auto* r = asRenderer(handle)) r->handleCameraZoom(scaleFactor);
}

JNIEXPORT jboolean JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeIsReady(JNIEnv*, jclass, jlong handle) {
    if (auto* r = asRenderer(handle)) return r->isReady() ? JNI_TRUE : JNI_FALSE;
    return JNI_FALSE;
}

// --- Gameplay state, polled every frame to drive the Compose HUD ---

JNIEXPORT void JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeGetNeeds(
        JNIEnv* env, jclass, jlong handle, jfloatArray outNeeds) {
    auto* r = asRenderer(handle);
    if (!r || !outNeeds) return;
    float needs[3];
    r->game().getNeeds(needs);
    env->SetFloatArrayRegion(outNeeds, 0, 3, needs);
}

JNIEXPORT jint JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeGetNearbyZone(JNIEnv*, jclass, jlong handle) {
    auto* r = asRenderer(handle);
    if (!r) return 0;
    return static_cast<jint>(r->game().getNearbyZone());
}

JNIEXPORT jint JNICALL
Java_com_maisonvie_game_engine_NativeGameBridge_nativeGetState(JNIEnv*, jclass, jlong handle) {
    auto* r = asRenderer(handle);
    if (!r) return 0;
    return static_cast<jint>(r->game().getState());
}

}  // extern "C"
