package org.axen.flutter.texture.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Surface;

import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import io.flutter.view.TextureRegistry;

public class SurfaceDrawableRenderer extends SurfaceImageRenderer<Drawable>{

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
    public void draw(final Surface surface, final Drawable image, Rect srcRect) {
        Canvas canvas = surface.lockCanvas(null);
        // Fixed: PNG图片背景默认显示为白色的问题
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        image.draw(canvas);
        surface.unlockCanvasAndPost(canvas);
    }

    @Override
    protected Rect calculateImageSrcRect(Rect imageSize, NativeImage info, Rect dstRect) {
        // 不用计算绘制区域
        return null;
    }
}
