package org.axen.flutter.texture.uri;

import android.content.Context;
import android.net.Uri;

public class DrawableURIParser implements URIParser {
    private Context context;

    public DrawableURIParser(Context context) {
        this.context = context;
    }

    @Override
    public Uri parse(Object source) {
        String[] segments = source.toString().split("\\.");
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(segments[2], segments[1], packageName);
        return Uri.parse("android.resource://" + packageName + "/" + resId);
    }
}
