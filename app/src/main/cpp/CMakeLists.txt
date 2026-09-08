cmake_minimum_required(VERSION 3.22.1)
project(maisonviegame)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(maisonviegame SHARED
    native-lib.cpp
    Renderer.cpp
    Game.cpp
)

find_library(log-lib log)
find_library(gles3-lib GLESv3)
find_library(android-lib android)

target_link_libraries(maisonviegame
    ${log-lib}
    ${gles3-lib}
    ${android-lib}
)

# -O3: the simulation + render loop runs every frame at 60fps.
target_compile_options(maisonviegame PRIVATE -O3 -Wall -Wextra)
