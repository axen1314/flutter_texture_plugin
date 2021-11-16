package org.axen.flutter.texture.renderer;

import org.axen.flutter.texture.entity.ImageResult;

public interface ImageRenderer<T> {
    ImageResult render(T info) throws Throwable;
}
