#
# Copyright (C) 2016 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Build one shared lib. When we build, we set the bin to $user/tmp, and
# remove it after build complete, so only the distribution place could be
# used for application linking.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

#aux_source_directory(${CMAKE_CURRENT_SOURCE_DIR}/src/SoundTouch SRC_LIST)
file(GLOB_RECURSE SRC_LIST ${PROJECT_SOURCE_DIR}/*.cpp)
add_library(soundtouch SHARED ${SRC_LIST})
target_include_directories(soundtouch PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/include)

# copy out the lib binary and remove generated files
set(soundtouch_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../soundtouch)
set_target_properties(soundtouch
                      PROPERTIES
                      LIBRARY_OUTPUT_DIRECTORY
                      "${soundtouch_DIR}/soundtouch/lib/${ANDROID_ABI}")

add_custom_command(TARGET soundtouch POST_BUILD
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy "${CMAKE_CURRENT_SOURCE_DIR}/src/SoundStretch/WavFile.h"
                                      "${soundtouch_DIR}/include/WavFile.h"
                                      COMMENT "Copying gperf to output directory")

