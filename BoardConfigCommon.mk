#
# Copyright 2017 The Android Open Source Project
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

# WARNING: This line must come *before* including the proprietary
# variant, so that it gets overwritten by the parent (which goes
# against the traditional rules of inheritance).
# The proprietary variant sets USE_CAMERA_STUB := false, this way
# we use the camera stub when the vendor tree isn't present, and
# the true camera library when the vendor tree is available.

LOCAL_PATH := device/samsung/msm7627a-common

# Inherit from the proprietary version
-include vendor/samsung/delos3geur/BoardConfigVendor.mk

# Compile sys
#TARGET_GCC_VERSION_EXP := 4.8
DISABLE_DEXPREOPT := true

# Compiler flags
TARGET_GLOBAL_CFLAGS += -mtune=cortex-a5 -mfpu=neon-vfpv4 -mfloat-abi=softfp
TARGET_GLOBAL_CPPFLAGS += -mtune=cortex-a5 -mfpu=neon-vfpv4 -mfloat-abi=softfp
TARGET_SPECIFIC_HEADER_PATH := device/samsung/msm7627a-common/include
#TARGET_SPECIFIC_HEADER_PATH += 
# Qualcomm hardware
BOARD_USES_QCOM_HARDWARE := true
COMMON_GLOBAL_CFLAGS += -DQCOM_HARDWARE
TARGET_OTA_ASSERT_DEVICE := delos3geur,GT-I8552b,I8552,msm7x27a

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := MSM8225
TARGET_NO_BOOTLOADER := true
TARGET_NO_RADIOIMAGE := true

# Architecture and CPU
TARGET_ARCH := arm
TARGET_ARCH_VARIANT := armv7-a-neon
TARGET_CPU_ABI := armeabi-v7a
TARGET_CPU_ABI2 := armeabi
TARGET_CPU_VARIANT := cortex-a9
TARGET_BOARD_PLATFORM := msm7627a
TARGET_CPU_SMP := true

# Optimisations
TARGET_USE_QCOM_BIONIC_OPTIMIZATION := true
TARGET_CORTEX_CACHE_LINE_32 := true
TARGET_AVOID_DRAW_TEXTURE_EXTENSION := true
TARGET_USES_16BPPSURFACE_FOR_OPAQUE := true
ARCH_ARM_HIGH_OPTIMIZATION := true
ARCH_ARM_HAVE_32_BYTE_CACHE_LINES := true

# Kernel
BOARD_KERNEL_CMDLINE := androidboot.hardware=qcom hack_lcd=1 chg_hack_lcd=0
BOARD_KERNEL_CMDLINE += androidboot.selinux=permissive
BOARD_KERNEL_BASE := 0x00200000
BOARD_KERNEL_PAGESIZE := 4096
BOARD_MKBOOTIMG_ARGS := --ramdisk_offset 0x01300000 
#TARGET_KERNEL_CROSS_COMPILE_PREFIX := /opt/toolchains/arm-eabi-4.7/bin/arm-eabi-
TARGET_KERNEL_CONFIG := gtl8552b_defconfig
TARGET_KERNEL_SOURCE := kernel/samsung/delos3geur
PRODUCT_COPY_FILES += device/samsung/msm7627a-common/rootdir/initlogo_i8552.rle:root/initlogo.rle
DEVICE_PACKAGE_OVERLAYS += device/samsung/msm7627a-common/overlay_delos

# Hardware rendering
BOARD_EGL_WORKAROUND_BUG_10194508 := true
#BOARD_USE_MHEAP_SCREENSHOT := true
TARGET_RUNNING_WITHOUT_SYNC_FRAMEWORK := true
TARGET_REQUIRES_SYNCHRONOUS_SETSURFACE := true

