package org.axen.flutter.texture.renderer;

import static java.lang.reflect.Modifier.PROTECTED;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.VisibleForTesting;

import org.axen.flutter.texture.constant.BoxFit;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public abstract class SurfaceImageRenderer<T> extends NativeImageRenderer<T> {

    public SurfaceImageRenderer(
            TextureRegistry.SurfaceTextureEntry textureEntry,
            ImageProvider<T, NativeImage> provider
    ) {
        super(textureEntry, provider);
    }

    private Surface surface;

    @Override
    public void release() {
        textureEntry.release();
        if (surface != null) surface.release();
    }

    /**
     * 计算图片需要绘制的区域
     * 此方法与{@link #calculateImageDstRect(Rect, NativeImage)}}结合实现根据缩放模式进行图片缩放
     */
    protected Rect calculateImageSrcRect(Rect imageSize, NativeImage info, Rect dstRect) {
        BoxFit fit = info.getFit();
        int originWidth = info.getWidth(), originHeight = info.getHeight();
        int bitmapWidth = imageSize.width(), bitmapHeight = imageSize.height();
        if ((originWidth >= bitmapWidth && originHeight >= bitmapHeight)
                || fit == BoxFit.FILL
                || fit == BoxFit.CONTAIN
                || fit == BoxFit.SCALE_DOWN) {
            return null;
        }

        Rect srcRect;
        int dstWidth = dstRect.width();
        int dstHeight = dstRect.height();
        double wPixelRatio = bitmapWidth * 1.0 / dstWidth;
        double hPixelRatio = bitmapHeight * 1.0 / dstHeight;
        if (fit == BoxFit.FIT_WIDTH || (fit == BoxFit.COVER && wPixelRatio <= hPixelRatio)) {
            int bitmapClipHeight = (int) (dstHeight * wPixelRatio);
            int top = (int) ((bitmapHeight - bitmapClipHeight) * 0.5);
            srcRect = new Rect(0, top,  bitmapWidth, top + bitmapClipHeight);
        } else if (fit == BoxFit.FIT_HEIGHT || (fit == BoxFit.COVER && wPixelRatio > hPixelRatio)) {
            int bitmapClipWidth = (int) (dstWidth * hPixelRatio);
            int left = (int) ((bitmapWidth - bitmapClipWidth) * 0.5);
            srcRect = new Rect(left, 0,  left + bitmapClipWidth, bitmapHeight);
        } else {
            int left = (int) ((bitmapWidth - dstWidth) * 0.5);
            int top = (int) ((bitmapHeight - dstHeight) * 0.5);
            srcRect = new Rect(left, top, left + dstWidth, top + dstHeight);
        }
        return srcRect;
    }

    /**
     * 计算图片控件的大小
     * 此方法与{@link #calculateImageSrcRect(Rect, NativeImage, Rect)}结合实现根据缩放模式进行图片缩放
     */
    protected Rect calculateImageDstRect(Rect imageSize, NativeImage info) {
        int originWidth = info.getWidth(), originHeight = info.getHeight();
        int bitmapWidth = imageSize.width(), bitmapHeight = imageSize.height();
        if (originHeight >= bitmapHeight && originWidth >= bitmapWidth)
            return new Rect(0, 0, bitmapWidth, bitmapHeight);
        BoxFit fit = info.getFit();
        if (fit == BoxFit.SCALE_DOWN || fit == BoxFit.CONTAIN) {
            double wRatio = originWidth * 1.0 / bitmapWidth;
            double hRatio = originHeight * 1.0 / bitmapHeight;
            double scaleRatio = Math.min(wRatio, hRatio);
            int width = (int) (bitmapWidth * scaleRatio);
            int height = (int) (bitmapHeight * scaleRatio);
            return new Rect(0, 0, width, height);
        }
        return new Rect(0, 0, originWidth, originHeight);
    }

    @Override
    protected void onDraw(T image, NativeImage info, MethodChannel.Result result) {
        SurfaceTexture texture = textureEntry.surfaceTexture();
        if (surface == null) surface = new Surface(texture);
        if (surface.isValid()) {
            Rect imageSize = getImageSize(image, info);
            texture.setDefaultBufferSize(imageSize.width(), imageSize.height());
            Rect dstRect = calculateImageDstRect(imageSize, info);
            Rect srcRect = calculateImageSrcRect(imageSize, info, dstRect);
            draw(surface, image, srcRect);
            Map<String, Object> map = new HashMap<>();
            map.put("textureId", textureEntry.id());
            map.put("width", dstRect.width());
            map.put("height", dstRect.height());
            postSuccess(result, map);
        } else {
            postError(result, "Surface is invalid!");
        }
    }

    protected abstract Rect getImageSize(T image, NativeImage info);

    @VisibleForTesting(otherwise = PROTECTED)
    public abstract void draw(Surface surface, T image, Rect srcRect);
}
