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

import com.brastasauce.purchaseprogress.PurchaseProgressConfig;
import com.brastasauce.purchaseprogress.PurchaseProgressPlugin;
import com.brastasauce.purchaseprogress.data.PurchaseProgressGroup;
import com.brastasauce.purchaseprogress.data.PurchaseProgressItem;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

public class PurchaseProgressGroupPanel extends JPanel
{
    private static final String DELETE_TITLE = "Warning";
    private static final String DELETE_MESSAGE = "Are you sure you want to delete this progress group? This will not delete your items.";
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon COLLAPSED_ICON;
    private static final ImageIcon COLLAPSED_HOVER_ICON;
    private static final ImageIcon UNCOLLAPSED_ICON;
    private static final ImageIcon UNCOLLAPSED_HOVER_ICON;

    private float percent;
    private final boolean collapsed;

    @Getter
    private long totalCost;

    static
    {
        final BufferedImage addImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/add_icon_white.png");
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));
        ADD_HOVER_ICON = new ImageIcon(addImage);

        final BufferedImage collapsedImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/collapsed_icon.png");
        COLLAPSED_ICON = new ImageIcon(collapsedImage);
        COLLAPSED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(collapsedImage, 0.53f));

        final BufferedImage uncollapsedImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/shift_down_icon.png");
        UNCOLLAPSED_ICON = new ImageIcon(uncollapsedImage);
        UNCOLLAPSED_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(uncollapsedImage, 0.53f));
    }

    PurchaseProgressGroupPanel(PurchaseProgressPlugin plugin, PurchaseProgressPluginPanel panel, PurchaseProgressGroup group, PurchaseProgressConfig config)
    {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        for (PurchaseProgressItem item : group.getItems())
        {
            totalCost += item.getGePrice();
        }

        this.collapsed = group.isCollapsed();

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Right click for deleting group
        JPopupMenu deletePopup = new JPopupMenu();

        JMenuItem delete = new JMenuItem(new AbstractAction("Delete Group")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (deleteConfirm())
                {
                    plugin.removeGroup(group);
                }
            }
        });
        deletePopup.add(delete);

        topPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (SwingUtilities.isRightMouseButton(e))
                {
                    deletePopup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        // Collapse and Names
        JPanel leftActions = new JPanel(new BorderLayout());
        leftActions.setOpaque(false);

        // Group Name
        JLabel groupName = new JLabel();
        groupName.setForeground(Color.WHITE);
        groupName.setBorder(new EmptyBorder(0, 5, 0, 0));
        groupName.setPreferredSize(new Dimension(140, 0));
        groupName.setText(group.getName());

        // Collapse
        JLabel collapseButton = new JLabel();
        collapseButton.setOpaque(false);

        if (collapsed)
        {
            groupName.setPreferredSize(new Dimension(160, 0));

            collapseButton.setIcon(COLLAPSED_ICON);
            collapseButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    plugin.switchGroupCollapse(group);
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    collapseButton.setIcon(COLLAPSED_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    collapseButton.setIcon(COLLAPSED_ICON);
                }
            });

            leftActions.add(groupName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);

            // Percent
            JLabel percentLabel = new JLabel();
            percentLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
            percent = ((float) plugin.getValue() / totalCost) * 100;
            if (totalCost == 0)
            {
                percent = 0;
            }
            else if (percent >= 100)
            {
                percent = 100;
            }
            percentLabel.setText(String.format("%.0f", percent) + "%");

            topPanel.add(leftActions, BorderLayout.WEST);
            topPanel.add(percentLabel, BorderLayout.EAST);

            add(topPanel, BorderLayout.CENTER);
        }
        else
        {
            collapseButton.setIcon(UNCOLLAPSED_ICON);
            collapseButton.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    plugin.switchGroupCollapse(group);
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    collapseButton.setIcon(UNCOLLAPSED_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    collapseButton.setIcon(UNCOLLAPSED_ICON);
                }
            });

            leftActions.add(groupName, BorderLayout.EAST);
            leftActions.add(collapseButton, BorderLayout.WEST);

            topPanel.add(leftActions, BorderLayout.WEST);

            // Actions Panel
            JPanel rightActions = new JPanel(new BorderLayout());
            rightActions.setBorder(new EmptyBorder(0, 0, 0, 5));
            rightActions.setOpaque(false);

            // Edit Button
            JLabel edit = new JLabel("Edit");
            edit.setVerticalAlignment(SwingConstants.CENTER);
            edit.setBorder(new EmptyBorder(0, 0, 0, 0));
            edit.setForeground(Color.LIGHT_GRAY);
            edit.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    plugin.editGroup(group);
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    edit.setForeground(Color.WHITE);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    edit.setForeground(Color.LIGHT_GRAY);
                }
            });
            rightActions.add(edit, BorderLayout.WEST);

            // Empty panel to separate without causing extra hover
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            rightActions.add(empty, BorderLayout.CENTER);

            JLabel addItem = new JLabel(ADD_ICON);
            addItem.setOpaque(false);
            addItem.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    final String[] itemNames = plugin.getItems().stream().map(PurchaseProgressItem::getName).toArray(String[]::new);
                    Arrays.sort(itemNames, String.CASE_INSENSITIVE_ORDER);

                    PurchaseProgressSelectionPanel selection = new PurchaseProgressSelectionPanel(panel, itemNames);
                    selection.setOnOk(e1 -> {
                        List<String> selectedItems = selection.getSelectedItems();
                        if (!selectedItems.isEmpty())
                        {
                            plugin.addItemsToGroup(group, selectedItems);
                        }
                    });
                    selection.show();
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {
                    addItem.setIcon(ADD_HOVER_ICON);
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    addItem.setIcon(ADD_ICON);
                }
            });
            rightActions.add(addItem, BorderLayout.EAST);

            topPanel.add(rightActions, BorderLayout.EAST);

            // Group Items
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridwidth = 1;
            constraints.weightx = 1;
            constraints.gridx = 0;
            constraints.gridy = 1;

            JPanel itemsPanel = new JPanel();
            itemsPanel.setLayout(new GridBagLayout());
            itemsPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
            itemsPanel.setOpaque(false);

            int index = 0;
            for (PurchaseProgressItem item : group.getItems())
            {
                PurchaseProgressGroupItemPanel itemPanel = new PurchaseProgressGroupItemPanel(plugin, group, item, config);

                if (index++ > 0)
                {
                    itemsPanel.add(createMarginWrapper(itemPanel), constraints);
                }
                else
                {
                    itemsPanel.add(itemPanel, constraints);
                }

                constraints.gridy++;
            }

            // Bottom Panel
            if (totalCost != 0)
            {
                PurchaseProgressTotalPanel totalPanel = new PurchaseProgressTotalPanel(plugin.getValue(), totalCost, ColorScheme.DARK_GRAY_COLOR);
                itemsPanel.add(createMarginWrapper(totalPanel), constraints);
            }

            add(topPanel, BorderLayout.NORTH);
            add(itemsPanel, BorderLayout.CENTER);
        }
    }

    private boolean deleteConfirm()
    {
        int confirm = JOptionPane.showConfirmDialog(this,
                DELETE_MESSAGE, DELETE_TITLE, JOptionPane.YES_NO_OPTION);

        return confirm == JOptionPane.YES_NO_OPTION;
    }

    private JPanel createMarginWrapper(JPanel panel)
    {
        JPanel marginWrapper = new JPanel(new BorderLayout());
        marginWrapper.setOpaque(false);
        marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        marginWrapper.add(panel, BorderLayout.NORTH);
        return marginWrapper;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        if (collapsed)
        {
            g.setColor(new Color(12, 85, 35));
            int greenWidth = (int) (this.getWidth() * percent / 100);
            g.fillRect(0, 0, greenWidth, this.getHeight());

            if (greenWidth != this.getWidth())
            {
                g.setColor(ColorScheme.DARKER_GRAY_COLOR);
                g.fillRect(greenWidth, 0, this.getWidth() - greenWidth, this.getHeight());
            }
        }
        else
        {
            g.setColor(ColorScheme.DARKER_GRAY_COLOR);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }
}
