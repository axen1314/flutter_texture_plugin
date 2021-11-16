package org.axen.flutter.texture.renderer;

import org.axen.flutter.texture.entity.ImageResult;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public abstract class AbstractImageRenderer<T, R> implements ImageRenderer<R> {
    protected TextureRegistry.SurfaceTextureEntry textureEntry;
    private final ImageProvider<T, R> provider;

    public AbstractImageRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<T, R> provider
    ) {
        this.textureEntry = textureEntry;
        this.provider = provider;
    }

    @Override
    public ImageResult render(final R info) throws Throwable {
        T image = provider.provide(info);
        return onDraw(image, info);
    }

    protected abstract ImageResult onDraw(T image, R info) throws Throwable;

}
