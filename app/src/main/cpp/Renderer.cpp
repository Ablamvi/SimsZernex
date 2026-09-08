#include "Renderer.h"

#include <android/log.h>
#include <time.h>
#include <cmath>
#include <algorithm>
#include <android/asset_manager.h>
#include <cstring>
#include <cstdint>
#include <string>
#include <array>

#include "shaders.h"

#define LOG_TAG "MaisonVieRenderer"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
constexpr int kFloatsPerVertex = 6; // position + normal
constexpr int kFloatsPerInstance = 11; // offset + scale + rgba + yaw
constexpr float kPi = 3.14159265358979323846f;

long long nowNanos() {
    struct timespec ts{};
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return static_cast<long long>(ts.tv_sec) * 1000000000LL + ts.tv_nsec;
}

void addVertex(std::vector<float>& v, float x,float y,float z,float nx,float ny,float nz){
    v.insert(v.end(), {x,y,z,nx,ny,nz});
}

std::vector<float> makeCube(){
    const float q=.5f;
    const float raw[] = {
     q,-q,-q,1,0,0, q,q,-q,1,0,0, q,q,q,1,0,0, q,-q,-q,1,0,0, q,q,q,1,0,0, q,-q,q,1,0,0,
    -q,-q,q,-1,0,0,-q,q,q,-1,0,0,-q,q,-q,-1,0,0,-q,-q,q,-1,0,0,-q,q,-q,-1,0,0,-q,-q,-q,-1,0,0,
    -q,q,-q,0,1,0,-q,q,q,0,1,0,q,q,q,0,1,0,-q,q,-q,0,1,0,q,q,q,0,1,0,q,q,-q,0,1,0,
    -q,-q,q,0,-1,0,-q,-q,-q,0,-1,0,q,-q,-q,0,-1,0,-q,-q,q,0,-1,0,q,-q,-q,0,-1,0,q,-q,q,0,-1,0,
     q,-q,q,0,0,1,q,q,q,0,0,1,-q,q,q,0,0,1,q,-q,q,0,0,1,-q,q,q,0,0,1,-q,-q,q,0,0,1,
    -q,-q,-q,0,0,-1,-q,q,-q,0,0,-1,q,q,-q,0,0,-1,-q,-q,-q,0,0,-1,q,q,-q,0,0,-1,q,-q,-q,0,0,-1
    };
    return std::vector<float>(raw, raw + sizeof(raw)/sizeof(float));
}

std::vector<float> makeSphere(int slices=18,int stacks=12){
    std::vector<float> v;
    for(int y=0;y<stacks;y++){
        float v0=(float)y/stacks, v1=(float)(y+1)/stacks;
        float p0=v0*kPi-kPi*.5f, p1=v1*kPi-kPi*.5f;
        for(int x=0;x<slices;x++){
            float u0=(float)x/slices, u1=(float)(x+1)/slices;
            float t0=u0*2*kPi, t1=u1*2*kPi;
            auto P=[](float p,float t){return Vec3{.5f*std::cos(p)*std::cos(t),.5f*std::sin(p),.5f*std::cos(p)*std::sin(t)};};
            Vec3 a=P(p0,t0), b=P(p1,t0), c=P(p1,t1), d=P(p0,t1);
            auto V=[&](const Vec3& p){float l=std::sqrt(p.x*p.x+p.y*p.y+p.z*p.z); addVertex(v,p.x,p.y,p.z,p.x/l,p.y/l,p.z/l);};
            V(a);V(b);V(c); V(a);V(c);V(d);
        }
    }
    return v;
}

std::vector<float> makeCylinder(int sides=16){
    std::vector<float> v;
    for(int i=0;i<sides;i++){
        float a0=2*kPi*i/sides,a1=2*kPi*(i+1)/sides;
        float x0=.5f*std::cos(a0),z0=.5f*std::sin(a0),x1=.5f*std::cos(a1),z1=.5f*std::sin(a1);
        addVertex(v,x0,-.5f,z0,std::cos(a0),0,std::sin(a0)); addVertex(v,x0,.5f,z0,std::cos(a0),0,std::sin(a0)); addVertex(v,x1,.5f,z1,std::cos(a1),0,std::sin(a1));
        addVertex(v,x0,-.5f,z0,std::cos(a0),0,std::sin(a0)); addVertex(v,x1,.5f,z1,std::cos(a1),0,std::sin(a1)); addVertex(v,x1,-.5f,z1,std::cos(a1),0,std::sin(a1));
        addVertex(v,0,.5f,0,0,1,0); addVertex(v,x0,.5f,z0,0,1,0); addVertex(v,x1,.5f,z1,0,1,0);
        addVertex(v,0,-.5f,0,0,-1,0); addVertex(v,x1,-.5f,z1,0,-1,0); addVertex(v,x0,-.5f,z0,0,-1,0);
    }
    return v;
}
} // namespace