# Partitions
BOARD_SYSTEMIMAGE_PARTITION_SIZE := 996147200
BOARD_USERDATAIMAGE_PARTITION_SIZE := 5443168000
BOARD_RECOVERYIMAGE_PARTITION_SIZE := 13471744
BOARD_CACHEIMAGE_PARTITION_SIZE := 737280000
BOARD_FLASH_BLOCK_SIZE := 131072
TARGET_USERIMAGES_USE_EXT4 := true
BOARD_CACHE_DEVICE := /dev/block/mmcblk0p22
BOARD_CACHE_FILESYSTEM := ext4
BOARD_CACHE_FILESYSTEM_OPTIONS := rw
BOARD_SYSTEM_DEVICE := /dev/block/mmcblk0p21
BOARD_SYSTEM_FILESYSTEM := ext4
BOARD_SYSTEM_FILESYSTEM_OPTIONS := rw
BOARD_DATA_DEVICE := /dev/block/mmcblk0p24
BOARD_DATA_FILESYSTEM := ext4
BOARD_DATA_FILESYSTEM_OPTIONS := rw
TARGET_USERIMAGES_USE_F2FS := true

# Dalvik
TARGET_ARCH_LOWMEM := true

# Charger
BOARD_CHARGER_SHOW_PERCENTAGE := true

# Low RAM settings
MALLOC_IMPL := dlmalloc
TARGET_BOOTANIMATION_HALF_RES := true
TARGET_BOOTANIMATION_TEXTURE_CACHE := false

# Media
TARGET_QCOM_MEDIA_VARIANT := caf
COMMON_GLOBAL_CFLAGS += -DLPA_DEFAULT_BUFFER_SIZE=480
COMMON_GLOBAL_CFLAGS += -DQCOM_NO_SECURE_PLAYBACK
#TARGET_ENABLE_QC_AV_ENHANCEMENTS := true
#COMMON_GLOBAL_CFLAGS += -DQCOM_LEGACY_MMPARSER

## Audio
TARGET_QCOM_AUDIO_VARIANT := caf
#BOARD_QCOM_VOIP_ENABLED := true
BOARD_USES_LEGACY_ALSA_AUDIO := true
#USE_CUSTOM_AUDIO_POLICY := 0
#USE_LEGACY_AUDIO_POLICY := 1
#COMMON_GLOBAL_CFLAGS += -DNO_TUNNELED_SOURCE
#TARGET_HAS_QACT := true

# Charger
BOARD_CHARGER_RES := device/samsung/msm7627a-common/charger
BOARD_BATTERY_DEVICE_NAME := "battery"
BOARD_CHARGER_ENABLE_SUSPEND := true
BOARD_LPM_BOOT_ARGUMENT_NAME := androidboot.bootchg
BOARD_LPM_BOOT_ARGUMENT_VALUE := true

PRODUCT_PREBUILT_WEBVIEWCHROMIUM := yes

# Misc.
TARGET_SYSTEM_PROP := $(LOCAL_PATH)/system.prop

## Override healthd HAL
#BOARD_HAL_STATIC_LIBRARIES := libhealthd.msm7x27a

## Use device specific modules
TARGET_PROVIDES_LIBLIGHTS := true

# Bluetooth
BOARD_HAVE_BLUETOOTH := true
BOARD_HAVE_BLUETOOTH_CSR := true
#BOARD_HAVE_BLUETOOTH_BLUEZ := true

# GPS
#QCOM_GPS_PATH := hardware/qcom/gps
QCOM_GPS_PATH := device/samsung/msm7627a-common/gps
BOARD_USES_QCOM_GPS := true
BOARD_VENDOR_QCOM_GPS_LOC_API_AMSS_VERSION := 50000
BOARD_GPS_LOC_API_HARDWARE := $(TARGET_BOARD_PLATFORM)
#BOARD_USES_QCOM_LIBRPC := true

# GPU
TARGET_BOARD_PLATFORM_GPU := qcom-adreno200

