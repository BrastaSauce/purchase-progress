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

import net.runelite.client.util.QuantityFormatter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

public class PurchaseProgressTotalPanel extends JPanel
{
    private float percent;
    private final Color background;

    PurchaseProgressTotalPanel(long value, long totalCost, Color background)
    {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 0));

        this.background = background;

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(new Color(0, 0, 0, 0));
        totalPanel.setOpaque(false);

        JLabel totalLabel = new JLabel();
        totalLabel.setForeground(new Color(255, 202, 36));
        totalLabel.setText("Total: " + QuantityFormatter.formatNumber(totalCost) + " gp");
        totalPanel.add(totalLabel, BorderLayout.WEST);

        JLabel percentLabel = new JLabel();
        percentLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        percent = ((float) value / totalCost) * 100;
        if (percent >= 100)
        {
            percent = 100;
        }
        percentLabel.setText(String.format("%.0f", percent) + "%");
        totalPanel.add(percentLabel, BorderLayout.EAST);

        add(totalPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        g.setColor(new Color(12, 85, 35));
        int greenWidth = (int) (this.getWidth() * percent / 100);
        g.fillRect(0, 0, greenWidth, this.getHeight());

        if (greenWidth != this.getWidth())
        {
            g.setColor(background);
            g.fillRect(greenWidth, 0, this.getWidth() - greenWidth, this.getHeight());
        }
    }
}
