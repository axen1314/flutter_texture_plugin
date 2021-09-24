package org.axen.flutter.texture.renderer;

import static java.lang.reflect.Modifier.PROTECTED;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.VisibleForTesting;

import org.axen.flutter.texture.constant.BoxFit;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.provider.ImageProvider;
import org.axen.flutter.texture.utils.NativeImageUtils;

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
        return null;
    }

    /**
     * 计算图片控件的大小
     * 此方法与{@link #calculateImageSrcRect(Rect, NativeImage, Rect)}结合实现根据缩放模式进行图片缩放
     */
    protected Rect calculateImageDstRect(Rect imageSize, NativeImage info) {
        return NativeImageUtils.calculateImageDstRect(imageSize, info);
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
