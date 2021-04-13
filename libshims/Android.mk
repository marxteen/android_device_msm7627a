LOCAL_PATH := $(call my-dir)

# Camera 
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
     gui/SensorManager.cpp \
     ui/GraphicBuffer.cpp \
     ui/GraphicBufferAllocator.cpp \
     ui/GraphicBufferMapper.cpp \
     media_shim.cpp \
     MemoryHeapPmem.cpp \
     MemoryBase.c \
     VectorImpl.c

LOCAL_SHARED_LIBRARIES := liblog libcutils libhardware libui libgui libbinder libutils libsync libmedia

LOCAL_MODULE := libshim_native
LOCAL_C_INCLUDES += $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr/include
LOCAL_ADDITIONAL_DEPENDENCIES := $(TARGET_OUT_INTERMEDIATES)/KERNEL_OBJ/usr
LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
