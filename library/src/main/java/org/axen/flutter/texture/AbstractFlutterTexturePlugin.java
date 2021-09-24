package org.axen.flutter.texture;

import android.content.Context;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.renderer.ImageRenderer;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public abstract class AbstractFlutterTexturePlugin<T> implements FlutterPlugin, MethodChannel.MethodCallHandler {
    private Context context;
    private MethodChannel channel;
    private TextureRegistry textureRegistry;
    private Map<Integer, ImageRenderer<T>> rendererMap;

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
        T info = getImageInfo(call);
        Integer textureId = call.argument("textureId");
        // TODO 支持OPENGL ES渲染
        ImageRenderer<T> renderer;
        if (textureId == null || !rendererMap.containsKey(textureId)) {
            TextureRegistry.SurfaceTextureEntry entry = textureRegistry.createSurfaceTexture();
            renderer = getImageRenderer(context, entry, info);
            rendererMap.put((int) entry.id(), renderer);
        } else {
            renderer = rendererMap.get(textureId);
        }
        if (renderer != null) renderer.render(info, result);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        for(Map.Entry<Integer, ImageRenderer<T>> entry : rendererMap.entrySet())
            entry.getValue().release();
        rendererMap.clear();
    }

    protected abstract ImageRenderer<T> getImageRenderer(
            Context context,
            TextureRegistry.SurfaceTextureEntry entry,
            T info
    );

    protected abstract String getChannel();

    protected abstract T getImageInfo(@NonNull MethodCall call);
}
