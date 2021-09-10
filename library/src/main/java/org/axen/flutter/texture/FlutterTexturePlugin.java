package org.axen.flutter.texture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.constant.BoxFit;
import org.axen.flutter.texture.constant.SourceType;
import org.axen.flutter.texture.entity.NativeImage;
import org.axen.flutter.texture.renderer.ImageRenderer;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public abstract class FlutterTexturePlugin implements FlutterPlugin, MethodChannel.MethodCallHandler {

    private Context context;
    private MethodChannel channel;
    private TextureRegistry textureRegistry;
    private Map<Integer, ImageRenderer> rendererMap;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        rendererMap = new HashMap<>();
        context = binding.getApplicationContext();
        textureRegistry = binding.getTextureRegistry();
        channel = new MethodChannel(binding.getBinaryMessenger(), getChannel());
        channel.setMethodCallHandler(this);
    }

    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if (call.method.equals("load")) {
            load(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void load(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        NativeImage info = NativeImage.obtain();
        info.setSource(call.argument("resource"));
        Integer resourceType = call.argument("resourceType");
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

        Integer textureId = call.argument("textureId");
        // TODO 支持OPENGL ES渲染
        ImageRenderer renderer;
        if (textureId == null || !rendererMap.containsKey(textureId)) {
            TextureRegistry.SurfaceTextureEntry entry = textureRegistry.createSurfaceTexture();
            renderer = getImageRenderer(context, entry, info.getSourceType());
            rendererMap.put((int) entry.id(), renderer);
        } else {
            renderer = rendererMap.get(textureId);
        }
        if (renderer != null) renderer.render(info, result);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        for(Map.Entry<Integer, ImageRenderer> entry : rendererMap.entrySet())
            entry.getValue().release();
        rendererMap.clear();
    }

    protected abstract ImageRenderer getImageRenderer(
            Context context,
            TextureRegistry.SurfaceTextureEntry entry,
            SourceType sourceType
    );

    protected abstract String getChannel();
}