Renderer::Renderer(AAssetManager* assets) : assets_(assets) {}
Renderer::~Renderer(){
    if(instanceVbo_) glDeleteBuffers(1,&instanceVbo_);
    for(Mesh* m : {&cube_,&sphere_,&cylinder_}) {
        if(m->vbo) glDeleteBuffers(1,&m->vbo);
        if(m->vao) glDeleteVertexArrays(1,&m->vao);
    }
    if(characterVbo_) glDeleteBuffers(1,&characterVbo_);
    if(characterEbo_) glDeleteBuffers(1,&characterEbo_);
    if(characterVao_) glDeleteVertexArrays(1,&characterVao_);
    if(houseVbo_) glDeleteBuffers(1,&houseVbo_);
    if(houseEbo_) glDeleteBuffers(1,&houseEbo_);
    if(houseVao_) glDeleteVertexArrays(1,&houseVao_);
    if(boneTexture_) glDeleteTextures(1,&boneTexture_);
    if(program_) glDeleteProgram(program_);
    if(houseProgram_) glDeleteProgram(houseProgram_);
    if(characterProgram_) glDeleteProgram(characterProgram_);
}

GLuint Renderer::compileShader(GLenum type,const char* src){
    GLuint s=glCreateShader(type); glShaderSource(s,1,&src,nullptr); glCompileShader(s);
    GLint ok=0; glGetShaderiv(s,GL_COMPILE_STATUS,&ok); if(!ok){char log[512];glGetShaderInfoLog(s,sizeof(log),nullptr,log);LOGE("Shader compile: %s",log);glDeleteShader(s);return 0;} return s;
}
void Renderer::buildProgram(){
    GLuint vs=compileShader(GL_VERTEX_SHADER,kVertexShaderSrc), fs=compileShader(GL_FRAGMENT_SHADER,kFragmentShaderSrc);
    if(!vs || !fs){ if(vs) glDeleteShader(vs); if(fs) glDeleteShader(fs); return; }
    program_=glCreateProgram(); glAttachShader(program_,vs);glAttachShader(program_,fs);glLinkProgram(program_);
    GLint ok=0;glGetProgramiv(program_,GL_LINK_STATUS,&ok);if(!ok){char log[512];glGetProgramInfoLog(program_,sizeof(log),nullptr,log);LOGE("Program link: %s",log);} glDeleteShader(vs);glDeleteShader(fs);
    uViewProjLoc_=glGetUniformLocation(program_,"uViewProj");uLightDirLoc_=glGetUniformLocation(program_,"uLightDir");
}
void Renderer::buildMesh(Mesh& m,const std::vector<float>& v){
    glGenBuffers(1,&m.vbo);glBindBuffer(GL_ARRAY_BUFFER,m.vbo);glBufferData(GL_ARRAY_BUFFER,v.size()*sizeof(float),v.data(),GL_STATIC_DRAW);m.vertexCount=(int)v.size()/kFloatsPerVertex;
    glGenVertexArrays(1,&m.vao);glBindVertexArray(m.vao);glBindBuffer(GL_ARRAY_BUFFER,m.vbo);
    glEnableVertexAttribArray(0);glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,kFloatsPerVertex*sizeof(float),(void*)0);
    glEnableVertexAttribArray(1);glVertexAttribPointer(1,3,GL_FLOAT,GL_FALSE,kFloatsPerVertex*sizeof(float),(void*)(3*sizeof(float)));
    glBindBuffer(GL_ARRAY_BUFFER,instanceVbo_);GLsizei stride=kFloatsPerInstance*sizeof(float);
    glEnableVertexAttribArray(2);glVertexAttribPointer(2,3,GL_FLOAT,GL_FALSE,stride,(void*)0);glVertexAttribDivisor(2,1);
    glEnableVertexAttribArray(3);glVertexAttribPointer(3,3,GL_FLOAT,GL_FALSE,stride,(void*)(3*sizeof(float)));glVertexAttribDivisor(3,1);
    glEnableVertexAttribArray(4);glVertexAttribPointer(4,4,GL_FLOAT,GL_FALSE,stride,(void*)(6*sizeof(float)));glVertexAttribDivisor(4,1);
    glEnableVertexAttribArray(5);glVertexAttribPointer(5,1,GL_FLOAT,GL_FALSE,stride,(void*)(10*sizeof(float)));glVertexAttribDivisor(5,1);
    glBindVertexArray(0);
}
void Renderer::buildInstanceBuffer(){glGenBuffers(1,&instanceVbo_);glBindBuffer(GL_ARRAY_BUFFER,instanceVbo_);glBufferData(GL_ARRAY_BUFFER,1,nullptr,GL_DYNAMIC_DRAW);}
void Renderer::buildMeshes(){buildMesh(cube_,makeCube());buildMesh(sphere_,makeSphere());buildMesh(cylinder_,makeCylinder());}

