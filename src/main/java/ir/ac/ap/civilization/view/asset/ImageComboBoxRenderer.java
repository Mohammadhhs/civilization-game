package ir.ac.ap.civilization.view.asset;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ImageComboBoxRenderer<T>
        extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    private final Function<T, BufferedImage> imageProvider;
    private final Map<T, ImageIcon> iconCache;

    public ImageComboBoxRenderer(
            Function<T, BufferedImage> imageProvider
    ) {
        this.imageProvider = imageProvider;
        this.iconCache = new HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus
        );

        if (value == null) {
            setIcon(null);
            return this;
        }

        T typedValue = (T) value;
        if (!iconCache.containsKey(typedValue)) {
            BufferedImage scaled = ImageScaler.fit(
                    imageProvider.apply(typedValue), 30, 30
            );
            iconCache.put(
                    typedValue,
                    scaled == null ? null : new ImageIcon(scaled)
            );
        }

        setIcon(iconCache.get(typedValue));
        setIconTextGap(8);
        return this;
    }
}
