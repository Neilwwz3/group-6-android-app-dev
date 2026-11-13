package hk.hkucs.group_6_android_app_dev.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Loads sample posters from assets asynchronously with a small in-memory cache. */
public final class PosterLoader {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(
            (int) (Runtime.getRuntime().maxMemory() / 8)) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    private PosterLoader() {
    }

    /**
     * Shows the poster image when {@code file} resolves to an asset, otherwise keeps
     * {@code fallback} (the letter placeholder) visible. Never decodes on the main thread.
     */
    public static void load(@NonNull Context context, @Nullable String file,
            @NonNull ImageView image, @NonNull View fallback) {
        image.setTag(file);
        if (file == null || file.isEmpty()) {
            image.setVisibility(View.GONE);
            fallback.setVisibility(View.VISIBLE);
            return;
        }
        Bitmap cached = CACHE.get(file);
        if (cached != null) {
            show(image, fallback, cached);
            return;
        }
        image.setVisibility(View.GONE);
        fallback.setVisibility(View.VISIBLE);
        String asset = file.startsWith("sample/") ? file : "sample/posters/" + file;
        IO.execute(() -> {
            Bitmap bmp = decode(context.getApplicationContext(), asset);
            MAIN.post(() -> {
                if (bmp != null) {
                    CACHE.put(file, bmp);
                }
                if (file.equals(image.getTag()) && bmp != null) {
                    show(image, fallback, bmp);
                }
            });
        });
    }

    private static void show(ImageView image, View fallback, Bitmap bmp) {
        image.setImageBitmap(bmp);
        image.setVisibility(View.VISIBLE);
        fallback.setVisibility(View.GONE);
    }

    @Nullable
    private static Bitmap decode(Context context, String asset) {
        try (InputStream in = context.getAssets().open(asset)) {
            return BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            return null;
        }
    }
}
