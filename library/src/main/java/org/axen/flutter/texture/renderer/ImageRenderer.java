package org.axen.flutter.texture.renderer;

import org.axen.flutter.texture.entity.NativeImage;

import io.flutter.plugin.common.MethodChannel;

public interface ImageRenderer<T> {
    void render(T info, MethodChannel.Result result);
    void release();
}
