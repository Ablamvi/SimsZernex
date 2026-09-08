#pragma once

// GLES 3.0 shaders for the house scene.
static const char* kVertexShaderSrc = R"(#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 iOffset;
layout(location = 3) in vec3 iScale;
layout(location = 4) in vec4 iColor;
layout(location = 5) in float iYawRad;
uniform mat4 uViewProj;
out vec3 vNormal;
out vec4 vColor;
void main() {
    vec3 local = aPosition * iScale;
    float c = cos(iYawRad), s = sin(iYawRad);
    vec3 rotated = vec3(local.x*c + local.z*s, local.y, -local.x*s + local.z*c);
    vec3 rotatedNormal = vec3(aNormal.x*c + aNormal.z*s, aNormal.y, -aNormal.x*s + aNormal.z*c);
    vNormal = rotatedNormal; vColor = iColor;
    gl_Position = uViewProj * vec4(rotated + iOffset, 1.0);
}
)";

static const char* kFragmentShaderSrc = R"(#version 300 es
precision mediump float;
in vec3 vNormal; in vec4 vColor;
uniform vec3 uLightDir;
out vec4 fragColor;
void main() {
    float diffuse = max(dot(normalize(vNormal), -normalize(uLightDir)), 0.0);
    float shade = 0.45 + diffuse * 0.55;
    fragColor = vec4(vColor.rgb * shade, vColor.a);
}
)";

// Static furnished house mesh loaded from the supplied GLB.
static const char* kHouseVertexShaderSrc = R"(#version 300 es
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in vec3 aColor;
uniform mat4 uViewProj;
out vec3 vNormal;
out vec3 vColor;
void main(){ vNormal=aNormal; vColor=aColor; gl_Position=uViewProj*vec4(aPosition,1.0); }
)";
static const char* kHouseFragmentShaderSrc = R"(#version 300 es
precision mediump float;
in vec3 vNormal; in vec3 vColor;
uniform vec3 uLightDir;
out vec4 fragColor;
void main(){ float d=max(dot(normalize(vNormal),-normalize(uLightDir)),0.0); fragColor=vec4(vColor*(0.45+0.55*d),1.0); }
)";

// Skinned character. Bone matrices are stored in a 1D RGBA32F texture:
// four texels (the four columns) per bone. This avoids a large uniform-array
// limit on GLES 3.0 devices while allowing the 102-bone rig to animate.
static const char* kCharacterVertexShaderSrc = R"(#version 300 es
precision highp float;
precision highp int;
layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aNormal;
layout(location = 2) in uvec4 aJoints;
layout(location = 3) in vec4 aWeights;
layout(location = 4) in vec3 aColor;

uniform mat4 uViewProj;
uniform vec3 uCharacterPos;
uniform float uCharacterScale;
uniform float uCharacterYaw;
uniform highp sampler2D uBoneTexture;
uniform int uBoneCount;

out vec3 vNormal;
out vec3 vColor;

mat4 boneMatrix(uint bone) {
    int base = int(bone) * 4;
    vec4 c0 = texelFetch(uBoneTexture, ivec2(base + 0, 0), 0);
    vec4 c1 = texelFetch(uBoneTexture, ivec2(base + 1, 0), 0);
    vec4 c2 = texelFetch(uBoneTexture, ivec2(base + 2, 0), 0);
    vec4 c3 = texelFetch(uBoneTexture, ivec2(base + 3, 0), 0);
    return mat4(c0,c1,c2,c3);
}

void main() {
    mat4 skin = boneMatrix(aJoints.x) * aWeights.x;
    skin += boneMatrix(aJoints.y) * aWeights.y;
    skin += boneMatrix(aJoints.z) * aWeights.z;
    skin += boneMatrix(aJoints.w) * aWeights.w;

    vec4 skinned = skin * vec4(aPosition, 1.0);
    vec3 skinnedNormal = mat3(skin) * aNormal;
    float c = cos(uCharacterYaw), s = sin(uCharacterYaw);
    vec3 local = skinned.xyz * uCharacterScale;
    vec3 rotated = vec3(local.x*c + local.z*s, local.y, -local.x*s + local.z*c);
    vNormal = normalize(vec3(skinnedNormal.x*c + skinnedNormal.z*s, skinnedNormal.y,
                             -skinnedNormal.x*s + skinnedNormal.z*c));
    vColor = aColor;
    gl_Position = uViewProj * vec4(rotated + uCharacterPos, 1.0);
}
)";

static const char* kCharacterFragmentShaderSrc = R"(#version 300 es
precision mediump float;
in vec3 vNormal; in vec3 vColor;
uniform vec3 uLightDir;
out vec4 fragColor;
void main() {
    float diffuse = max(dot(normalize(vNormal), -normalize(uLightDir)), 0.0);
    float shade = 0.50 + diffuse * 0.50;
    fragColor = vec4(vColor * shade, 1.0);
}
)";
