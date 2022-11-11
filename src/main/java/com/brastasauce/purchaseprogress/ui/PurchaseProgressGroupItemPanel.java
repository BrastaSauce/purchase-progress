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

import com.brastasauce.purchaseprogress.PurchaseProgressPlugin;
import com.brastasauce.purchaseprogress.data.PurchaseProgressGroup;
import com.brastasauce.purchaseprogress.data.PurchaseProgressItem;
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

public class PurchaseProgressGroupItemPanel extends JPanel
{
    private static final String REMOVE_TITLE = "Warning";
    private static final String REMOVE_MESSAGE = "Are you sure you want to remove this progress item from the group?";
    private static final ImageIcon REMOVE_ICON;
    private static final ImageIcon REMOVE_HOVER_ICON;
    private static final ImageIcon SHIFT_UP_ICON;
    private static final ImageIcon SHIFT_UP_HOVER_ICON;
    private static final ImageIcon SHIFT_DOWN_ICON;
    private static final ImageIcon SHIFT_DOWN_HOVER_ICON;
    private static final Dimension IMAGE_SIZE = new Dimension(32, 32);

    private float percent;

    static
    {
        final BufferedImage removeImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/delete_icon.png");
        REMOVE_ICON = new ImageIcon(removeImage);
        REMOVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(removeImage, 0.53f));

        final BufferedImage shiftUpImage = ImageUtil.loadImageResource(PurchaseProgressPlugin.class, "/shift_up_icon.png");
        SHIFT_UP_ICON = new ImageIcon(shiftUpImage);
        SHIFT_UP_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftUpImage, 0.53f));

        final BufferedImage shiftDownImage = ImageUtil.loadImageResource(PurchaseProgressPlugin.class, "/shift_down_icon.png");
        SHIFT_DOWN_ICON = new ImageIcon(shiftDownImage);
        SHIFT_DOWN_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(shiftDownImage, 0.53f));
    }

    PurchaseProgressGroupItemPanel(PurchaseProgressPlugin plugin, PurchaseProgressGroup group, PurchaseProgressItem item)
    {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));

        int itemIndex = group.getItems().indexOf(item);
        int itemsSize = group.getItems().size();

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

        // Action Panel (Delete, Shift item)
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(new Color(0, 0, 0, 0));
        actionPanel.setOpaque(false);

        // Delete Item
        JLabel removeItem = new JLabel(REMOVE_ICON);
        removeItem.setBorder(new EmptyBorder(0, 15, 0, 0));
        removeItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (removeConfirm())
                {
                    plugin.removeItemFromGroup(group, item);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                removeItem.setIcon(REMOVE_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                removeItem.setIcon(REMOVE_ICON);
            }
        });
        actionPanel.add(removeItem, BorderLayout.NORTH);

        // Shift Item Panel
        JPanel shiftItemPanel = new JPanel(new BorderLayout());
        shiftItemPanel.setOpaque(false);

        // Shift item up
        JLabel shiftUp = new JLabel(SHIFT_UP_ICON);
        shiftUp.setBorder(new EmptyBorder(0, 0, 0, 5));

        if (itemIndex == 0)
        {
            shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
        }

        shiftUp.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (itemIndex != 0)
                {
                    plugin.shiftItemInGroup(group, itemIndex, true);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                shiftUp.setIcon(SHIFT_UP_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (itemIndex != 0)
                {
                    shiftUp.setIcon(SHIFT_UP_ICON);
                }
            }
        });
        shiftItemPanel.add(shiftUp, BorderLayout.WEST);

        // Shift item down
        JLabel shiftDown = new JLabel(SHIFT_DOWN_ICON);
        shiftDown.setBorder(new EmptyBorder(0, 0, 0, 5));

        if (itemIndex == itemsSize - 1)
        {
            shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
        }

        shiftDown.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (itemIndex != itemsSize - 1)
                {
                    plugin.shiftItemInGroup(group, itemIndex, false);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                shiftDown.setIcon(SHIFT_DOWN_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (itemIndex != itemsSize - 1)
                {
                    shiftDown.setIcon(SHIFT_DOWN_ICON);
                }
            }
        });
        shiftItemPanel.add(shiftDown, BorderLayout.EAST);

        actionPanel.add(shiftItemPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.EAST);
    }

    private boolean removeConfirm()
    {
        int confirm = JOptionPane.showConfirmDialog(this,
                REMOVE_MESSAGE, REMOVE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor(new Color(12, 85, 35));
        int greenWidth = (int) (this.getWidth() * percent / 100);
        g.fillRect(0, 0, greenWidth, this.getHeight());

        if (greenWidth != this.getWidth())
        {
            g.setColor(ColorScheme.DARK_GRAY_COLOR);
            g.fillRect(greenWidth, 0, this.getWidth() - greenWidth, this.getHeight());
        }
    }
}
