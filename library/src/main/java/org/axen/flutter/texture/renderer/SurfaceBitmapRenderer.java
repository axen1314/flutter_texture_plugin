package org.axen.flutter.texture.renderer;

import static java.lang.reflect.Modifier.PROTECTED;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Surface;

import androidx.annotation.VisibleForTesting;

import org.axen.flutter.texture.constant.BoxFit;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public class SurfaceBitmapRenderer extends SurfaceImageRenderer<Bitmap> {

    public SurfaceBitmapRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry, 
            ImageProvider<Bitmap, NativeImage> provider
    ) {
        super(textureEntry, provider);
    }

    @Override
    protected Rect getImageSize(Bitmap image, NativeImage info) {
        return new Rect(0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    @VisibleForTesting(otherwise = PROTECTED)
    public void draw(Surface surface, Bitmap image, Rect srcRect) {
        Rect dstRect = new Rect(0, 0, image.getWidth(), image.getHeight());
        Canvas canvas = surface.lockCanvas(null);
        // Fixed: PNG图片背景默认显示为白色的问题
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(image, srcRect, dstRect, null); //图片的绘制
        surface.unlockCanvasAndPost(canvas);
    }
}
