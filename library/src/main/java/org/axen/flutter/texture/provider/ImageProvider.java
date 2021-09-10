package org.axen.flutter.texture.provider;

import org.axen.flutter.texture.entity.NativeImage;

public interface ImageProvider<T> {
    T provide(NativeImage info) throws Exception;
}
