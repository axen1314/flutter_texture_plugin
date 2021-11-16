package org.axen.flutter.texture.entity;

import java.util.HashMap;
import java.util.Map;

import io.flutter.view.TextureRegistry;

public class ImageResult {
    private final int width;
    private final int height;
    private final TextureRegistry.SurfaceTextureEntry textureEntry;

    public ImageResult(
            int width,
            int height,
            TextureRegistry.SurfaceTextureEntry textureEntry
    ) {
        this.textureEntry = textureEntry;
        this.width = width;
        this.height = height;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("textureId", this.textureEntry.id());
        map.put("width", this.width);
        map.put("height", this.height);
        return map;
    }

    public void release() {
        this.textureEntry.release();
    }
}