void Renderer::buildCharacterProgram(){
    GLuint vs=compileShader(GL_VERTEX_SHADER,kCharacterVertexShaderSrc);
    GLuint fs=compileShader(GL_FRAGMENT_SHADER,kCharacterFragmentShaderSrc);
    if(!vs || !fs) return;
    characterProgram_=glCreateProgram();
    glAttachShader(characterProgram_,vs);
    glAttachShader(characterProgram_,fs);
    glLinkProgram(characterProgram_);
    GLint ok=0;
    glGetProgramiv(characterProgram_,GL_LINK_STATUS,&ok);
    if(!ok){
        char log[512]{};
        glGetProgramInfoLog(characterProgram_,sizeof(log),nullptr,log);
        LOGE("Character program link: %s",log);
        glDeleteProgram(characterProgram_);
        characterProgram_=0;
    }
    glDeleteShader(vs);
    glDeleteShader(fs);
    if(!characterProgram_) return;
    characterViewProjLoc_=glGetUniformLocation(characterProgram_,"uViewProj");
    characterLightDirLoc_=glGetUniformLocation(characterProgram_,"uLightDir");
    characterPosLoc_=glGetUniformLocation(characterProgram_,"uCharacterPos");
    characterScaleLoc_=glGetUniformLocation(characterProgram_,"uCharacterScale");
    characterYawLoc_=glGetUniformLocation(characterProgram_,"uCharacterYaw");
    characterBoneTextureLoc_=glGetUniformLocation(characterProgram_,"uBoneTexture");
    characterBoneCountLoc_=glGetUniformLocation(characterProgram_,"uBoneCount");
}

bool Renderer::loadCharacterMesh(){
    if(!assets_) { LOGE("Character asset manager is null"); return false; }
    AAsset* asset=AAssetManager_open(assets_,"models/character.skinnedbin",AASSET_MODE_BUFFER);
    if(!asset){ LOGE("Missing assets/models/character.skinnedbin"); return false; }
    const size_t length=static_cast<size_t>(AAsset_getLength(asset));
    const unsigned char* data=static_cast<const unsigned char*>(AAsset_getBuffer(asset));
    if(!data || length<20 || std::memcmp(data,"ZSKN",4)!=0){ AAsset_close(asset); LOGE("Invalid skinned character header"); return false; }
    size_t off=4;
    auto readU32=[&](){ std::uint32_t v; std::memcpy(&v,data+off,4); off+=4; return v; };
    const std::uint32_t vertexCount=readU32();
    const std::uint32_t indexCount=readU32();
    const std::uint32_t boneCount=readU32();
    (void)readU32();
    if(vertexCount==0 || indexCount==0 || boneCount==0 || boneCount>128){ AAsset_close(asset); LOGE("Invalid skinned counts"); return false; }

    // Vertex: position(3f), normal(3f), joints(4u16), weights(4f), color(3f) = 60 bytes.
    const size_t vertexStride=60;
    const size_t vertexBytes=static_cast<size_t>(vertexCount)*vertexStride;
    const size_t indexBytes=static_cast<size_t>(indexCount)*sizeof(std::uint32_t);
    if(off+vertexBytes+indexBytes>length){ AAsset_close(asset); LOGE("Skinned character data truncated"); return false; }

    glGenBuffers(1,&characterVbo_);
    glBindBuffer(GL_ARRAY_BUFFER,characterVbo_);
    glBufferData(GL_ARRAY_BUFFER,(GLsizeiptr)vertexBytes,data+off,GL_STATIC_DRAW);
    off+=vertexBytes;

    glGenBuffers(1,&characterEbo_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,characterEbo_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,(GLsizeiptr)indexBytes,data+off,GL_STATIC_DRAW);
    off+=indexBytes;

    glGenVertexArrays(1,&characterVao_);
    glBindVertexArray(characterVao_);
    glBindBuffer(GL_ARRAY_BUFFER,characterVbo_);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,characterEbo_);
    glEnableVertexAttribArray(0); glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,vertexStride,(void*)0);
    glEnableVertexAttribArray(1); glVertexAttribPointer(1,3,GL_FLOAT,GL_FALSE,vertexStride,(void*)(12));
    glEnableVertexAttribArray(2); glVertexAttribIPointer(2,4,GL_UNSIGNED_SHORT,vertexStride,(void*)(24));
    glEnableVertexAttribArray(3); glVertexAttribPointer(3,4,GL_FLOAT,GL_FALSE,vertexStride,(void*)(32));
    glEnableVertexAttribArray(4); glVertexAttribPointer(4,3,GL_FLOAT,GL_FALSE,vertexStride,(void*)(48));
    glBindVertexArray(0);

    characterParents_.resize(boneCount);
    characterRestLocal_.resize(boneCount);
    characterInverseBind_.resize(boneCount);
    characterBoneNames_.resize(boneCount);
    characterBoneMatrices_.resize(static_cast<size_t>(boneCount)*16);

    auto readI32=[&](){ std::int32_t v; std::memcpy(&v,data+off,4); off+=4; return v; };
    auto readF32=[&](){ float v; std::memcpy(&v,data+off,4); off+=4; return v; };
    for(std::uint32_t b=0;b<boneCount;b++){
        characterParents_[b]=readI32(); (void)readI32();
        const std::uint32_t nameLen=readU32();
        if(off+16*sizeof(float)+16*sizeof(float)+nameLen>length || nameLen>256){
            AAsset_close(asset); LOGE("Invalid bone record %u",b); return false;
        }
        for(int k=0;k<16;k++) characterRestLocal_[b].m[k]=readF32();
        for(int k=0;k<16;k++) characterInverseBind_[b].m[k]=readF32();
        characterBoneNames_[b].assign(reinterpret_cast<const char*>(data+off),nameLen); off+=nameLen;
    }
    characterVertexCount_=static_cast<int>(vertexCount);
    characterIndexCount_=static_cast<int>(indexCount);
    characterBoneCount_=static_cast<int>(boneCount);

    glGenTextures(1,&boneTexture_);
    glBindTexture(GL_TEXTURE_2D,boneTexture_);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA32F,characterBoneCount_*4,1,0,GL_RGBA,GL_FLOAT,characterBoneMatrices_.data());
    glBindTexture(GL_TEXTURE_2D,0);
    AAsset_close(asset);
    LOGE("Skinned character loaded: %u vertices, %u indices, %u bones",vertexCount,indexCount,boneCount);
    return true;
}


