package com.ktimazstudio.enums

enum class AnimationSpeed(val multiplier: Float) {
    DISABLED(0f),
    SLOW(0.5f),
    NORMAL(1f),
    FAST(1.5f),
    INSTANT(3f)
}
