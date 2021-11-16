package org.axen.flutter.texture.provider;

import org.axen.flutter.texture.entity.NativeImage;

public interface ImageProvider<T, R> {
    T provide(R info) throws Throwable;
}