bool Renderer::loadHouseMesh(){
    if(!assets_) return false;
    AAsset* asset=AAssetManager_open(assets_,"models/house.scene.bin",AASSET_MODE_BUFFER);
    if(!asset){ LOGE("Missing assets/models/house.scene.bin"); return false; }
    const size_t length=(size_t)AAsset_getLength(asset);
    const unsigned char* data=(const unsigned char*)AAsset_getBuffer(asset);
    if(!data || length<16 || std::memcmp(data,"HSCN",4)!=0){ AAsset_close(asset); return false; }
    size_t off=4;
    auto u32=[&](){ std::uint32_t v; std::memcpy(&v,data+off,4); off+=4; return v; };
    const std::uint32_t version=u32();
    const std::uint32_t vc=u32();
    const std::uint32_t ic=u32();
    if(version != 1u || !vc || !ic || off + (size_t)vc*36 + (size_t)ic*4 > length){
        AAsset_close(asset);
        LOGE("Invalid house scene header: version=%u vertices=%u indices=%u length=%zu", version, vc, ic, length);
        return false;
    }
    glGenBuffers(1,&houseVbo_); glBindBuffer(GL_ARRAY_BUFFER,houseVbo_);
    glBufferData(GL_ARRAY_BUFFER,(GLsizeiptr)vc*36,data+off,GL_STATIC_DRAW); off += (size_t)vc*36;
    glGenBuffers(1,&houseEbo_); glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,houseEbo_);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER,(GLsizeiptr)ic*4,data+off,GL_STATIC_DRAW);
    glGenVertexArrays(1,&houseVao_); glBindVertexArray(houseVao_);
    glBindBuffer(GL_ARRAY_BUFFER,houseVbo_); glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,houseEbo_);
    glEnableVertexAttribArray(0); glVertexAttribPointer(0,3,GL_FLOAT,GL_FALSE,36,(void*)0);
    glEnableVertexAttribArray(1); glVertexAttribPointer(1,3,GL_FLOAT,GL_FALSE,36,(void*)12);
    glEnableVertexAttribArray(2); glVertexAttribPointer(2,3,GL_FLOAT,GL_FALSE,36,(void*)24);
    glBindVertexArray(0); AAsset_close(asset); houseIndexCount_=(int)ic;
    LOGE("House mesh loaded: %u vertices, %u indices",vc,ic); return true;
}

void Renderer::drawHouse(){
    if(!houseProgram_ || !houseVao_ || houseIndexCount_<=0) return;
    glUseProgram(houseProgram_);
    glUniformMatrix4fv(houseViewProjLoc_,1,GL_FALSE,characterVp_.m);
    glUniform3f(houseLightDirLoc_,-.4f,-1.f,-.3f);
    glDisable(GL_CULL_FACE); // visible from the playable interior and exterior
    glBindVertexArray(houseVao_);
    glDrawElements(GL_TRIANGLES,houseIndexCount_,GL_UNSIGNED_INT,nullptr);
    glBindVertexArray(0);
    glEnable(GL_CULL_FACE);
}

int Renderer::findBone(const char* name) const{
    for(int i=0;i<(int)characterBoneNames_.size();++i){
        if(characterBoneNames_[i].find(name)!=std::string::npos) return i;
    }
    return -1;
}

namespace {
Mat4 mulM(const Mat4& a,const Mat4& b){ return Mat4::multiply(a,b); }
Mat4 rotX(float deg){
    Mat4 r=Mat4::identity(); float a=deg*kPi/180.f,c=std::cos(a),s=std::sin(a);
    r.m[5]=c; r.m[9]=-s; r.m[6]=s; r.m[10]=c; return r;
}
Mat4 rotZ(float deg){
    Mat4 r=Mat4::identity(); float a=deg*kPi/180.f,c=std::cos(a),s=std::sin(a);
    r.m[0]=c; r.m[4]=-s; r.m[1]=s; r.m[5]=c; return r;
}
}

