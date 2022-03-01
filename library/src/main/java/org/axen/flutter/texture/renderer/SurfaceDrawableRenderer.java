package org.axen.flutter.texture.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public class SurfaceDrawableRenderer extends SurfaceImageRenderer<Drawable> {
    private Drawable.Callback mCallback;

    public SurfaceDrawableRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<Drawable, NativeImage> provider
    ) {
        super(textureEntry, provider);
    }

    @Override
    protected Rect getImageSize(Drawable image, NativeImage info) {
        return new Rect(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
    }

    @Override
    public void draw(final Surface surface, final Drawable image, Handler handler) {
        // play drawable animation;
        // keep this reference or JVM might recycle it.
        mCallback = new Drawable.Callback() {
            @Override
            public void invalidateDrawable(@NonNull Drawable who) {
                handler.post(() -> {
                    if (surface.isValid()) {
                        drawImage(surface, who);
                    }
                });
            }

            @Override
            public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                handler.postAtTime(what, who, when);
            }

            @Override
            public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                handler.removeCallbacks(what, who);
            }
        };
        image.setCallback(mCallback);
        if (image instanceof Animatable) {
            ((Animatable) image).start();
        } else {
            image.invalidateSelf();
        }
    }

    private void drawImage(final Surface surface, final Drawable image) {
        Canvas canvas = surface.lockCanvas(null);
        // Fixed: PNG图片背景默认显示为白色的问题
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        image.draw(canvas);
        surface.unlockCanvasAndPost(canvas);
    }

    @Override
    public void release() {
        super.release();
        mCallback = null;
    }
}
