// Minimal column-major 4x4 matrix math for the native visualizer.
// Deliberately dependency-free (no glm) to keep the native library tiny
// and the build simple - this is the entire math library the renderer needs.
#pragma once

#include <cmath>
#include <cstring>

struct Vec3 {
    float x = 0.f, y = 0.f, z = 0.f;

    Vec3() = default;
    Vec3(float x_, float y_, float z_) : x(x_), y(y_), z(z_) {}

    Vec3 operator-(const Vec3& o) const { return {x - o.x, y - o.y, z - o.z}; }
    Vec3 operator+(const Vec3& o) const { return {x + o.x, y + o.y, z + o.z}; }
    Vec3 operator*(float s) const { return {x * s, y * s, z * s}; }

    static Vec3 cross(const Vec3& a, const Vec3& b) {
        return {a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x};
    }

    static float dot(const Vec3& a, const Vec3& b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    static Vec3 normalize(const Vec3& v) {
        float len = std::sqrt(dot(v, v));
        if (len < 1e-6f) return {0.f, 0.f, 0.f};
        return {v.x / len, v.y / len, v.z / len};
    }
};

// Column-major, matches GLES conventions (m[column * 4 + row]).
struct Mat4 {
    float m[16]{};

    static Mat4 identity() {
        Mat4 r;
        r.m[0] = r.m[5] = r.m[10] = r.m[15] = 1.f;
        return r;
    }

    static Mat4 multiply(const Mat4& a, const Mat4& b) {
        Mat4 r;
        for (int col = 0; col < 4; ++col) {
            for (int row = 0; row < 4; ++row) {
                float sum = 0.f;
                for (int k = 0; k < 4; ++k) {
                    sum += a.m[k * 4 + row] * b.m[col * 4 + k];
                }
                r.m[col * 4 + row] = sum;
            }
        }
        return r;
    }

    static Mat4 perspective(float fovYDeg, float aspect, float near, float far) {
        Mat4 r;
        const float f = 1.0f / std::tan((fovYDeg * 0.5f) * (float) M_PI / 180.0f);
        r.m[0] = f / aspect;
        r.m[5] = f;
        r.m[10] = (far + near) / (near - far);
        r.m[11] = -1.0f;
        r.m[14] = (2.0f * far * near) / (near - far);
        return r;
    }

    static Mat4 lookAt(const Vec3& eye, const Vec3& center, const Vec3& up) {
        Vec3 f = Vec3::normalize(center - eye);
        Vec3 s = Vec3::normalize(Vec3::cross(f, up));
        Vec3 u = Vec3::cross(s, f);

        Mat4 r = Mat4::identity();
        r.m[0] = s.x;  r.m[4] = s.y;  r.m[8] = s.z;
        r.m[1] = u.x;  r.m[5] = u.y;  r.m[9] = u.z;
        r.m[2] = -f.x; r.m[6] = -f.y; r.m[10] = -f.z;
        r.m[12] = -Vec3::dot(s, eye);
        r.m[13] = -Vec3::dot(u, eye);
        r.m[14] = Vec3::dot(f, eye);
        return r;
    }

    static Mat4 rotateY(float degrees) {
        Mat4 r = Mat4::identity();
        const float rad = degrees * (float) M_PI / 180.0f;
        const float c = std::cos(rad);
        const float s = std::sin(rad);
        r.m[0] = c;  r.m[8] = s;
        r.m[2] = -s; r.m[10] = c;
        return r;
    }
};