void Renderer::updateCharacterBones(float t){
    if(characterBoneCount_<=0 || !boneTexture_) return;
    std::vector<Mat4> globals(characterBoneCount_);
    std::vector<Mat4> locals=characterRestLocal_;
    const CharState state=game_.getState();
    ZoneType action=game_.getActionZone();
    const float actionProgress=game_.getActionProgress();
    enum Anim { IDLE, WALK, SLEEP, EAT, SHOWER, SIT, WORK } anim=IDLE;
    if(state==CharState::WALKING || state==CharState::APPROACHING) anim=WALK;
    else if(state==CharState::BUSY){
        if(action==ZoneType::BED) anim=SLEEP;
        else if(action==ZoneType::SHOWER || action==ZoneType::SINK) anim=SHOWER;
        else if(action==ZoneType::KITCHEN || action==ZoneType::FRIDGE) anim=EAT;
        else if(action==ZoneType::SOFA) anim=SIT;
        else if(action==ZoneType::DESK) anim=WORK;
    }

    auto B=[&](const char* n){ return findBone(n); };
    const int hips=B("CC_Base_Hip_02");
    const int pelvis=B("CC_Base_Pelvis_03");
    const int spine1=B("CC_Base_Spine01_034");
    const int spine2=B("CC_Base_Spine02_035");
    const int head=B("CC_Base_Head_038");
    const int lThigh=B("CC_Base_L_Thigh_04"); const int rThigh=B("CC_Base_R_Thigh_018");
    const int lCalf=B("CC_Base_L_Calf_05"); const int rCalf=B("CC_Base_R_Calf_021");
    const int lUpper=B("CC_Base_L_Upperarm_050"); const int rUpper=B("CC_Base_R_Upperarm_074");
    const int lFore=B("CC_Base_L_Forearm_051"); const int rFore=B("CC_Base_R_Forearm_077");
    const int lHand=B("CC_Base_L_Hand_055"); const int rHand=B("CC_Base_R_Hand_081");

    auto add=[&](int b,const Mat4& d){ if(b>=0) locals[b]=mulM(locals[b],d); };
    const float phase=t*6.6f;
    if(anim==WALK){
        // The supplied CC_Base rig has the upper arms almost horizontal in
        // its rest pose.  The correct axis for lowering them is local Z, not
        // X.  Use opposite Z rotations on the two shoulders so both hands
        // hang beside the hips instead of pointing upward.
        const float step=std::sin(phase);
        const float opp=-step;
        const float fastKnee=std::max(0.f,-step);
        const float fastKneeR=std::max(0.f,-opp);
        add(lThigh,rotX(step*27.f)); add(rThigh,rotX(opp*27.f));
        add(lCalf,rotX(fastKnee*22.f)); add(rCalf,rotX(fastKneeR*22.f));

        // Neutral arm pose is deliberately DOWN and is never replaced by a
        // T-pose.  The small +/- swing is layered around that neutral pose.
        // Left arm: negative Z lowers it; right arm: positive Z lowers it.
        const float armSwing=step*10.f;
        add(lUpper,rotZ(-82.f + armSwing));
        add(rUpper,rotZ( 82.f + armSwing));
        add(lFore,rotX(3.f)); add(rFore,rotX(3.f));
        add(lHand,rotZ(std::sin(phase+0.4f)*3.f));
        add(rHand,rotZ(std::sin(phase+3.54f)*3.f));
        add(hips,rotZ(std::sin(phase*0.5f)*2.0f));
        add(spine1,rotZ(-std::sin(phase)*0.8f));
        add(head,rotZ(-std::sin(phase*0.5f)*0.8f));
    } else if(anim==SLEEP){
        add(hips,rotX(-62.f)); add(pelvis,rotX(-12.f)); add(spine1,rotX(-18.f)); add(spine2,rotX(-15.f));
        add(lThigh,rotX(55.f)); add(rThigh,rotX(55.f)); add(lCalf,rotX(-70.f)); add(rCalf,rotX(-70.f));
        add(lUpper,rotX(165.f)); add(rUpper,rotX(165.f)); add(head,rotX(8.f));
    } else if(anim==EAT){
        const float s=std::sin(t*3.2f);
        add(lUpper,rotX(135.f)); add(rUpper,rotX(135.f)); add(lFore,rotX(-52.f)); add(rFore,rotX(-52.f));
        add(lHand,rotX(18.f+s*8.f)); add(rHand,rotX(18.f-s*8.f)); add(head,rotX(8.f+std::sin(t*2.0f)*4.f));
    } else if(anim==SHOWER){
        add(lUpper,rotX(35.f)); add(rUpper,rotX(35.f)); add(lFore,rotX(-55.f)); add(rFore,rotX(-55.f));
        add(spine1,rotX(std::sin(t*2.0f)*3.f)); add(head,rotZ(std::sin(t*1.8f)*5.f));
    } else if(anim==SIT){
        // Continuous sit/stand curve. 0-.28 = turn/lower, .28-.82 = seated,
        // .82-1 = stand. Smoothstep removes the robotic pop at both ends.
        float q=0.f;
        if(actionProgress<0.28f){ q=actionProgress/0.28f; q=q*q*(3.f-2.f*q); }
        else if(actionProgress<0.82f) q=1.f;
        else { q=(actionProgress-0.82f)/0.18f; q=1.f-(q*q*(3.f-2.f*q)); }
        add(hips,rotX(-68.f*q));
        add(lThigh,rotX(72.f*q)); add(rThigh,rotX(72.f*q));
        add(lCalf,rotX(-78.f*q)); add(rCalf,rotX(-78.f*q));
        add(spine1,rotX(10.f*q));
        // Arms relax onto the thighs once seated.
        add(lUpper,rotX((154.f-12.f*q))); add(rUpper,rotX((154.f-12.f*q)));
        add(lFore,rotX((-16.f+18.f*q))); add(rFore,rotX((-16.f+18.f*q)));
        add(head,rotX(2.f*q));
    } else if(anim==WORK){
        add(lUpper,rotX(145.f)); add(rUpper,rotX(145.f)); add(lFore,rotX(-65.f)); add(rFore,rotX(-65.f));
        add(head,rotX(14.f+std::sin(t*1.5f)*2.f));
    } else if(anim==IDLE){
        // Permanent relaxed standing pose: arms stay down even when the Sim
        // is doing absolutely nothing.  Only a tiny breathing/weight shift is
        // animated; there is no return to the original raised-arm rest pose.
        const float breath=std::sin(t*1.55f);
        add(lUpper,rotZ(-82.f + breath*1.2f));
        add(rUpper,rotZ( 82.f + breath*1.2f));
        add(lFore,rotX(3.f)); add(rFore,rotX(3.f));
        add(lHand,rotZ(std::sin(t*1.4f)*1.2f));
        add(rHand,rotZ(std::sin(t*1.4f+0.25f)*1.2f));
        add(spine1,rotZ(breath*0.7f));
        add(head,rotZ(std::sin(t*1.3f)*1.0f));
    }

    for(int i=0;i<characterBoneCount_;++i){
        const int p=characterParents_[i];
        globals[i]=(p<0)?locals[i]:mulM(globals[p],locals[i]);
        Mat4 skin=mulM(globals[i],characterInverseBind_[i]);
        for(int k=0;k<16;k++) characterBoneMatrices_[static_cast<size_t>(i)*16+k]=skin.m[k];
    }
    glBindTexture(GL_TEXTURE_2D,boneTexture_);
    glTexSubImage2D(GL_TEXTURE_2D,0,0,0,characterBoneCount_*4,1,GL_RGBA,GL_FLOAT,characterBoneMatrices_.data());
    glBindTexture(GL_TEXTURE_2D,0);
}

