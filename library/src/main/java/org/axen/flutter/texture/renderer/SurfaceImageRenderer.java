package org.axen.flutter.texture.renderer;

import static java.lang.reflect.Modifier.PROTECTED;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.VisibleForTesting;

import org.axen.flutter.texture.entity.ImageResult;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public abstract class SurfaceImageRenderer<T> extends AbstractImageRenderer<T, NativeImage> {

    public SurfaceImageRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<T, NativeImage> provider
    ) {
        super(textureEntry, provider);
    }

    @Override
    protected ImageResult onDraw(T image, NativeImage info) throws Throwable {
        SurfaceTexture texture = textureEntry.surfaceTexture();
        Surface surface = new Surface(texture);
        try {
            if (!surface.isValid()) {
                throw new IllegalArgumentException("Surface is invalid!");
            }
            Rect imageSize = getImageSize(image, info);
            texture.setDefaultBufferSize(imageSize.width(), imageSize.height());
            draw(surface, image);
            return new ImageResult(imageSize.width(), imageSize.height(), textureEntry);
        } finally {
            surface.release();
        }
    }

    protected abstract Rect getImageSize(T image, NativeImage info);

    @VisibleForTesting(otherwise = PROTECTED)
    public abstract void draw(Surface surface, T image);
}
