`mkdir .repo/local_manifests`

`gedit .repo/local_manifests/msm7627a.xml `

<b>PASTE THIS INSIDE THAT FILE :- </b>


```xml
<?xml version="1.0" encoding="UTF-8"?>
<manifest>

<!-- Device trees -->
<project path="device/samsung/msm7627a-common" name="marxteen/android_device_msm7627a-common" revision="cm-13.0" />
<project path="device/samsung/delos3geur" name="marxteen/android_device_delos3geur" revision="13.0" />

<!-- vendor -->
<project path="vendor/samsung/msm7627a-common" name="marxteen/android_vendor_msm7627a-common" revision="cm-13.0" />

<!--kernel-->
<project path="kernel/samsung/delos3geur" name="marxteen/android_kernel_delos3geur-1" revision="cm-13.0" />

<!--Hardware-->
<project path="hardware/qcom/display-caf/msm7x27a" name="marxteen/android_kernel_delos3geur-1" revision="cm-13.0" />
<project path="hardware/qcom/media-caf/msm7x27a" name="marxteen/hardware_qcom_media" revision="lp5.1" />
<project path="hardware/atheros/wlan" name="marxteen/android_hardware_atheros_wlan" remote="github" revision="cm-13.0" />
<project path="hardware/ril-legacy" name="marxteen/android_hardware_ril-legacy" revision="cm-13.0" />

<!--Graphics-->
<project path="external/stlport" name="CyanogenMod/android_external_stlport" revision="cm-13.0" />

<!--Bluetooth-->
<project path="external/bluetooth" name="marxteen/external_bluetooth" revision="cm-13.0" />
<project path="external/dbus" name="marxteen/external_dbus" revision="cm-13.0" />
<project path="system/bluetooth" name="marxteen/system_bluetooth" revision="cm-13.0" />

</manifest>
```

`. build/envsetup.sh `

`brunch code name `

<b>Good Luck!</b>

