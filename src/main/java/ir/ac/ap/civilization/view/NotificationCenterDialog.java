package ir.ac.ap.civilization.view;

import ir.ac.ap.civilization.model.phase2.PhaseTwoState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class NotificationCenterDialog extends JDialog {
    public NotificationCenterDialog(JFrame owner, PhaseTwoState state) {
        super(owner, "Notification Center", true);
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String message : state.getNotifications()) model.addElement(message);
        JList<String> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> source, Object value, int index,
                    boolean selected, boolean focused) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        source, value, index, selected, focused);
                label.setBorder(new EmptyBorder(8, 8, 8, 8));
                label.setText("Turn event: " + value);
                return label;
            }
        });
        JButton clear = new JButton("Clear Notifications");
        clear.setForeground(Color.BLACK);
        clear.addActionListener(event -> {
            state.clearNotifications();
            model.clear();
        });
        setLayout(new BorderLayout(8, 8));
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(clear, BorderLayout.SOUTH);
        setSize(600, 450);
        setLocationRelativeTo(owner);
    }
}