void Renderer::onSurfaceCreated(){
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glClearColor(.07f,.08f,.11f,1);
    buildProgram();
    { GLuint vs=compileShader(GL_VERTEX_SHADER,kHouseVertexShaderSrc), fs=compileShader(GL_FRAGMENT_SHADER,kHouseFragmentShaderSrc);
      if(vs&&fs){ houseProgram_=glCreateProgram(); glAttachShader(houseProgram_,vs); glAttachShader(houseProgram_,fs); glLinkProgram(houseProgram_);
        GLint ok=0; glGetProgramiv(houseProgram_,GL_LINK_STATUS,&ok); if(!ok){glDeleteProgram(houseProgram_); houseProgram_=0;}
        glDeleteShader(vs); glDeleteShader(fs); if(houseProgram_){houseViewProjLoc_=glGetUniformLocation(houseProgram_,"uViewProj"); houseLightDirLoc_=glGetUniformLocation(houseProgram_,"uLightDir");}}
    }
    buildCharacterProgram();
    buildInstanceBuffer();
    buildMeshes();
    loadCharacterMesh();
    loadHouseMesh();
    lastFrameNanos_=nowNanos();
}
void Renderer::onSurfaceChanged(int w,int h){viewportWidth_=w>0?w:1;viewportHeight_=h>0?h:1;glViewport(0,0,viewportWidth_,viewportHeight_);}

void Renderer::push(MeshKind k,float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float a,float yaw){
    auto* dst=&boxInstances_;if(k==MeshKind::SPHERE)dst=&sphereInstances_;else if(k==MeshKind::CYLINDER)dst=&cylinderInstances_;
    dst->insert(dst->end(),{x,y,z,sx,sy,sz,r,g,b,a,yaw});
}
void Renderer::pushBox(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float a,float yaw){push(MeshKind::BOX,x,y,z,sx,sy,sz,r,g,b,a,yaw);}
void Renderer::pushSphere(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float a,float yaw){push(MeshKind::SPHERE,x,y,z,sx,sy,sz,r,g,b,a,yaw);}
void Renderer::pushCylinder(float x,float y,float z,float sx,float sy,float sz,float r,float g,float b,float a,float yaw){push(MeshKind::CYLINDER,x,y,z,sx,sy,sz,r,g,b,a,yaw);}