# Graphics
COMMON_GLOBAL_CFLAGS += -DUSE_MDP3
TARGET_USES_QCOM_BSP := true
#COMMON_GLOBAL_CFLAGS += -DQCOM_BSP
TARGET_GRALLOC_USES_ASHMEM := true
BOARD_ADRENO_DECIDE_TEXTURE_TARGET := true
TARGET_NO_COMPAT_GRALLOC_PERFORM := true
USE_OPENGL_RENDERER := true
TARGET_DISPLAY_USE_RETIRE_FENCE := true
TARGET_USES_ION := true
COMMON_GLOBAL_CFLAGS += -DHAVE_NEW_GRALLOC
BOARD_USES_PMEM_ADSP := true
BOARD_NEEDS_MEMORYHEAPPMEM := true
TARGET_QCOM_DISPLAY_VARIANT := caf
#TARGET_DOESNT_USE_FENCE_SYNC := true
#BOARD_EGL_CFG := $(LOCAL_PATH)/configs/etc/egl.cfg

# Camera
#USE_DEVICE_SPECIFIC_CAMERA := true
COMMON_GLOBAL_CFLAGS += -DBINDER_COMPAT
BOARD_GLOBAL_CFLAGS += -DSAMSUNG_CAMERA_LEGACY
BOARD_GLOBAL_CFLAGS += -DUSE_LEGACY_BLOBS
TARGET_PROVIDES_CAMERA_HAL := true
TARGET_NEED_CAMERA_ZSL := true
TARGET_NEED_FFC_PICTURE_FIXUP := true
TARGET_NEED_FFC_VIDEO_FIXUP := true
TARGET_NEED_DISABLE_FACE_DETECTION := true
TARGET_NEED_DISABLE_FACE_DETECTION_BOTH_CAMERAS := true
COMMON_GLOBAL_CFLAGS += -DNEEDS_VECTORIMPL_SYMBOLS

# Other
TARGET_BOOTANIMATION_PRELOAD := true
TARGET_BOOTANIMATION_TEXTURE_CACHE := true
BOARD_CHARGER_ENABLE_SUSPEND := true
ARCH_ARM_HAVE_TLS_REGISTER := true
BOARD_WANTS_EMMC_BOOT := true

# RIL
#BOARD_USES_LEGACY_RIL := true
TARGET_RIL_VARIANT := legacy
#BOARD_PROVIDES_LIBRIL := true
#BOARD_PROVIDES_RIL_REFERENCE := true
#BOARD_MOBILEDATA_INTERFACE_NAME := "pdp0"
COMMON_GLOBAL_CFLAGS += -DRIL_SUPPORTS_SEEK
COMMON_GLOBAL_CFLAGS += -DRIL_VARIANT_LEGACY
BOARD_RIL_CLASS := ../../../device/samsung/msm7627a-common/ril/
#TARGET_NEEDS_BIONIC_PRELINK_SUPPORT := true

# Hardware
#BOARD_HARDWARE_CLASS := device/samsung/msm7627a-common/cmhw

# Lights
BOARD_HAVE_GENERIC_BLN := true
TARGET_PROVIDES_LIBLIGHT := true

# SELinux
#BOARD_SEPOLICY_DIRS += device/samsung/msm7627a-common/sepolicy

## Vold
BOARD_VOLD_EMMC_SHARES_DEV_MAJOR := true
BOARD_VOLD_MAX_PARTITIONS := 24

## Samsung has weird framebuffer
TARGET_NO_INITLOGO := true

## UMS
TARGET_USE_CUSTOM_LUN_FILE_PATH := /sys/devices/platform/msm_hsusb/gadget/lun%d/file
BOARD_UMS_LUNFILE := "/sys/devices/platform/msm_hsusb/gadget/lun%d/file"

