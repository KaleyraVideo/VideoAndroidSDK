# Kaleyra Video Android SDK

<p align="center">
<img src="img/kaleyra.png" alt="Kaleyra" title="Kaleyra" />
</p>

[![Download](https://badgen.net/maven/v/metadata-url/https/maven.bandyer.com/releases/com/kaleyra/video-sdk/maven-metadata.xml?label=maven.bandyer.com/releases) ](https://maven.bandyer.com/index.html#releases/com/kaleyra/video-sdk/)[![Android CI](https://github.com/KaleyraVideo/VideoAndroidSDK/actions/workflows/android.yml/badge.svg?branch=develop)](https://github.com/KaleyraVideo/VideoAndroidSDK/actions/workflows/android.yml)

**Kaleyra video** enables audio/video communication and collaboration from any platform and browser! Through its WebRTC architecture, it makes video communication simple and punctual.

---

. **[Overview](#overview)** .
**[Requirements](#requirements)** .
**[Features](#features)** .

---

## Overview

**Kaleyra Video Android SDK** makes it easy to add video conference and chat communication to mobile apps.

**Even though this sdk encloses strongly the UI/UX, it is fully styleable through default Android style system.**

<img src="img/img0.png" height="360"/>

## Requirements

**Gradle 8.+**

**JDK 17**

**Kotlin v1.9.+**

**Java 1.8**

**Target API 33**

**Supported API level 21+ (Android 5.0 Lollipop).**


```java
android {

   defaultConfig {
        minSdkVersion 21
        targetSdkVersion 33
        [...]
   }
   
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    composeOptions {
        kotlinCompilerExtensionVersion 1.5.3  // or newer
    }
    
    [...]
}

```

## Features

- Audio call
- Audio call upgradable to video call
- Video call
- Chat
- Collaborative whiteboard 
- File sharing in call
- Screen sharing in call:
	- App only
	- System wide
- Call Recording
- Virtual Background
- External Camera
