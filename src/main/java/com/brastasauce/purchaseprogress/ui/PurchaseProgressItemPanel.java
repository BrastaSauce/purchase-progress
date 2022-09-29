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
package com.brastasauce.purchaseprogress.ui;

import com.brastasauce.purchaseprogress.data.PurchaseProgressItem;
import com.brastasauce.purchaseprogress.PurchaseProgressPlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PurchaseProgressItemPanel extends JPanel
{
    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this progress item?";
    private static final ImageIcon DELETE_ICON;
    private static final ImageIcon DELETE_HOVER_ICON;
    private static final Dimension IMAGE_SIZE = new Dimension(32, 32);

    private float percent;

    static
    {
        final BufferedImage deleteImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/delete_icon.png");
        DELETE_ICON = new ImageIcon(deleteImage);
        DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImage, 0.53f));
    }

    PurchaseProgressItemPanel(PurchaseProgressPlugin plugin, PurchaseProgressItem item)
    {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
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
        JPanel rightPanel = new JPanel(new GridLayout(3, 1));
        rightPanel.setBackground(new Color(0, 0, 0, 0));

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

        // Purchase Progress
        JLabel progressLabel = new JLabel();
        percent = ((float) plugin.getValue() / item.getGePrice()) * 100;
        if (percent >= 100)
        {
            percent = 100;
        }
        progressLabel.setText(String.format("%.0f", percent) + "%");
        rightPanel.add(progressLabel);

        // Remove Button
        JPanel deletePanel = new JPanel(new BorderLayout());
        deletePanel.setBackground(new Color(0, 0, 0, 0));

        JLabel deleteItem = new JLabel(DELETE_ICON);
        deleteItem.setBorder(new EmptyBorder(0, 15, 0, 0));
        deleteItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (deleteConfirm())
                {
                    plugin.removeItem(item);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                deleteItem.setIcon(DELETE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                deleteItem.setIcon(DELETE_ICON);
            }
        });
        deletePanel.add(deleteItem, BorderLayout.NORTH);
        deletePanel.setOpaque(false);

        add(rightPanel, BorderLayout.CENTER);
        add(deletePanel, BorderLayout.EAST);
    }

    private boolean deleteConfirm()
    {
        int confirm = JOptionPane.showConfirmDialog(this,
                        DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor(new Color(12, 85, 35));
        float greenPercent = this.getWidth() * percent / 100;
        int greenWidth = (int) greenPercent;
        g.fillRect(0, 0, greenWidth, this.getHeight());

        if (greenWidth != this.getWidth())
        {
            g.setColor(ColorScheme.DARKER_GRAY_COLOR);
            g.fillRect(greenWidth, 0, this.getWidth() - greenWidth, this.getHeight());
        }
    }
}
