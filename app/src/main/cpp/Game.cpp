#include "Game.h"
#include <algorithm>
#include <cmath>
#include <array>

namespace {
float clamp(float v, float lo, float hi) { return std::max(lo, std::min(v, hi)); }
}

Game::Game() {
    // Coordinates are aligned to the supplied furnished house scene.
    // X/Z are floor coordinates, Y is height. Interaction anchors are chosen
    // from the actual furniture footprints so the character lands on seats,
    // the mattress, the kitchen work area and the shower.
    furniture_.push_back({ZoneType::SOFA, -2.0f, 0.45f, 2.8f, 1.38f, .90f, .35f,.38f,.43f});
    furniture_.push_back({ZoneType::BED, 2.2f, -1.90f, 3.0f, 3.85f, .80f, .48f,.30f,.18f});
    furniture_.push_back({ZoneType::KITCHEN, -2.0f, -3.70f, 3.0f, .80f, 1.08f, .42f,.42f,.45f});
    furniture_.push_back({ZoneType::SHOWER, 3.80f, 3.10f, 1.60f, 1.60f, 2.25f, .35f,.55f,.62f});
    furniture_.push_back({ZoneType::SINK, 3.40f, 1.00f, 1.45f, .65f, .90f, .82f,.82f,.82f});
    furniture_.push_back({ZoneType::TOILET, 3.15f, 1.95f, .85f, 1.05f, .85f, .90f,.91f,.89f});
    furniture_.push_back({ZoneType::FRIDGE, -3.45f, -3.25f, .82f, .83f, 2.15f, .84f,.86f,.87f});
    furniture_.push_back({ZoneType::DESK, -0.95f, 3.05f, 2.0f, .90f, .78f, .50f,.34f,.18f});
    furniture_.push_back({ZoneType::NONE, -1.45f, 0.0f, .65f, .65f, .80f, .55f,.38f,.18f});
    furniture_.push_back({ZoneType::NONE, 1.45f, 0.0f, .65f, .65f, .80f, .55f,.38f,.18f});
    furniture_.push_back({ZoneType::NONE, 0.0f, -0.95f, .65f, .65f, .80f, .55f,.38f,.18f});
    furniture_.push_back({ZoneType::NONE, 0.0f, 0.95f, .65f, .65f, .80f, .55f,.38f,.18f});

    // Start near the center so the character is visible immediately on the first camera frame.
    charX_ = -1.0f;
    charZ_ = 0.8f;
    charYawDeg_ = 180.0f;
    snapCharX_ = charX_;
    snapCharZ_ = charZ_;
    snapYaw_ = charYawDeg_;

    // Collision layout mirrors the visible interior wall boxes in
    // models/house.scene.bin. Each partition has a real doorway gap.
    const float h=2.55f;
    // Kitchen / living divider: z=-2.55, doorway around x=-0.55.
    wallSegments_.push_back({-2.70f,-2.55f,3.50f,.18f,h});
    wallSegments_.push_back({0.00f,-2.55f,.20f,.18f,h});
    // Office / living divider: z=1.85, doorway around x=-0.55.
    wallSegments_.push_back({-2.70f,1.85f,3.50f,.18f,h});
    wallSegments_.push_back({0.00f,1.85f,.20f,.18f,h});
    // Bedroom / living divider: x=.30, doorway between z=-.55 and .75.
    wallSegments_.push_back({.30f,-1.85f,.18f,5.20f,h});
    // Bathroom / bedroom divider: z=.75, doorway on its west side.
    wallSegments_.push_back({3.35f,.75f,2.20f,.18f,h});
    // Bathroom west wall: x=2.20, doorway between z=1.15 and 2.70.
    wallSegments_.push_back({2.20f,1.15f,.18f,.55f,h});
    wallSegments_.push_back({2.20f,3.35f,.18f,2.30f,h});
}


