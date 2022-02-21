package org.axen.flutter.texture.entity;

import org.axen.flutter.texture.renderer.ImageRenderer;

import java.util.HashMap;
import java.util.Map;

import io.flutter.view.TextureRegistry;

public class ImageResult {
    private final int width;
    private final int height;
    private final ImageRenderer<?> renderer;

    public ImageResult(
            int width,
            int height,
            ImageRenderer<?> renderer
    ) {
        this.renderer = renderer;
        this.width = width;
        this.height = height;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("textureId", this.renderer.id());
        map.put("width", this.width);
        map.put("height", this.height);
        return map;
    }

    public void release() {
        this.renderer.release();
    }
}
