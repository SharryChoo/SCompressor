# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

# 判断编译器类型,如果是 gcc 编译器,则在编译选项中加入c++11支持
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")
if (CMAKE_COMPILER_IS_GNUCXX)
    set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS}")
    message(STATUS "optional:-std=c++11")
endif (CMAKE_COMPILER_IS_GNUCXX)

# 设置变量, 找到存放资源的目录, ".."代表上一级目录
set(source_dir ${CMAKE_SOURCE_DIR}/src/main)

# 链接头文件
include_directories(${source_dir}/jniLibs/include)

# libjpeg-turbo
add_library(libjpeg SHARED IMPORTED)
set_target_properties(
        libjpeg
        PROPERTIES
        IMPORTED_LOCATION
        ${source_dir}/jniLibs/armeabi/libjpeg.so
)

# 添加要打包的资源
add_library(
        # 打包后的库名称
        libscompressor
        # 库类型
        SHARED
        # 打包的文件
        ${source_dir}/cpp/lib_compressor_Core.cpp
        ${source_dir}/cpp/libjpegturbo_utils.cpp
)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(
        # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log
)

# 将打包的 so 链接到项目中
target_link_libraries(
        # Specifies the target library.
        libscompressor
        # 外部链接库
        jnigraphics
        libjpeg
        #        libturbojpeg
        #        libturbojpeg0
        # Links the target library to the log library
        # included in the NDK.
        log
)