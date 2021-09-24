package org.axen.flutter.texture.renderer;

import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public abstract class NativeImageRenderer<T> extends AbstractImageRenderer<T, NativeImage>{

    public NativeImageRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<T, NativeImage> provider
    ) {
        super(textureEntry, provider);
    }

    @Override
    protected void onComplete(NativeImage info) {
        info.recycle();
    }
}
