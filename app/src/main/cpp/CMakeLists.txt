# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall")

#Set .so library path
set(soundtouch_DIR ${CMAKE_SOURCE_DIR}/../../../../soundtouch)

#Add .so library and define it's path
add_library(lib_soundtouch SHARED IMPORTED)
set_target_properties(lib_soundtouch
                      PROPERTIES IMPORTED_LOCATION
                      ${soundtouch_DIR}/lib/${ANDROID_ABI}/libsoundtouch.so)

#Add jni library and define it's path
add_library(soundtouch-jni SHARED soundtouch-jni.cpp)
target_include_directories(soundtouch-jni PRIVATE ${soundtouch_DIR}/include)


#Link .so library to jni library
target_link_libraries(soundtouch-jni
                      lib_soundtouch
                      android
                      log)




