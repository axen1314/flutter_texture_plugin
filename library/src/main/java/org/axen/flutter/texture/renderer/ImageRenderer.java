package org.axen.flutter.texture.renderer;

import android.os.Handler;

import org.axen.flutter.texture.entity.ImageResult;

public interface ImageRenderer<T> {
    ImageResult render(T info, Handler handler) throws Throwable;
    void release();
    long id();
}
