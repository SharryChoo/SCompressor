# Sets the minimum version of CMake required to build the native library.
CMAKE_MINIMUM_REQUIRED(VERSION 3.4.1)

# DEBUG 代码混淆
SET(CMAKE_C_FLAGS_DEBUG "${CMAKE_C_FLAGS_DEBUG} -mllvm -fla")
SET(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -mllvm -fla")
# RELEASE 代码混淆
SET(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -mllvm -fla")
SET(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -mllvm -fla")

# 链接头文件
INCLUDE_DIRECTORIES(${PROJECT_SOURCE_DIR}/src/main/jniLibs/include)

# 指定外部链接库目录
LINK_DIRECTORIES(${PROJECT_SOURCE_DIR}/src/main/jniLibs/armeabi-v7a)

# 对文件集合取变量
FILE(GLOB SRC_LIST "${PROJECT_SOURCE_DIR}/src/main/cpp/*.cpp")
# 添加要打包的资源
ADD_LIBRARY(
        # 打包后的库名称
        scompressor
        # 库类型
        SHARED
        # 打包的文件
        ${SRC_LIST}
)

# 为 scompressor 添加外部依赖
TARGET_LINK_LIBRARIES(
        # Specifies the target library.
        scompressor
        # 外部链接库
        jpeg
        turbojpeg
        # Anroid libs
        jnigraphics
        log
)
