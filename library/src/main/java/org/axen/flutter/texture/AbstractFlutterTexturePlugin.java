package org.axen.flutter.texture;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.entity.ImageResult;
import org.axen.flutter.texture.renderer.ImageRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public abstract class AbstractFlutterTexturePlugin<T> implements FlutterPlugin, MethodChannel.MethodCallHandler {
    private Context context;
    private MethodChannel channel;
    private TextureRegistry textureRegistry;
    private LruCache<Integer, ImageResult> imageResultLruCache;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        imageResultLruCache = new LruCache<Integer, ImageResult>(16) {
            @Override
            protected void entryRemoved(
                    boolean evicted,
                    Integer key,
                    ImageResult oldValue,
                    ImageResult newValue
            ) {
                oldValue.release();
            }
        };
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
        if (textureId != null) {
            ImageResult imageResult = imageResultLruCache.get(textureId);
            if (imageResult != null)  {
                postSuccess(result, imageResult.toMap());
                return;
            }
        }
        TextureRegistry.SurfaceTextureEntry entry = textureRegistry.createSurfaceTexture();
        ImageRenderer<T> renderer = getImageRenderer(context, entry, info);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ImageResult imageResult = renderer.render(info);
                    imageResultLruCache.put((int) entry.id(), imageResult);
                    postSuccess(result, imageResult.toMap());
                } catch (Throwable e) {
                    postError(result, e.getMessage());
                }
            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        imageResultLruCache.evictAll();
    }

    protected void postSuccess(final MethodChannel.Result result, final Map<String, Object> map) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                result.success(map);
            }
        });
    }

    protected void postError(
            final MethodChannel.Result result,
            final String errorString
    ) {
        postError(result, "-1", errorString);
    }

    protected void postError(
            final MethodChannel.Result result,
            final String errorCode,
            final String errorString
    ) {
        postError(result, errorCode, errorString, "");
    }

    protected void postError(
            final MethodChannel.Result result,
            final String errorCode,
            final String errorString,
            final String errorDetail
    ) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                result.error(errorCode, errorString, errorDetail);
            }
        });
    }

    protected abstract ImageRenderer<T> getImageRenderer(
            Context context,
            TextureRegistry.SurfaceTextureEntry entry,
            T info
    );

    protected abstract String getChannel();

    protected abstract T getImageInfo(@NonNull MethodCall call);
}