void Game::setMoveInput(float dx,float dz){ std::lock_guard<std::mutex> l(inputMutex_); inputDx_=dx; inputDz_=dz; tapTargetActive_=false; }
void Game::tapAt(float worldX, float worldZ) {
    std::lock_guard<std::mutex> l(inputMutex_);
    pendingTapX_ = worldX;
    pendingTapZ_ = worldZ;
    tapRequested_ = true;
}

void Game::triggerAction(){ actionRequested_.store(true); }

void Game::update(float dt) {
    const bool wantsAction=actionRequested_.exchange(false);
    float tapX=0.0f, tapZ=0.0f;
    bool hasTap=false;
    {
        std::lock_guard<std::mutex> l(inputMutex_);
        if (tapRequested_) {
            tapX=pendingTapX_; tapZ=pendingTapZ_; tapRequested_=false; hasTap=true;
        }
    }

    if(state_==CharState::APPROACHING){
        float targetX=actionTargetX_, targetZ=actionTargetZ_;
        if(pathIndex_ < path_.size()) {
            targetX=path_[pathIndex_].first; targetZ=path_[pathIndex_].second;
        }
        const float dx=targetX-charX_, dz=targetZ-charZ_;
        const float d=std::sqrt(dx*dx+dz*dz);
        if(d<0.10f){
            if(pathIndex_ < path_.size()) ++pathIndex_;
            if(pathIndex_ < path_.size()) {
                // Continue along the computed doorway-safe path.
            } else {
                path_.clear(); pathIndex_=0;
                // For seated/reclined actions, keep the character at the exit anchor
                // and let BUSY interpolate onto the furniture. Standing actions can
                // safely use their final interaction point.
                if(busyKind_!=ZoneType::SOFA && busyKind_!=ZoneType::BED && busyKind_!=ZoneType::SHOWER){
                    charX_=actionPoseX_; charZ_=actionPoseZ_;
                }
                actionElapsed_=0.0f; busyTimer_=kActionDurationSeconds;
                state_=CharState::BUSY;
            }
        } else {
            const float brake=clamp(d/1.15f,0.22f,1.0f);
            const float step=std::min(d,kApproachSpeed*brake*dt);
            const float ux=dx/d, uz=dz/d;
            const float nx=charX_+ux*step, nz=charZ_+uz*step;
            if(!collidesAt(nx,nz)){ charX_=nx; charZ_=nz; }
            else {
                const float sx=charX_+ux*step;
                const float sz=charZ_+uz*step;
                if(!collidesAt(sx,charZ_)) charX_=sx;
                if(!collidesAt(charX_,sz)) charZ_=sz;
            }
            const float desiredYaw=std::atan2(ux,uz)*180.f/(float)M_PI;
            float delta=desiredYaw-charYawDeg_;
            while(delta>180.f) delta-=360.f; while(delta<-180.f) delta+=360.f;
            const float turnRate=260.f*dt;
            charYawDeg_ += clamp(delta,-turnRate,turnRate);
            resolveCollisions();
        }
    } else if(state_==CharState::BUSY){
        busyTimer_-=dt;
        actionElapsed_+=dt;
        const float active=clamp(actionElapsed_/kActionDurationSeconds,0.f,1.f);
        // Sofa: move from the standing anchor onto the seat during sit-down,
        // hold the seated position, then return to the same exit point before
        // ending the interaction. This avoids a visible teleport.
        if(busyKind_==ZoneType::SOFA || busyKind_==ZoneType::BED || busyKind_==ZoneType::SHOWER){
            float q=0.f;
            if(active<0.28f){
                q=active/0.28f; q=q*q*(3.f-2.f*q);
            } else if(active<0.82f){
                q=1.f;
            } else {
                q=(active-0.82f)/0.18f; q=1.f-(q*q*(3.f-2.f*q));
            }
            charX_=actionTargetX_+(actionPoseX_-actionTargetX_)*q;
            charZ_=actionTargetZ_+(actionPoseZ_-actionTargetZ_)*q;
        }
        {
            float delta=actionPoseYaw_-charYawDeg_;
            while(delta>180.f) delta-=360.f; while(delta<-180.f) delta+=360.f;
            const float turnRate=320.f*dt;
            charYawDeg_ += clamp(delta,-turnRate,turnRate);
        }
        if(busyKind_==ZoneType::BED) needs_[1]=clamp(needs_[1]+35.f*dt,0.f,100.f);
        else if(busyKind_==ZoneType::SHOWER || busyKind_==ZoneType::SINK) needs_[2]=clamp(needs_[2]+40.f*dt,0.f,100.f);
        else if(busyKind_==ZoneType::KITCHEN || busyKind_==ZoneType::FRIDGE) needs_[0]=clamp(needs_[0]+42.f*dt,0.f,100.f);
        else if(busyKind_==ZoneType::SOFA || busyKind_==ZoneType::DESK) needs_[1]=clamp(needs_[1]+15.f*dt,0.f,100.f);
        (void)active;
        if(busyTimer_<=0.f){state_=CharState::IDLE; busyKind_=ZoneType::NONE;}
    } else {
        needs_[0]=clamp(needs_[0]-kHungerDecayPerSec*dt,0.f,100.f);
        needs_[1]=clamp(needs_[1]-kEnergyDecayPerSec*dt,0.f,100.f);
        needs_[2]=clamp(needs_[2]-kHygieneDecayPerSec*dt,0.f,100.f);

        if (hasTap) {
            autonomousCooldown_=30.0f;
            ZoneType selected=ZoneType::NONE;
            float best=9999.0f;
            for (const auto& f : furniture_) {
                if (f.type==ZoneType::NONE) continue;
                const float dx=tapX-f.x, dz=tapZ-f.z;
                const float rx=f.sizeX*.5f+.55f, rz=f.sizeZ*.5f+.55f;
                if (std::fabs(dx)<=rx && std::fabs(dz)<=rz) {
                    const float d2=dx*dx+dz*dz;
                    if (d2<best) { best=d2; selected=f.type; }
                }
            }
            if (selected!=ZoneType::NONE) {
                float tx,tz,ty;
                if (getInteractionAnchor(selected,&tx,&tz,&ty)) {
                    actionTargetX_=tx; actionTargetZ_=tz; actionTargetYaw_=ty;
                    getActionPose(selected,&actionPoseX_,&actionPoseY_,&actionPoseZ_,&actionPoseYaw_);
                    busyKind_=selected; actionElapsed_=0.0f;
                    const bool reachable=rebuildPathTo(actionTargetX_,actionTargetZ_);
                    state_=reachable ? CharState::APPROACHING : CharState::IDLE;
                    tapTargetActive_=false;
                    std::lock_guard<std::mutex> l(inputMutex_); inputDx_=inputDz_=0.0f;
                }
            } else {
                tapTargetX_=clamp(tapX,-houseHalfWidth_+.42f,houseHalfWidth_-.42f);
                tapTargetZ_=clamp(tapZ,-houseHalfDepth_+.42f,houseHalfDepth_-.42f);
                tapTargetActive_=rebuildPathTo(tapTargetX_,tapTargetZ_);
            }
        }

        float dx=0.0f,dz=0.0f; bool hasTarget=false; float targetX=0.0f,targetZ=0.0f;
        {
            std::lock_guard<std::mutex> l(inputMutex_);
            dx=inputDx_; dz=inputDz_; hasTarget=tapTargetActive_; targetX=tapTargetX_; targetZ=tapTargetZ_;
        }
        if (hasTarget) {
            if(pathIndex_ >= path_.size()) {
                std::lock_guard<std::mutex> l(inputMutex_);
                tapTargetActive_ = false;
                path_.clear(); pathIndex_=0;
                dx = dz = 0.0f;
            } else {
                targetX=path_[pathIndex_].first; targetZ=path_[pathIndex_].second;
                dx = targetX - charX_; dz = targetZ - charZ_;
                const float d = std::sqrt(dx*dx + dz*dz);
                if (d < 0.10f) {
                    ++pathIndex_;
                    if(pathIndex_ >= path_.size()) {
                        std::lock_guard<std::mutex> l(inputMutex_);
                        tapTargetActive_ = false;
                        path_.clear(); pathIndex_=0;
                        dx = dz = 0.0f;
                    }
                }
            }
        }
        const float len=std::sqrt(dx*dx+dz*dz);
        if(len>.05f){
            dx/=len; dz/=len;
            const float step=kMoveSpeed*dt;
            const float nx=charX_+dx*step, nz=charZ_+dz*step;
            if(!collidesAt(nx,nz)){
                charX_=nx; charZ_=nz;
            } else {
                // Slide along the obstacle instead of getting stuck against it.
                const float sx=charX_+dx*step;
                if(!collidesAt(sx,charZ_)) charX_=sx;
                const float sz=charZ_+dz*step;
                if(!collidesAt(charX_,sz)) charZ_=sz;
            }
            charYawDeg_=std::atan2(dx,dz)*180.f/(float)M_PI; state_=CharState::WALKING; resolveCollisions();
        } else state_=CharState::IDLE;
        autonomousCooldown_=std::max(0.0f,autonomousCooldown_-dt);
        if(!hasTap && autonomousCooldown_<=0.0f && state_==CharState::IDLE) chooseAutonomousAction();
        if(wantsAction){
            ZoneType n=computeNearbyZone();
            float tx,tz,ty;
            if(n!=ZoneType::NONE && getInteractionAnchor(n,&tx,&tz,&ty)){
                actionTargetX_=tx; actionTargetZ_=tz; actionTargetYaw_=ty;
                getActionPose(n,&actionPoseX_,&actionPoseY_,&actionPoseZ_,&actionPoseYaw_);
                busyKind_=n; actionElapsed_=0.0f;
                if(rebuildPathTo(actionTargetX_,actionTargetZ_)) state_=CharState::APPROACHING;
                else { busyKind_=ZoneType::NONE; state_=CharState::IDLE; }
                std::lock_guard<std::mutex> l(inputMutex_); inputDx_=inputDz_=0.f;
            }
        }
    }
    publishSnapshot();
}

