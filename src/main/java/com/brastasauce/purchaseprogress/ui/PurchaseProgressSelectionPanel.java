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

import net.runelite.client.ui.ColorScheme;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PurchaseProgressSelectionPanel
{
    private final JList<String> list;
    private ActionListener okEvent;
    private final JDialog dialog;

    private static final String TITLE = "Select Items";
    private static final String MESSAGE = "Select items to add to this group";
    private static final String SUBMESSAGE = "Ctrl+Click to select multiple items";

    public PurchaseProgressSelectionPanel(JPanel parent, String[] options)
    {
        this.list = new JList<>(options);
        this.list.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel message = new JLabel(MESSAGE);
        message.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subMessage = new JLabel(SUBMESSAGE);
        subMessage.setHorizontalAlignment(SwingConstants.CENTER);

        topPanel.add(message, BorderLayout.NORTH);
        topPanel.add(subMessage, BorderLayout.CENTER);

        // Center Panel with Items
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setPreferredSize(new Dimension(250, 300));

        DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);

        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Options
        JOptionPane optionPane = new JOptionPane(centerPanel);

        JButton okButton = new JButton("Ok");
        okButton.addActionListener(this::onOkButtonClick);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::onCancelButtonClick);

        optionPane.setOptions(new Object[]{okButton, cancelButton});

        dialog = optionPane.createDialog(parent, "Select items");
        dialog.setTitle(TITLE);
    }

    public List<String> getSelectedItems()
    {
        return list.getSelectedValuesList();
    }

    public void setOnOk(ActionListener event)
    {
        okEvent = event;
    }

    private void onOkButtonClick(ActionEvent e)
    {
        if (okEvent != null)
        {
            okEvent.actionPerformed(e);
        }
        dialog.setVisible(false);
    }

    private void onCancelButtonClick(ActionEvent e)
    {
        dialog.setVisible(false);
    }

    public void show()
    {
        dialog.setVisible(true);
    }
}
