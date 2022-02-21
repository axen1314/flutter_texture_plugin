package org.axen.flutter.texture.renderer;

import static java.lang.reflect.Modifier.PROTECTED;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Handler;
import android.view.Surface;

import androidx.annotation.VisibleForTesting;

import org.axen.flutter.texture.entity.ImageResult;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public abstract class SurfaceImageRenderer<T> extends AbstractImageRenderer<T, NativeImage> {
    private final Surface mSurface;
    private final SurfaceTexture mTexture;

    public SurfaceImageRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<T, NativeImage> provider
    ) {
        super(textureEntry, provider);
        mTexture = textureEntry.surfaceTexture();
        mSurface = new Surface(mTexture);
    }

    @Override
    protected ImageResult onDraw(T image, NativeImage info, Handler handler) throws Throwable {
        if (!mSurface.isValid()) {
            throw new IllegalArgumentException("Surface is invalid!");
        }
        Rect imageSize = getImageSize(image, info);
        mTexture.setDefaultBufferSize(imageSize.width(), imageSize.height());
        draw(mSurface, image, handler);
        return new ImageResult(imageSize.width(), imageSize.height(), this);
    }

    protected abstract Rect getImageSize(T image, NativeImage info);

    @VisibleForTesting(otherwise = PROTECTED)
    public abstract void draw(Surface surface, T image, android.os.Handler handler);

    @Override
    public void release() {
        super.release();
        mSurface.release();
    }
}