bool Game::rebuildPathTo(float targetX, float targetZ) {
    path_.clear();
    pathIndex_ = 0;
    auto toCell=[](float v)->int {
        return static_cast<int>(std::lround((v - kNavMin) / kNavStep));
    };
    auto cellToWorld=[](int c)->float { return kNavMin + static_cast<float>(c) * kNavStep; };
    auto valid=[](int c)->bool { return c >= 0 && c < kNavCells; };
    auto id=[](int x,int z)->int { return z * kNavCells + x; };

    int sx=toCell(charX_), sz=toCell(charZ_);
    int tx=toCell(targetX), tz=toCell(targetZ);
    sx=std::max(0,std::min(kNavCells-1,sx)); sz=std::max(0,std::min(kNavCells-1,sz));
    tx=std::max(0,std::min(kNavCells-1,tx)); tz=std::max(0,std::min(kNavCells-1,tz));

    auto walkable=[&](int x,int z)->bool {
        if(!valid(x)||!valid(z)) return false;
        return !collidesAt(cellToWorld(x),cellToWorld(z));
    };
    // If a requested point is inside an object, choose the closest walkable
    // cell around it rather than sending the character into the obstacle.
    if(!walkable(tx,tz)) {
        bool found=false;
        for(int r=1;r<8 && !found;r++) {
            for(int dz=-r;dz<=r && !found;dz++) for(int dx=-r;dx<=r;dx++) {
                int x=tx+dx,z=tz+dz;
                if(valid(x)&&valid(z)&&walkable(x,z)){tx=x;tz=z;found=true;break;}
            }
        }
        if(!found) return false;
    }
    if(!walkable(sx,sz)) {
        // Current position is normally walkable; this is only a safety net.
        for(int r=1;r<5 && !walkable(sx,sz);r++) {
            bool moved=false;
            for(int dz=-r;dz<=r && !moved;dz++) for(int dx=-r;dx<=r;dx++) {
                int x=sx+dx,z=sz+dz;
                if(valid(x)&&valid(z)&&walkable(x,z)){sx=x;sz=z;moved=true;break;}
            }
        }
    }

    std::array<int,kNavCells*kNavCells> prev{};
    std::array<unsigned char,kNavCells*kNavCells> seen{};
    std::array<int,kNavCells*kNavCells> queue{};
    prev.fill(-1);
    int head=0,tail=0; const int start=id(sx,sz), goal=id(tx,tz);
    queue[tail++]=start; seen[start]=1;
    const int dirs[8][2]={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
    while(head<tail && !seen[goal]) {
        const int cur=queue[head++]; const int cx=cur%kNavCells, czc=cur/kNavCells;
        for(const auto& d:dirs) {
            int nx=cx+d[0], nz=czc+d[1];
            if(!valid(nx)||!valid(nz)) continue;
            if(d[0]!=0 && d[1]!=0 && (!walkable(cx+d[0],czc) || !walkable(cx,czc+d[1]))) continue;
            int ni=id(nx,nz); if(seen[ni]||!walkable(nx,nz)) continue;
            seen[ni]=1; prev[ni]=cur; queue[tail++]=ni;
        }
    }
    if(!seen[goal]) return false;

    std::vector<int> cells;
    for(int cur=goal;cur!=start;cur=prev[cur]) cells.push_back(cur);
    cells.push_back(start);
    std::reverse(cells.begin(),cells.end());
    // Remove redundant intermediate points on straight runs so the character
    // walks in long smooth segments instead of visibly following grid cells.
    int lastDx=0,lastDz=0;
    for(size_t i=1;i<cells.size();++i) {
        int ax=cells[i-1]%kNavCells, az=cells[i-1]/kNavCells;
        int bx=cells[i]%kNavCells, bz=cells[i]/kNavCells;
        int dx=(bx>ax)-(bx<ax), dz=(bz>az)-(bz<az);
        if(i==1) lastDx=dx,lastDz=dz;
        if(dx!=lastDx || dz!=lastDz) {
            path_.push_back({cellToWorld(ax),cellToWorld(az)});
            lastDx=dx; lastDz=dz;
        }
    }
    path_.push_back({cellToWorld(tx),cellToWorld(tz)});
    // The first waypoint can be the current cell; skip it.
    if(!path_.empty()) {
        const float dx=path_[0].first-charX_, dz=path_[0].second-charZ_;
        if(dx*dx+dz*dz<0.12f*0.12f) pathIndex_=1;
    }
    return !path_.empty();
}

void Game::getActionPose(ZoneType type, float* outX, float* outY, float* outZ, float* outYaw) const {
    switch(type){
        case ZoneType::SOFA:   *outX=-2.0f; *outY=0.62f; *outZ=0.45f; *outYaw=180.f; return;
        case ZoneType::BED:    *outX=2.2f; *outY=.05f; *outZ=-1.90f; *outYaw=0.f; return;
        case ZoneType::SHOWER: *outX=3.80f; *outY=.06f; *outZ=3.10f; *outYaw=0.f; return;
        case ZoneType::SINK:   *outX=2.08f; *outY=.02f; *outZ=1.00f; *outYaw=90.f; return;
        case ZoneType::KITCHEN:*outX=-2.0f; *outY=.02f; *outZ=-2.55f; *outYaw=0.f; return;
        case ZoneType::TOILET: *outX=3.15f; *outY=.02f; *outZ=1.35f; *outYaw=0.f; return;
        case ZoneType::FRIDGE: *outX=-4.00f; *outY=.02f; *outZ=-2.25f; *outYaw=0.f; return;
        case ZoneType::DESK:   *outX=-0.80f; *outY=.02f; *outZ=2.10f; *outYaw=0.f; return;
        default: *outX=charX_; *outY=0.f; *outZ=charZ_; *outYaw=charYawDeg_; return;
    }
}

bool Game::getInteractionAnchor(ZoneType type, float* outX, float* outZ, float* outYaw) const {
    float bestD=9999.f; const Furniture* best=nullptr;
    for(const auto& f:furniture_) if(f.type==type){
        const float dx=charX_-f.x, dz=charZ_-f.z, d=std::sqrt(dx*dx+dz*dz);
        if(d<bestD){bestD=d;best=&f;}
    }
    if(!best) return false;
    float x=best->x, z=best->z;
    switch(type){
        case ZoneType::BED:    z=best->z+best->sizeZ*.5f+.48f; break;
        case ZoneType::SHOWER: x=best->x; z=best->z-best->sizeZ*.5f-.48f; break;
        case ZoneType::SINK:   x=best->x-best->sizeX*.5f-.58f; z=best->z; break;
        case ZoneType::TOILET: z=best->z-best->sizeZ*.5f-.55f; break;
        case ZoneType::KITCHEN:x=best->x; z=best->z+best->sizeZ*.5f+.30f; break;
        case ZoneType::FRIDGE: z=best->z+best->sizeZ*.5f+.62f; break;
        case ZoneType::SOFA:   z=best->z-best->sizeZ*.5f-.62f; break;
        case ZoneType::DESK:   x=best->x+0.15f; z=best->z-best->sizeZ*.5f-.50f; break;
        default: return false;
    }
    // Face the usable surface/furniture, not an arbitrary world direction.
    *outX=x; *outZ=z;
    *outYaw=std::atan2(best->x-x,best->z-z)*180.f/(float)M_PI;
    return true;
}

bool Game::collidesAt(float x,float z) const {
    const float oldX=charX_, oldZ=charZ_;
    const_cast<Game*>(this)->charX_=x; const_cast<Game*>(this)->charZ_=z;
    bool hit = x < -houseHalfWidth_+.42f || x > houseHalfWidth_-.42f ||
               z < -houseHalfDepth_+.42f || z > houseHalfDepth_-.42f;
    if(!hit){
        for(const auto& f:furniture_) {
            if(f.type==ZoneType::NONE) continue;
            const float hx=f.sizeX*.5f+.34f, hz=f.sizeZ*.5f+.34f;
            if(std::fabs(x-f.x)<hx && std::fabs(z-f.z)<hz){ hit=true; break; }
        }
    }
    if(!hit){
        for(const auto& w:wallSegments_) {
            const float hx=w.sizeX*.5f+.34f, hz=w.sizeZ*.5f+.34f;
            if(std::fabs(x-w.x)<hx && std::fabs(z-w.z)<hz){ hit=true; break; }
        }
    }
    const_cast<Game*>(this)->charX_=oldX; const_cast<Game*>(this)->charZ_=oldZ;
    return hit;
}

void Game::chooseAutonomousAction(){
    ZoneType need=ZoneType::NONE;
    if(needs_[0] < 22.f) need=ZoneType::KITCHEN;
    else if(needs_[2] < 22.f) need=ZoneType::SHOWER;
    else if(needs_[1] < 22.f) need=ZoneType::BED;
    if(need==ZoneType::NONE) return;
    float tx,tz,ty;
    if(!getInteractionAnchor(need,&tx,&tz,&ty)) return;
    actionTargetX_=tx; actionTargetZ_=tz; actionTargetYaw_=ty;
    getActionPose(need,&actionPoseX_,&actionPoseY_,&actionPoseZ_,&actionPoseYaw_);
    busyKind_=need; actionElapsed_=0.f;
    if(!rebuildPathTo(actionTargetX_,actionTargetZ_)) { busyKind_=ZoneType::NONE; state_=CharState::IDLE; return; }
    tapTargetActive_=false; autonomousCooldown_=8.f;
    std::lock_guard<std::mutex> l(inputMutex_); inputDx_=inputDz_=0.f;
}

void Game::collideBox(float x,float z,float sx,float sz){
    const float hx=sx*.5f+.34f,hz=sz*.5f+.34f,dx=charX_-x,dz=charZ_-z;
    if(std::fabs(dx)<hx&&std::fabs(dz)<hz){const float ox=hx-std::fabs(dx),oz=hz-std::fabs(dz);if(ox<oz)charX_+=(dx<0?-ox:ox);else charZ_+=(dz<0?-oz:oz);}
}
void Game::resolveCollisions(){
    charX_=clamp(charX_,-houseHalfWidth_+.42f,houseHalfWidth_-.42f);charZ_=clamp(charZ_,-houseHalfDepth_+.42f,houseHalfDepth_-.42f);
    for(const auto& f:furniture_)collideBox(f.x,f.z,f.sizeX,f.sizeZ);for(const auto&w:wallSegments_)collideBox(w.x,w.z,w.sizeX,w.sizeZ);
}
ZoneType Game::computeNearbyZone() const{
    ZoneType best=ZoneType::NONE;float bestD=kInteractionRadius;
    for(const auto& f:furniture_){if(f.type==ZoneType::NONE)continue;float dx=charX_-f.x,dz=charZ_-f.z,d=std::sqrt(dx*dx+dz*dz);if(d<bestD){bestD=d;best=f.type;}}
    return best;
}
void Game::publishSnapshot(){std::lock_guard<std::mutex> l(stateMutex_);snapCharX_=charX_;snapCharY_=0.f;
    if(state_==CharState::BUSY){
        if(busyKind_==ZoneType::SOFA){
            // Root lowers during sit-down and rises again during stand-up.
            const float p=clamp(actionElapsed_/kActionDurationSeconds,0.f,1.f);
            float lift=0.f;
            if(p<0.28f){ float q=p/0.28f; q=q*q*(3.f-2.f*q); lift=q; }
            else if(p<0.82f) lift=1.f;
            else { float q=(p-0.82f)/0.18f; q=q*q*(3.f-2.f*q); lift=1.f-q; }
            snapCharY_=actionPoseY_*lift;
        } else snapCharY_=actionPoseY_;
    }snapCharZ_=charZ_;snapYaw_=charYawDeg_;snapNeeds_=needs_;snapZone_=(state_==CharState::BUSY?ZoneType::NONE:computeNearbyZone());snapActionZone_=((state_==CharState::BUSY || state_==CharState::APPROACHING)?busyKind_:ZoneType::NONE);snapState_=state_;snapActionProgress_=clamp(actionElapsed_/kActionDurationSeconds,0.f,1.f);}
void Game::getCharacterTransform(float*x,float*y,float*z,float*yaw)const{std::lock_guard<std::mutex>l(stateMutex_);*x=snapCharX_;*y=snapCharY_;*z=snapCharZ_;*yaw=snapYaw_;}
void Game::getNeeds(float*out)const{std::lock_guard<std::mutex>l(stateMutex_);out[0]=snapNeeds_[0];out[1]=snapNeeds_[1];out[2]=snapNeeds_[2];}
ZoneType Game::getNearbyZone()const{std::lock_guard<std::mutex>l(stateMutex_);return snapZone_;}
CharState Game::getState()const{std::lock_guard<std::mutex>l(stateMutex_);return snapState_;}
ZoneType Game::getActionZone()const{std::lock_guard<std::mutex>l(stateMutex_);return snapActionZone_;}

float Game::getActionProgress() const { std::lock_guard<std::mutex> l(stateMutex_); return snapActionProgress_; }
