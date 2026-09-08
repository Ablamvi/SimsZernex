#pragma once

#include <GLES3/gl3.h>
#include <android/asset_manager.h>
#include <vector>
#include <string>
#include <mutex>

#include "Game.h"
#include "Mat4.h"

// MaisonVie 3D renderer: furnished house scene + skinned character +
// touch-controlled orbit camera. Procedural meshes are retained only for
// interaction furniture that is not part of the house asset.
class Renderer {
public:
    explicit Renderer(AAssetManager* assets);
    ~Renderer();

    void onSurfaceCreated();
    void onSurfaceChanged(int width, int height);
    void onDrawFrame();
    bool isReady() const;
    void handleTap(float screenX, float screenY);
    void handleCameraDrag(float dxPixels, float dyPixels);
    void handleCameraZoom(float scaleFactor);
    Game& game() { return game_; }

private:
    enum class MeshKind { BOX = 0, SPHERE = 1, CYLINDER = 2 };
    struct Mesh {
        GLuint vao = 0;
        GLuint vbo = 0;
        int vertexCount = 0;
    };

    void buildProgram();
    void buildCharacterProgram();
    bool loadCharacterMesh();
    bool loadHouseMesh();
    void updateCharacterBones(float timeSeconds);
    void drawHouse();
    void drawCharacter(float x, float y, float z, float yaw);
    int findBone(const char* name) const;
    void buildMeshes();
    void buildMesh(Mesh& mesh, const std::vector<float>& vertices);
    void buildInstanceBuffer();
    GLuint compileShader(GLenum type, const char* src);

    void push(MeshKind kind, float x, float y, float z, float sx, float sy, float sz,
              float r, float g, float b, float a = 1.0f, float yawRad = 0.0f);
    void pushBox(float x, float y, float z, float sx, float sy, float sz,
                 float r, float g, float b, float a = 1.0f, float yawRad = 0.0f);
    void pushSphere(float x, float y, float z, float sx, float sy, float sz,
                    float r, float g, float b, float a = 1.0f, float yawRad = 0.0f);
    void pushCylinder(float x, float y, float z, float sx, float sy, float sz,
                      float r, float g, float b, float a = 1.0f, float yawRad = 0.0f);
    void buildSceneInstances();
    void drawMeshInstances(const Mesh& mesh, const std::vector<float>& instances);

    Game game_;
    GLuint program_ = 0;
    GLuint houseProgram_ = 0;
    GLint houseViewProjLoc_ = -1;
    GLint houseLightDirLoc_ = -1;
    GLuint characterProgram_ = 0;
    GLint characterViewProjLoc_ = -1;
    GLint characterLightDirLoc_ = -1;
    GLint characterPosLoc_ = -1;
    GLint characterScaleLoc_ = -1;
    GLint characterYawLoc_ = -1;
    GLint characterBoneTextureLoc_ = -1;
    GLint characterBoneCountLoc_ = -1;
    GLuint characterVao_ = 0;
    GLuint characterVbo_ = 0;
    GLuint characterEbo_ = 0;
    GLuint boneTexture_ = 0;
    GLuint houseVao_ = 0;
    GLuint houseVbo_ = 0;
    GLuint houseEbo_ = 0;
    int houseIndexCount_ = 0;
    int characterVertexCount_ = 0;
    int characterIndexCount_ = 0;
    int characterBoneCount_ = 0;
    std::vector<int> characterParents_;
    std::vector<Mat4> characterRestLocal_;
    std::vector<Mat4> characterInverseBind_;
    std::vector<std::string> characterBoneNames_;
    std::vector<float> characterBoneMatrices_;
    Mat4 characterVp_{};
    float cameraYawDeg_ = 45.0f;
    float cameraPitchDeg_ = 52.0f;
    float cameraRadius_ = 4.1f;
    mutable std::mutex cameraMutex_;
    AAssetManager* assets_ = nullptr;
    GLint uViewProjLoc_ = -1;
    GLint uLightDirLoc_ = -1;

    Mesh cube_;
    Mesh sphere_;
    Mesh cylinder_;
    GLuint instanceVbo_ = 0;

    int viewportWidth_ = 1;
    int viewportHeight_ = 1;
    long long lastFrameNanos_ = 0;
    float animTime_ = 0.0f;

    std::vector<float> boxInstances_;
    std::vector<float> sphereInstances_;
    std::vector<float> cylinderInstances_;
};
