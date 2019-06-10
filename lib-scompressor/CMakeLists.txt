# Sets the minimum version of CMake required to build the native library.
CMAKE_MINIMUM_REQUIRED(VERSION 3.4.1)

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
        # Anroid libs
        jnigraphics
        log
)