# Recovery
#TARGET_RECOVERY_LCD_BACKLIGHT_PATH := \"/sys/class/leds/lcd-backlight/brightness\"
TARGET_RECOVERY_SWIPE := true
TARGET_RECOVERY_INITRC := device/samsung/msm7x27a-common/recovery/init.rc
TARGET_RECOVERY_FSTAB := device/samsung/msm7627a-common/recovery/recovery.fstab
BOARD_CUSTOM_RECOVERY_KEYMAPPING := ../../device/samsung/msm7x27a-common/recovery/recovery_keys.c
TARGET_RECOVERY_PIXEL_FORMAT := RGBX_8888
#BOARD_HAS_SDCARD_INTERNAL := true
#BOARD_HAS_DOWNLOAD_MODE := true
#BOARD_USES_MMCUTILS := true
#BOARD_HAS_NO_MISC_PARTITION := true
BOARD_HAS_NO_SELECT_BUTTON := true
BOARD_RECOVERY_SWIPE := true
#BOARD_SUPPRESS_EMMC_WIPE := true
COMMON_GLOBAL_CFLAGS += -DNO_SECURE_DISCARD
DEVICE_RESOLUTION := 480x800
#BOARD_CUSTOM_GRAPHICS := ../../../device/samsung/msm7627a-common/recovery/graphics.c



# Wi-Fi CAF
BOARD_WPA_SUPPLICANT_DRIVER := NL80211
BOARD_HOSTAPD_DRIVER := NL80211
WPA_SUPPLICANT_VERSION := VER_0_8_X
BOARD_HAS_ATH_WLAN := true
BOARD_WLAN_DEVICE := ath6kl
WIFI_DRIVER_IFACE_NAME := "wlan"
WIFI_PARAMS += WIFI_MAC_FROM_NV="true"

WIFI_PARAMS += WIFI_DRIVER_IFACE_NAME="$(WIFI_DRIVER_IFACE_NAME)"
WIFI_PARAMS += WIFI_DRIVER_MODULE_PATH="$(WIFI_DRIVER_MODULE_PATH)"
WIFI_PARAMS += WIFI_DRIVER_MODULE_NAME="$(WIFI_DRIVER_MODULE_NAME)"

WIFI_PARAMS += AR6003_MAC_FILE="softmac"
WIFI_PARAMS += AR6003_REV2_BOARD_POWER_PA_FILE="../../data/misc/wifi/Cal_powerTuned_pa.bin"
WIFI_PARAMS += AR6003_HW_FW_DIR="ath6k/AR6003/hw2.1.1"
WIFI_PARAMS += AR6003_HW_BOARD_DATA_FILE="ath6k/AR6003/hw2.1.1/caldata.bin"
WIFI_PARAMS += AR6003_HW_DEFAULT_BOARD_DATA_FILE="ath6k/AR6003/hw2.1.1/bdata.SD31.bin"
BOARD_HOSTAPD_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
BOARD_WPA_SUPPLICANT_PRIVATE_LIB := lib_driver_cmd_$(BOARD_WLAN_DEVICE)
WIFI_DRIVER_FW_PATH_AP := "ap"
WIFI_DRIVER_FW_PATH_STA := "sta"
WIFI_DRIVER_FW_PATH_P2P := "p2p"
WIFI_DRIVER_MODULE_PATH := "/system/lib/modules/ath6kl_sdio.ko"
WIFI_DRIVER_MODULE_NAME := "ath6kl_sdio"
WIFI_EXT_MODULE_PATH := "/system/lib/modules/cfg80211.ko"
WIFI_EXT_MODULE_NAME := "cfg80211"
WIFI_DRIVER_FW_PATH_PARAM := "/data/misc/wifi/fwpath"
KERNEL_HAS_FINIT_MODULE := false
BOARD_GLOBAL_CFLAGS := -DNO_FINIT_MODULE

WITH_DEXPREOPT := true
   
#WITH_DEXPREOPT_PIC := true
#DONT_DEXPREOPT_PREBUILTS := true

# Final ZIP type
BLOCK_BASED_OTA := true
 
# Include an expanded selection of fonts
EXTENDED_FONT_FOOTPRINT := true

# Enable Minikin text layout engine (will be the default soon)
USE_MINIKIN := true


## FM Radio
BOARD_HAVE_QCOM_FM := true
COMMON_GLOBAL_CFLAGS += -DQCOM_FM_ENABLED


# BUILD FLAGS
#TARGET_USE_O3 := true
#OPT_MEMORY := true
STRICT_ALIASING := false
SUPPRESS_UNUSED_WARNING := true
