package org.axen.flutter.texture.renderer;

import org.axen.flutter.texture.entity.NativeImage;

import io.flutter.plugin.common.MethodChannel;

public interface ImageRenderer {
    void render(NativeImage info, MethodChannel.Result result);
    void release();
}
