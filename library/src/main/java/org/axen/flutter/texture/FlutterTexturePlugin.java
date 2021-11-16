package org.axen.flutter.texture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.constant.BoxFit;
import org.axen.flutter.texture.constant.SourceType;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.renderer.ImageRenderer;

import io.flutter.plugin.common.MethodCall;
import io.flutter.view.TextureRegistry;

public abstract class FlutterTexturePlugin extends AbstractFlutterTexturePlugin<NativeImage> {

    @Override
    protected NativeImage getImageInfo(@NonNull MethodCall call) {
        NativeImage info = NativeImage.obtain();
        info.setSource(call.argument("source"));
        Integer resourceType = call.argument("sourceType");
        if (resourceType != null)
            info.setSourceType(SourceType.values()[resourceType]);
        Double scaleRatio = call.argument("scaleRatio");
        if (scaleRatio != null) info.setScaleRatio(scaleRatio);
        Double width = call.argument("width");
        if (width != null) info.setWidth(width.intValue());
        Double height = call.argument("height");
        if (height != null) info.setHeight(height.intValue());
        Integer fit = call.argument("fit");
        if (fit != null) info.setFit(BoxFit.values()[fit]);
        return info;
    }

    @Override
    protected ImageRenderer<NativeImage> getImageRenderer(
            Context context,
            TextureRegistry.SurfaceTextureEntry entry,
            NativeImage info
    ) {
        return getImageRenderer(context, entry, info.getSourceType());
    }

    protected abstract ImageRenderer<NativeImage> getImageRenderer(
            Context context,
            TextureRegistry.SurfaceTextureEntry entry,
            SourceType sourceType
    );
}