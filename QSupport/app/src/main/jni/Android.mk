LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := q-support
LOCAL_SRC_FILES := main.cpp q-supportAI.cpp
LOCAL_CPPFLAGS += -fexceptions
LOCAL_CPP_FEATURES := exceptions
LOCAL_LDLIBS := -llog -landroid


include $(BUILD_SHARED_LIBRARY)