void Renderer::buildSceneInstances(){
    // The corrected house.glb is authoritative for walls, floor, sofa, bed,
    // kitchen, shower, vanity and dining set. These procedural meshes fill
    // only the interaction furniture that is intentionally not duplicated in
    // the supplied GLB: desk and toilet. The supplied table and refrigerator
    // are baked into the scene from their own GLBs.
    boxInstances_.clear(); sphereInstances_.clear(); cylinderInstances_.clear();
    for(const auto& f: game_.furniture()){
        const float x=f.x, z=f.z;
        switch(f.type){
            case ZoneType::TOILET:
                pushSphere(x,.40f,z-.16f,.78f,.52f,.86f,.90f,.91f,.89f);
                pushSphere(x,.56f,z-.16f,.52f,.15f,.56f,.35f,.38f,.39f);
                pushBox(x,.92f,z+.30f,.82f,.68f,.42f,.88f,.89f,.87f);
                pushBox(x,.86f,z+.49f,.10f,.66f,.62f,.78f,.79f,.78f);
                break;
            case ZoneType::DESK:
                pushBox(x,.73f,z,2.0f,.13f,.90f,.50f,.34f,.18f);
                for(float dx:{-.82f,.82f}) for(float dz:{-.32f,.32f})
                    pushCylinder(x+dx,.36f,z+dz,.07f,.72f,.07f,.58f,.55f,.48f);
                pushBox(x,1.10f,z-.08f,.82f,.50f,.10f,.10f,.13f,.16f);
                pushCylinder(x,.84f,z-.08f,.07f,.20f,.07f,.18f,.20f,.21f);
                pushCylinder(x-.72f,1.08f,z+.12f,.05f,.76f,.05f,.24f,.24f,.22f);
                pushSphere(x-.72f,1.50f,z+.12f,.38f,.16f,.38f,.82f,.70f,.45f);
                pushBox(x+.58f,.88f,z+.16f,.45f,.16f,.30f,.30f,.34f,.36f);
                break;
            default:
                break;
        }
    }
}

void Renderer::drawCharacter(float x,float y,float z,float yaw){
    if(!characterProgram_ || !characterVao_ || characterIndexCount_<=0 || !boneTexture_) return;
    glUseProgram(characterProgram_);
    glUniformMatrix4fv(characterViewProjLoc_,1,GL_FALSE,characterVp_.m);
    glUniform3f(characterLightDirLoc_,-.4f,-1.f,-.3f);
    glUniform3f(characterPosLoc_,x,y,z);
    glUniform1f(characterScaleLoc_,.55f);
    glUniform1f(characterYawLoc_,yaw*kPi/180.f);
    glUniform1i(characterBoneCountLoc_,characterBoneCount_);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D,boneTexture_);
    glUniform1i(characterBoneTextureLoc_,0);
    glDisable(GL_CULL_FACE);
    glBindVertexArray(characterVao_);
    glDrawElements(GL_TRIANGLES,characterIndexCount_,GL_UNSIGNED_INT,nullptr);
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D,0);
    glEnable(GL_CULL_FACE);
}

void Renderer::drawMeshInstances(const Mesh& mesh,const std::vector<float>& inst){
    if(inst.empty())return;
    glBindBuffer(GL_ARRAY_BUFFER,instanceVbo_);
    glBufferData(GL_ARRAY_BUFFER,inst.size()*sizeof(float),inst.data(),GL_DYNAMIC_DRAW);
    glBindVertexArray(mesh.vao);
    glDrawArraysInstanced(GL_TRIANGLES,0,mesh.vertexCount,(GLsizei)(inst.size()/kFloatsPerInstance));
}

bool Renderer::isReady() const {
    return houseProgram_ != 0 && characterProgram_ != 0 && houseVao_ != 0 && characterVao_ != 0 &&
           houseIndexCount_ > 0 && characterIndexCount_ > 0 && boneTexture_ != 0;
}

void Renderer::handleCameraDrag(float dxPixels, float dyPixels) {
    std::lock_guard<std::mutex> lock(cameraMutex_);
    cameraYawDeg_ += dxPixels * 0.28f;
    cameraPitchDeg_ = std::clamp(cameraPitchDeg_ - dyPixels * 0.22f, 28.0f, 68.0f);
    while (cameraYawDeg_ > 180.0f) cameraYawDeg_ -= 360.0f;
    while (cameraYawDeg_ < -180.0f) cameraYawDeg_ += 360.0f;
}

void Renderer::handleCameraZoom(float scaleFactor) {
    if (!(scaleFactor > 0.01f) || !std::isfinite(scaleFactor)) return;
    std::lock_guard<std::mutex> lock(cameraMutex_);
    cameraRadius_ = std::clamp(cameraRadius_ / scaleFactor, 2.8f, 4.4f);
}

