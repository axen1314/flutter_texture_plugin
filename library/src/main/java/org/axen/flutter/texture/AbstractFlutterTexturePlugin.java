package org.axen.flutter.texture;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.axen.flutter.texture.entity.ImageResult;
import org.axen.flutter.texture.renderer.ImageRenderer;
import org.axen.flutter.texture.utils.MD5Utils;
import org.axen.flutter.texture.utils.MarkSweepLruCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private MarkSweepLruCache<String, ImageResult> imageResultLruCache;
    private final Map<String, Queue<MethodChannel.Result>> pendingResults = new HashMap<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        imageResultLruCache = new MarkSweepLruCache<String, ImageResult>() {
            @Override
            protected void entryRemoved(
                    String key,
                    ImageResult value
            ) {
                value.release();
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
        } else if (call.method.equals("release")) {
            release(call, result);
        }else {
            result.notImplemented();
        }
    }

    private void load(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        String source = call.argument("source");
        if (source == null || source.isEmpty()) {
            postError(result, "Source is null or empty!");
        } else {
            final String md5 = MD5Utils.stringToMD5(source);
            // Be careful to use it which will increase value's hit counts.
            ImageResult imageResult = imageResultLruCache.get(md5);
            if (imageResult != null) {
                postSuccess(result, imageResult.toMap());
            } else {
                Queue<MethodChannel.Result> resultList = pendingResults.get(md5);
                if (resultList == null || resultList.isEmpty()) {
                    if (resultList == null) {
                        resultList = new LinkedList<>();
                        pendingResults.put(md5, resultList);
                    }
                    resultList.add(result);
                    final T info = getImageInfo(call);
                    TextureRegistry.SurfaceTextureEntry entry = textureRegistry.createSurfaceTexture();
                    ImageRenderer<T> renderer = getImageRenderer(context, entry, info);
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ImageResult imageResult = renderer.render(info, handler);
                                imageResultLruCache.put(md5, imageResult);
                                Map<String, Object> imageResultMap = imageResult.toMap();
                                Queue<MethodChannel.Result> pending = pendingResults.get(md5);
                                while (pending != null && !pending.isEmpty()) {
                                    imageResultLruCache.get(md5);// increase hit counts
                                    MethodChannel.Result rs = pending.poll();
                                    postSuccess(rs, imageResultMap);
                                }
                            } catch (Throwable e) {
                                Queue<MethodChannel.Result> pending = pendingResults.get(md5);
                                while (pending != null && !pending.isEmpty()) {
                                    MethodChannel.Result rs = pending.poll();
                                    postError(rs, e.getMessage());
                                }
                            }
                        }
                    });
                } else {
                    resultList.add(result);
                }
            }

        }
    }

    private void release(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        String source = call.argument("source");
        if (source == null || source.isEmpty()) {
            postError(result, "Source is null or empty!");
        } else {
            final String md5 = MD5Utils.stringToMD5(source);
            imageResultLruCache.remove(md5);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
        imageResultLruCache.evictAll();
        channel.setMethodCallHandler(null);
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
