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

    @Getter
    private long totalCost;

    static
    {
        final BufferedImage addImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/add_icon_white.png");
        ADD_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));
        ADD_HOVER_ICON = new ImageIcon(addImage);
    }

    PurchaseProgressGroupPanel(PurchaseProgressPlugin plugin, PurchaseProgressPluginPanel panel, PurchaseProgressGroup group)
    {
        setLayout(new BorderLayout(5, 0));
        setBorder(new EmptyBorder(5, 5, 5, 0));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

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

        // Group Name
        JLabel groupName = new JLabel();
        groupName.setForeground(Color.WHITE);
        groupName.setPreferredSize(new Dimension(150, 0));
        groupName.setText(group.getName());
        topPanel.add(groupName, BorderLayout.WEST);

        // Actions Panel
        JPanel actions = new JPanel(new BorderLayout());
        actions.setBorder(new EmptyBorder(0, 0, 0, 5));
        actions.setOpaque(false);

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
        actions.add(edit, BorderLayout.WEST);

        // Empty panel to separate without causing extra hover
        JPanel empty = new JPanel();
        empty.setOpaque(false);
        actions.add(empty, BorderLayout.CENTER);

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
        actions.add(addItem, BorderLayout.EAST);

        topPanel.add(actions, BorderLayout.EAST);

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
            PurchaseProgressGroupItemPanel itemPanel = new PurchaseProgressGroupItemPanel(plugin, group, item);

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
        for (PurchaseProgressItem item : group.getItems())
        {
            totalCost += item.getGePrice();
        }

        if (totalCost != 0)
        {
            PurchaseProgressTotalPanel totalPanel = new PurchaseProgressTotalPanel(plugin.getValue(), totalCost, ColorScheme.DARK_GRAY_COLOR);
            itemsPanel.add(createMarginWrapper(totalPanel), constraints);
        }

        add(topPanel, BorderLayout.NORTH);
        add(itemsPanel, BorderLayout.CENTER);
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
}
