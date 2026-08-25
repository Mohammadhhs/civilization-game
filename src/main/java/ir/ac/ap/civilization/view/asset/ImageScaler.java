package ir.ac.ap.civilization.view.asset;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class ImageScaler {

    private static final Map<BufferedImage, Map<Long, BufferedImage>> CACHE =
            new WeakHashMap<>();

    private ImageScaler() {
    }

    public static synchronized BufferedImage fit(
            BufferedImage source,
            int maximumWidth,
            int maximumHeight
    ) {
        if (source == null || maximumWidth <= 0 || maximumHeight <= 0) {
            return null;
        }

        long key = (((long) maximumWidth) << 32) |
                (maximumHeight & 0xffffffffL);
        Map<Long, BufferedImage> sizes = CACHE.get(source);
        if (sizes != null && sizes.containsKey(key)) {
            return sizes.get(key);
        }

        double scale = Math.min(
                maximumWidth / (double) source.getWidth(),
                maximumHeight / (double) source.getHeight()
        );
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage result = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        if (sizes == null) {
            sizes = new HashMap<>();
            CACHE.put(source, sizes);
        }
        sizes.put(key, result);
        return result;
    }
}
