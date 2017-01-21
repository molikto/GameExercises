cmake_minimum_required(VERSION 2.8.4)
project(graphics001)

if (NOT CMAKE_BUILD_TYPE)
  message(STATUS "Build type defaulting to \"Release\"")
  set(CMAKE_BUILD_TYPE "Release")
endif()

IF (APPLE)
	# set(CMAKE_CXX_COMPILER "/usr/bin/g++")
	set(CMAKE_CXX_FLAGS "-std=c++11 -lglfw3 -framework Cocoa -framework OpenGL -framework IOKit -framework CoreVideo")
ENDIF (APPLE)

# Defines where all custom CMake scripts go.
set(CMAKE_SCRIPT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/cmake ${CMAKE_MODULE_PATH})
# Defines where all source code goes.
set(SRC_DIR ${CMAKE_SOURCE_DIR}/src)
set(SRC_THIRDPARTY_DIR ${CMAKE_SOURCE_DIR}/thirdparty)
# Defines where all shader files go.
set(SHADER_DIR ${SRC_DIR}/shaders)

# Defines where the built source goes.
set(BUILD_DIR "${CMAKE_SOURCE_DIR}/build")
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/build")

####### Dependencies
set(DEPS ${CMAKE_SOURCE_DIR}/deps)

# GLFW
set(GLFW_ROOT_DIR ${DEPS}/glfw-3.0.4)
add_subdirectory(${GLFW_ROOT_DIR})


include_directories(${DEPS}/stb)

find_package(GLEW REQUIRED)
# set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -I/usr/include")

include_directories(${GLFW_INCLUDE_DIRS})


set(SOURCE_FILES    ${SRC_DIR}/main.cpp
					${SRC_DIR}/bitmap.cpp
					${SRC_DIR}/common.cpp
					${SRC_DIR}/game.cpp
					${SRC_DIR}/graphics.cpp
					${SRC_DIR}/shader.cpp
					${SRC_DIR}/texture.cpp
					${SRC_DIR}/model/model.cpp
					)

add_executable(graphics001 ${SOURCE_FILES})

target_link_libraries(graphics001 ${glfw_LIBRARIES}
							${GLEW_LIBRARY})