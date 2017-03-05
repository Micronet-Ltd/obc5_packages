/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.nfc.beam;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import android.text.TextUtils;
public final class MimeTypeUtil {

    private static final String TAG = "MimeTypeUtil";

    private MimeTypeUtil() {}
    private static String getFileExtensionFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }
            int filenamePos = url.lastIndexOf('/');
            String filename =
                0 <= filenamePos ? url.substring(filenamePos + 1) : url;
            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }
        return "";
    }
    public static String getMimeTypeForUri(Context context, Uri uri) {
        if (uri.getScheme() == null) return null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            return cr.getType(uri);
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            String extension = getFileExtensionFromUrl(uri.getPath()).toLowerCase();
           if (extension != null) {
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            } else {
                return null;
            }
        } else {
            Log.d(TAG, "Could not determine mime type for Uri " + uri);
            return null;
        }
    }
}
