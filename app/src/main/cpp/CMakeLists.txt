# CMakeLists.txt - Native Security Module for Ktimaz Studio
cmake_minimum_required(VERSION 3.22.1)

project(KtimazSecurity
    VERSION 1.0.0
    DESCRIPTION "Advanced Native Security Module"
    LANGUAGES C CXX
)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Build configuration options
option(ENABLE_SECURITY_FEATURES "Enable advanced security features" ON)
option(ENABLE_DEBUG_LOGGING "Enable debug logging" OFF)
option(ENABLE_OBFUSCATION "Enable code obfuscation" OFF)
option(ENABLE_ANTI_DEBUG "Enable anti-debugging measures" OFF)

# Compiler-specific options
if(CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Wpedantic")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fstack-protector-strong")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC -fvisibility=hidden")
    
    if(CMAKE_BUILD_TYPE STREQUAL "Release")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -flto -ffunction-sections -fdata-sections")
        
        if(ENABLE_OBFUSCATION)
            set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-omit-frame-pointer")
            set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mllvm -fla -mllvm -sub -mllvm -bcf")
        endif()
    else()
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O0 -g")
    endif()
endif()

# Security hardening flags
if(ENABLE_SECURITY_FEATURES)
    add_definitions(-DENABLE_SECURITY_FEATURES)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_FORTIFY_SOURCE=2")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fstack-clash-protection")
    
    if(ENABLE_ANTI_DEBUG)
        add_definitions(-DENABLE_ANTI_DEBUG)
    endif()
endif()

# Debug logging
if(ENABLE_DEBUG_LOGGING)
    add_definitions(-DENABLE_DEBUG_LOGGING)
endif()

# Find required packages
find_library(log-lib log)
find_library(android-lib android)
find_library(jnigraphics-lib jnigraphics)

# Include directories
include_directories(
    ${CMAKE_SOURCE_DIR}/src/main/cpp/include
    ${CMAKE_SOURCE_DIR}/src/main/cpp/security
    ${CMAKE_SOURCE_DIR}/src/main/cpp/crypto
    ${CMAKE_SOURCE_DIR}/src/main/cpp/utils
)

# Source files
set(SECURITY_SOURCES
    # Main JNI interface
    src/main/cpp/ktimaz_security_jni.cpp
    src/main/cpp/jni_helpers.cpp
    
    # Security modules
    src/main/cpp/security/anti_debug.cpp
    src/main/cpp/security/anti_tampering.cpp
    src/main/cpp/security/integrity_check.cpp
    src/main/cpp/security/emulator_detection.cpp
    src/main/cpp/security/root_detection.cpp
    
    # Cryptographic functions
    src/main/cpp/crypto/native_crypto.cpp
    src/main/cpp/crypto/hash_functions.cpp
    src/main/cpp/crypto/secure_random.cpp
    
    # Utility functions
    src/main/cpp/utils/string_obfuscation.cpp
    src/main/cpp/utils/system_info.cpp
    src/main/cpp/utils/native_utils.cpp
)

# Header files
set(SECURITY_HEADERS
    src/main/cpp/include/ktimaz_security.h
    src/main/cpp/include/jni_helpers.h
    src/main/cpp/include/security_config.h
    
    src/main/cpp/security/anti_debug.h
    src/main/cpp/security/anti_tampering.h
    src/main/cpp/security/integrity_check.h
    src/main/cpp/security/emulator_detection.h
    src/main/cpp/security/root_detection.h
    
    src/main/cpp/crypto/native_crypto.h
    src/main/cpp/crypto/hash_functions.h
    src/main/cpp/crypto/secure_random.h
    
    src/main/cpp/utils/string_obfuscation.h
    src/main/cpp/utils/system_info.h
    src/main/cpp/utils/native_utils.h
)

# Create the main security library
add_library(ktimaz-security
    SHARED
    ${SECURITY_SOURCES}
    ${SECURITY_HEADERS}
)

# Set library properties
set_target_properties(ktimaz-security PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION ${PROJECT_VERSION_MAJOR}
    CXX_VISIBILITY_PRESET hidden
    VISIBILITY_INLINES_HIDDEN ON
)

# Link libraries
target_link_libraries(ktimaz-security
    ${log-lib}
    ${android-lib}
    ${jnigraphics-lib}
)

# Additional security library for advanced features
add_library(ktimaz-crypto
    SHARED
    src/main/cpp/crypto/aes_encryption.cpp
    src/main/cpp/crypto/rsa_operations.cpp
    src/main/cpp/crypto/ecdh_keyexchange.cpp
    src/main/cpp/crypto/secure_memory.cpp
)

target_link_libraries(ktimaz-crypto
    ${log-lib}
    ktimaz-security
)

# Utility library for system operations
add_library(ktimaz-utils
    STATIC
    src/main/cpp/utils/file_operations.cpp
    src/main/cpp/utils/process_monitor.cpp
    src/main/cpp/utils/memory_utils.cpp
)

target_link_libraries(ktimaz-utils
    ${log-lib}
    ${android-lib}
)

# Link utils to main security library
target_link_libraries(ktimaz-security ktimaz-utils)

# Compiler definitions based on build configuration
target_compile_definitions(ktimaz-security PRIVATE
    $<$<CONFIG:Debug>:DEBUG_BUILD>
    $<$<CONFIG:Release>:RELEASE_BUILD>
    ANDROID_NDK_VERSION="${ANDROID_NDK_VERSION}"
    BUILD_TIMESTAMP="${CMAKE_BUILD_TIMESTAMP}"
)

# Installation rules
install(TARGETS ktimaz-security ktimaz-crypto
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
)

# Custom commands for security hardening
if(ENABLE_OBFUSCATION AND CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(TARGET ktimaz-security POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E echo "🔒 Applying security obfuscation..."
        COMMAND ${CMAKE_STRIP} --strip-unneeded $<TARGET_FILE:ktimaz-security>
        COMMENT "Stripping debug symbols and applying obfuscation"
    )
endif()

# Generate build information
configure_file(
    "${CMAKE_SOURCE_DIR}/src/main/cpp/include/build_config.h.in"
    "${CMAKE_BINARY_DIR}/generated/build_config.h"
)

target_include_directories(ktimaz-security PRIVATE
    "${CMAKE_BINARY_DIR}/generated"
)

# Testing (if enabled)
option(BUILD_TESTS "Build test suite" OFF)
if(BUILD_TESTS)
    enable_testing()
    
    add_executable(security_tests
        src/test/cpp/test_anti_debug.cpp
        src/test/cpp/test_crypto.cpp
        src/test/cpp/test_integrity.cpp
    )
    
    target_link_libraries(security_tests
        ktimaz-security
        ${log-lib}
    )
    
    add_test(NAME SecurityTests COMMAND security_tests)
endif()

# Print build configuration
message(STATUS "🏗️ Ktimaz Security Native Build Configuration:")
message(STATUS "   Build Type: ${CMAKE_BUILD_TYPE}")
message(STATUS "   Security Features: ${ENABLE_SECURITY_FEATURES}")
message(STATUS "   Debug Logging: ${ENABLE_DEBUG_LOGGING}")
message(STATUS "   Obfuscation: ${ENABLE_OBFUSCATION}")
message(STATUS "   Anti-Debug: ${ENABLE_ANTI_DEBUG}")
message(STATUS "   Target ABIs: ${ANDROID_ABI}")
message(STATUS "   NDK Version: ${ANDROID_NDK_VERSION}")
message(STATUS "   API Level: ${ANDROID_PLATFORM_LEVEL}")