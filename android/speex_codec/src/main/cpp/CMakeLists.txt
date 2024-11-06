# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

set(PYTHON_EXECUTABLE "python3")

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("speex_codec")

add_library(kissfft STATIC
        kissfft/kiss_fft.c
        kissfft/kfc.c
        kissfft/kiss_fftnd.c
        kissfft/kiss_fftndr.c
        kissfft/kiss_fftr.c
)

target_include_directories(kissfft PRIVATE
        kissfft
)

target_compile_definitions(kissfft PRIVATE
        KISSFFT_DATATYPE=int16_t
        KISSFFT_TOOLS=0
)
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        speex_codec.cpp

        speex/libspeex/speex.c
        speex/libspeex/nb_celp.c
        speex/libspeex/sb_celp.c
        speex/libspeex/window.c
        speex/libspeex/vbr.c
        speex/libspeex/filters.c
        speex/libspeex/ltp.c
        speex/libspeex/lpc.c
        speex/libspeex/lsp.c
        speex/libspeex/bits.c
        speex/libspeex/stereo.c
        speex/libspeex/vq.c
        speex/libspeex/cb_search.c
        speex/libspeex/speex_callbacks.c
        speex/libspeex/quant_lsp.c
        speex/libspeex/lsp_tables_nb.c
        speex/libspeex/high_lsp_tables.c
        speex/libspeex/modes.c
        speex/libspeex/modes_wb.c
        speex/libspeex/exc_5_64_table.c
        speex/libspeex/exc_5_256_table.c
        speex/libspeex/exc_8_128_table.c
        speex/libspeex/exc_10_16_table.c
        speex/libspeex/exc_10_32_table.c
        speex/libspeex/exc_20_32_table.c
        speex/libspeex/hexc_table.c
        speex/libspeex/hexc_10_32_table.c
        speex/libspeex/gain_table.c
        speex/libspeex/gain_table_lbr.c

        speexdsp/libspeexdsp/buffer.c
        speexdsp/libspeexdsp/fftwrap.c
        speexdsp/libspeexdsp/filterbank.c
        speexdsp/libspeexdsp/jitter.c
        speexdsp/libspeexdsp/kiss_fft.c
        speexdsp/libspeexdsp/kiss_fftr.c
        speexdsp/libspeexdsp/mdf.c
        speexdsp/libspeexdsp/preprocess.c
        speexdsp/libspeexdsp/resample.c
        speexdsp/libspeexdsp/scal.c
        speexdsp/libspeexdsp/smallft.c
)

target_compile_options(${CMAKE_PROJECT_NAME} PRIVATE
        "$<$<CONFIG:RELEASE>:-O3>"
        "$<$<CONFIG:DEBUG>:-O3>"
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        # List paths to include headers from
        include
        speex/include
        speexdsp/include
)

target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
        FIXED_POINT
        "EXPORT=/* */"
        USE_KISS_FFT
)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        android
        log
        kissfft
)