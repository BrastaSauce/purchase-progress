/*
 * Copyright (c) 2022, BrastaSauce
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.brastasauce.purchaseprogress;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PurchaseProgressResultPanel extends JPanel
{
    private static final Dimension IMAGE_SIZE = new Dimension(32, 32);

    PurchaseProgressResultPanel(PurchaseProgressPlugin plugin, PurchaseProgressItem item)
    {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
        setToolTipText(item.getName());
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        Color background = getBackground();
        List<JPanel> panels = new ArrayList<>();
        panels.add(this);

        MouseAdapter itemPanelMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                plugin.addItem(item);
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                for (JPanel panel : panels)
                {
                    panel.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
                }
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                for (JPanel panel : panels)
                {
                    panel.setBackground(background);
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };

        addMouseListener(itemPanelMouseListener);
        setBorder(new EmptyBorder(5, 5, 5, 0));

        // Image
        JLabel itemImage = new JLabel();
        itemImage.setPreferredSize(IMAGE_SIZE);
        if (item.getImage() != null)
        {
            item.getImage().addTo(itemImage);
        }
        add(itemImage, BorderLayout.LINE_START);

        // Item Details Panel
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        panels.add(rightPanel);
        rightPanel.setBackground(background);

        // Item Name
        JLabel itemName = new JLabel();
        itemName.setForeground(Color.WHITE);
        itemName.setMaximumSize(new Dimension(0, 0));
        itemName.setPreferredSize(new Dimension(0, 0));
        itemName.setText(item.getName());
        rightPanel.add(itemName);

        // GE Price
        JLabel gePriceLabel = new JLabel();
        if (item.getGePrice() > 0)
        {
            gePriceLabel.setText(QuantityFormatter.formatNumber(item.getGePrice()) + " gp");
        }
        else
        {
            gePriceLabel.setText("N/A");
        }
        gePriceLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
        rightPanel.add(gePriceLabel);

        add(rightPanel, BorderLayout.CENTER);
    }
}
