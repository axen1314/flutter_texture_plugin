package org.axen.flutter.texture.uri;

import android.net.Uri;

public class NetworkURIParser implements URIParser {
    @Override
    public Uri parse(Object source) {
        return Uri.parse(source.toString());
    }
}
