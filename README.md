# FlutterTexturePlugin

FlutterTexturePlugin是基于渲染器的外接纹理插件，使用不同的渲染器可各种外接纹理效果

# 基本使用

创建一个Flutter Plugin项目，然后将插件类的父类改为`FlutterTexturePlugin`类，实现父类的getImageRenderer方法和getChannel方法：

```java
public class ExamplePlugin extends FlutterTexturePlugin {
    @Override
    protected ImageRenderer getImageRenderer(Context context, TextureRegistry.SurfaceTextureEntry entry, SourceType sourceType) {
        return new SurfaceBitmapRenderer(entry, new GlideProvider(context));
    }

    @Override
    protected String getChannel() {
        return "org.axen.flutter/example";
    }
}
```

# 渲染器

渲染器实现了外接纹理的渲染逻辑，插件默认实现了一些渲染器：

| 渲染器 | 说明 |
| :-----: | :-----: |
| SurfaceBitmapRenderer | 使用Surface作为纹理载体，将一个Bitmap对象渲染到外接纹理上 |
| SurfaceDrawableRenderer | 使用Surface作为纹理载体，将一个Drawable对象渲染到外接纹理上 |

如果需要自定义渲染器，可参考[源码](library/src/main/java/org/axen/flutter/texture/renderer/SurfaceBitmapRenderer.java)继承`AbstractImageRenderer`类

# 内容提供者

渲染器的构造函数需要传入一个`ImageProvider`的实现类

该类的作用是根据提供的纹理信息，获取一个渲染目标(如Bitmap、Drawable)

利用一些第三方框架，如Glide、Picasso、Fresco等，可轻松获取渲染目标

```java
// 使用Glide获取Bitmap实例
public class GlideProvider implements ImageProvider<Bitmap> {
    
    private Context context;
    
    public GlideProvider(Context context) {
        this.context = context;
    }

    @Override
    public Bitmap provide(NativeImage info) throws Exception {
        assert info.getSourceType() == SourceType.NETWORK;
            
        return Glide.with(context)
                .load(info.getSource().toString())
                .asBitmap()
                .override(info.getWidth(), info.getHeight())
                .submit()
                .get();
    }
}
```

# 纹理信息

`NativeImage`是一个实体类，封装从Flutter层传入的基本纹理信息：

| 参数名 | 类型 | 说明 |
| :-----: | :-----: | :----- |
| source | Object | 渲染对象，可以是网络链接、Drawable资源ID、Asset资源名称和文件路径 |
| sourceType | SourceType | 渲染对象类型，NETWORK: 网络链接 DRAWABLE: Drawable资源 ASSET: Asset资源 FILE: 文件 |
| width | Double | 纹理宽度 |
| height | Double | 纹理高度 | 
| scaleRatio | Double | 纹理放大系数，默认是3.0 |
| fit | BoxFit | 纹理缩放模式，FILL: 填充 COVER: 覆盖 CONTAIN: 包含 FIT_WIDTH: 宽度自适应 FIT_HEIGHT: 高度自适应 SCALE_DOWN: 缩放 NONE: 无 |

从Flutter层传入纹理信息时，参数名和参数类型需要与文档描述一致

# 应用

- [FlutterFresco](https://github.com/axen1314/flutter_fresco) 基于Fresco实现图片加载的Flutter控件
- [FlutterGlide](https://github.com/axen1314/flutter_glide) 基于Glide实现图片加载的Flutter控件





