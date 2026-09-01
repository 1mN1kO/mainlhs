package com.mainlhs.common.scale;

public final class ScaleMath {

    public static final float DEFAULT_HEIGHT = 1.8f;

    private ScaleMath() {}

    /**
     * Size 1 = default height (1.8 blocks).
     * Every additional 1.0 in size adds 1 block of height.
     */
    public static float heightFromSizeParam(float sizeParam) {
        if (Math.abs(sizeParam - 1.0f) < 0.0001f) {
            return DEFAULT_HEIGHT;
        }
        return DEFAULT_HEIGHT + (sizeParam - 1.0f);
    }

    public static float getScaleFactor(float sizeParam) {
        return heightFromSizeParam(sizeParam) / DEFAULT_HEIGHT;
    }
}
