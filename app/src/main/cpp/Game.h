#pragma once

#include <array>
#include <atomic>
#include <mutex>
#include <vector>
#include <utility>
#include "Mat4.h"

enum class ZoneType {
    NONE = 0, BED = 1, SHOWER = 2, KITCHEN = 3, SOFA = 4,
    SINK = 5, TOILET = 6, DESK = 7, FRIDGE = 8
};

enum class CharState { IDLE = 0, WALKING = 1, APPROACHING = 2, BUSY = 3 };

struct Furniture {
    ZoneType type;
    float x, z;
    float sizeX, sizeZ;
    float height;
    float r, g, b;
};

struct WallBox { float x, z, sizeX, sizeZ, height; };

class Game {
public:
    Game();
    void update(float dtSeconds);
    void setMoveInput(float dx, float dz);
    void tapAt(float worldX, float worldZ);
    void triggerAction();
    void getCharacterTransform(float* outX, float* outY, float* outZ, float* outYawDeg) const;
    void getNeeds(float* out3) const;
    ZoneType getNearbyZone() const;
    CharState getState() const;
    ZoneType getActionZone() const;
    float getActionProgress() const;
    const std::vector<Furniture>& furniture() const { return furniture_; }
    const std::vector<WallBox>& walls() const { return wallSegments_; }
    float houseHalfWidth() const { return houseHalfWidth_; }
    float houseHalfDepth() const { return houseHalfDepth_; }
private:
    void resolveCollisions();
    bool collidesAt(float x, float z) const;
    void collideBox(float x, float z, float sizeX, float sizeZ);
    void chooseAutonomousAction();
    ZoneType computeNearbyZone() const;
    bool getInteractionAnchor(ZoneType type, float* outX, float* outZ, float* outYaw) const;
    void getActionPose(ZoneType type, float* outX, float* outY, float* outZ, float* outYaw) const;
    void publishSnapshot();
    bool rebuildPathTo(float targetX, float targetZ);

    // Large playable house inspired by the supplied isometric reference.
    float houseHalfWidth_ = 4.55f;
    float houseHalfDepth_ = 4.55f;
    std::vector<Furniture> furniture_;
    std::vector<WallBox> wallSegments_;

    float charX_ = -1.0f;
    float charZ_ = 0.8f;
    float charYawDeg_ = 180.0f;
    CharState state_ = CharState::IDLE;
    ZoneType busyKind_ = ZoneType::NONE;
    float busyTimer_ = 0.0f;
    float actionElapsed_ = 0.0f;
    float actionTargetX_ = 0.0f, actionTargetZ_ = 0.0f, actionTargetYaw_ = 0.0f;
    float actionPoseX_ = 0.0f, actionPoseY_ = 0.0f, actionPoseZ_ = 0.0f, actionPoseYaw_ = 0.0f;
    std::array<float, 3> needs_{78.0f, 76.0f, 74.0f};

    static constexpr float kActionDurationSeconds = 4.6f;
    static constexpr float kApproachSpeed = 3.8f;
    static constexpr float kHungerDecayPerSec = 0.11f;
    static constexpr float kEnergyDecayPerSec = 0.08f;
    static constexpr float kHygieneDecayPerSec = 0.07f;
    static constexpr float kMoveSpeed = 3.1f;
    static constexpr float kInteractionRadius = 1.55f;

    mutable std::mutex inputMutex_;
    float inputDx_ = 0.0f, inputDz_ = 0.0f;
    std::atomic<bool> actionRequested_{false};
    bool tapRequested_ = false;
    float pendingTapX_ = 0.0f, pendingTapZ_ = 0.0f;
    bool tapTargetActive_ = false;
    float tapTargetX_ = 0.0f, tapTargetZ_ = 0.0f;
    float autonomousCooldown_ = 0.0f;

    // Small grid navigator: enough for the 9x9 house while remaining cheap
    // on mobile. Paths are rebuilt only after a tap/autonomous destination.
    static constexpr int kNavCells = 35;
    static constexpr float kNavMin = -4.25f;
    static constexpr float kNavStep = 0.25f;
    std::vector<std::pair<float,float>> path_;
    size_t pathIndex_ = 0;

    mutable std::mutex stateMutex_;
    float snapCharX_ = -1.0f, snapCharY_ = 0.0f, snapCharZ_ = 0.8f, snapYaw_ = 180.0f;
    std::array<float, 3> snapNeeds_{78.0f, 76.0f, 74.0f};
    ZoneType snapZone_ = ZoneType::NONE;
    ZoneType snapActionZone_ = ZoneType::NONE;
    CharState snapState_ = CharState::IDLE;
    float snapActionProgress_ = 0.0f;
};