void Renderer::handleTap(float screenX, float screenY) {
    if (viewportWidth_ <= 0 || viewportHeight_ <= 0) return;
    const float nx = (2.0f * screenX) / (float)viewportWidth_ - 1.0f;
    const float ny = 1.0f - (2.0f * screenY) / (float)viewportHeight_;
    float yawDeg, pitchDeg, radius;
    { std::lock_guard<std::mutex> lock(cameraMutex_); yawDeg=cameraYawDeg_; pitchDeg=cameraPitchDeg_; radius=cameraRadius_; }
    const float yaw = yawDeg * kPi / 180.0f;
    const float pitch = pitchDeg * kPi / 180.0f;
    const Vec3 target{0.0f, 0.70f, 0.0f};
    const Vec3 eye{
        target.x + radius * std::cos(pitch) * std::sin(yaw),
        target.y + radius * std::sin(pitch),
        target.z + radius * std::cos(pitch) * std::cos(yaw)
    };
    const Vec3 forward = Vec3::normalize(target - eye);
    const Vec3 right = Vec3::normalize(Vec3::cross(forward, Vec3{0,1,0}));
    const Vec3 up = Vec3::cross(right, forward);
    const float aspect = (float)viewportWidth_ / (float)viewportHeight_;
    const float tanHalf = std::tan(50.0f * kPi / 360.0f);
    const Vec3 rayDir = Vec3::normalize(forward + right * (nx * aspect * tanHalf) + up * (ny * tanHalf));

    // First test the actual furniture volumes. Previously a tap was always
    // projected onto the floor behind an object, so tapping a bed/sofa/etc.
    // could be interpreted as an ordinary floor move. A ray hit now selects
    // the object itself and automatically starts its interaction.
    float bestT = 1.0e30f;
    ZoneType hitZone = ZoneType::NONE;
    const auto& furniture = game_.furniture();
    for (const auto& f : furniture) {
        if (f.type == ZoneType::NONE) continue;
        const float minX = f.x - f.sizeX * 0.5f;
        const float maxX = f.x + f.sizeX * 0.5f;
        const float minY = 0.0f;
        const float maxY = std::max(0.15f, f.height);
        const float minZ = f.z - f.sizeZ * 0.5f;
        const float maxZ = f.z + f.sizeZ * 0.5f;
        float tmin = 0.0f, tmax = bestT;
        auto slab = [&](float origin, float dir, float lo, float hi) -> bool {
            if (std::fabs(dir) < 1.0e-6f) return origin >= lo && origin <= hi;
            float a = (lo - origin) / dir;
            float b = (hi - origin) / dir;
            if (a > b) std::swap(a, b);
            tmin = std::max(tmin, a);
            tmax = std::min(tmax, b);
            return tmin <= tmax;
        };
        if (slab(eye.x, rayDir.x, minX, maxX) &&
            slab(eye.y, rayDir.y, minY, maxY) &&
            slab(eye.z, rayDir.z, minZ, maxZ) &&
            tmin > 0.0f && tmin < bestT) {
            bestT = tmin;
            hitZone = f.type;
        }
    }

    if (hitZone != ZoneType::NONE) {
        const Vec3 hit = eye + rayDir * bestT;
        game_.tapAt(hit.x, hit.z);
        return;
    }

    // No object hit: use the floor as the movement target.
    if (std::fabs(rayDir.y) < 1e-5f) return;
    const float t = -eye.y / rayDir.y;
    if (t <= 0.0f) return;
    const Vec3 hit = eye + rayDir * t;
    game_.tapAt(hit.x, hit.z);
}

void Renderer::onDrawFrame(){
    long long now=nowNanos();
    float dt=(float)(now-lastFrameNanos_)/1e9f;
    lastFrameNanos_=now;
    if(dt>.1f)dt=.1f;
    game_.update(dt);
    animTime_+=dt;
    glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
    float cx,cy,cz,cyaw;
    game_.getCharacterTransform(&cx,&cy,&cz,&cyaw);
    // True orbit camera. It stays close enough to the room to keep the
    // character and furniture visible while allowing 3D rotation and zoom.
    float yawDeg, pitchDeg, radius;
    { std::lock_guard<std::mutex> lock(cameraMutex_); yawDeg=cameraYawDeg_; pitchDeg=cameraPitchDeg_; radius=cameraRadius_; }
    const float yaw = yawDeg * kPi / 180.0f;
    const float pitch = pitchDeg * kPi / 180.0f;
    Vec3 target{0.0f,0.70f,0.0f};
    Vec3 eye{
        target.x + radius * std::cos(pitch) * std::sin(yaw),
        target.y + radius * std::sin(pitch),
        target.z + radius * std::cos(pitch) * std::cos(yaw)
    };
    float aspect=(float)viewportWidth_/viewportHeight_;
    Mat4 proj=Mat4::perspective(50.f,aspect,.1f,80.f);
    Mat4 view=Mat4::lookAt(eye,target,Vec3{0,1,0});
    Mat4 vp=Mat4::multiply(proj,view);
    glUniformMatrix4fv(uViewProjLoc_,1,GL_FALSE,vp.m);
    glUniform3f(uLightDirLoc_,-.4f,-1.f,-.3f);
    characterVp_=vp;
    drawHouse();
    glUseProgram(program_);
    glUniformMatrix4fv(uViewProjLoc_,1,GL_FALSE,vp.m);
    glUniform3f(uLightDirLoc_,-.4f,-1.f,-.3f);
    buildSceneInstances();
    drawMeshInstances(cube_,boxInstances_);
    drawMeshInstances(sphere_,sphereInstances_);
    drawMeshInstances(cylinder_,cylinderInstances_);
    updateCharacterBones(animTime_);
    drawCharacter(cx,cy,cz,cyaw);
    glBindVertexArray(0);
}
