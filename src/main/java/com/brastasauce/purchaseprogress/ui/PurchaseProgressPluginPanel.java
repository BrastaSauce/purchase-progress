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
import com.google.inject.Inject;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;
import net.runelite.http.api.item.ItemPrice;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


public class PurchaseProgressPluginPanel extends PluginPanel
{
    private static final String PROGRESS_PANEL = "PROGRESS_PANEL";
    private static final String SEARCH_PANEL = "SEARCH_PANEL";
    private static final String RESULTS_PANEL = "RESULTS_PANEL";
    private static final String ERROR_PANEL = "ERROR_PANEL";
    private static final String CONTAINS_ITEM_TITLE = "Info";
    private static final String CONTAINS_ITEM_MESSAGE = "This item is already being tracked.";
    private static final ImageIcon ADD_ICON;
    private static final ImageIcon ADD_HOVER_ICON;
    private static final ImageIcon CANCEL_ICON;
    private static final ImageIcon CANCEL_HOVER_ICON;
    private static final ImageIcon SORT_ICON;
    private static final ImageIcon SORT_HOVER_ICON;
    private static final int MAX_SEARCH_ITEMS = 100;

    private final PurchaseProgressPlugin plugin;
    private final ClientThread clientThread;
    private final ItemManager itemManager;
    private final RuneLiteConfig runeLiteConfig;

    private final CardLayout centerCard = new CardLayout();
    private final CardLayout searchCard = new CardLayout();
    private final JPanel titlePanel = new JPanel(new BorderLayout());
    private final JLabel title = new JLabel();
    private final JLabel addItem = new JLabel(ADD_ICON);
    private final JLabel cancelItem = new JLabel(CANCEL_ICON);
    private final JPanel centerPanel = new JPanel(centerCard);
    private final JPanel progressPanel = new JPanel(new BorderLayout());
    private final JPanel valuePanel = new JPanel(new BorderLayout());
    private final JLabel value = new JLabel();
    private final JLabel sortButton = new JLabel();
    private final JPanel progressItemsPanel = new JPanel();
    private final JPanel searchPanel = new JPanel(new BorderLayout());
    private final JPanel searchCenterPanel = new JPanel(searchCard);
    private final JPanel searchResultsPanel = new JPanel();
    private final IconTextField searchBar = new IconTextField();
    private final PluginErrorPanel searchErrorPanel = new PluginErrorPanel();
    private final GridBagConstraints constraints = new GridBagConstraints();

    private final List<PurchaseProgressItem> searchItems = new ArrayList<>();

    static
    {
        final BufferedImage addImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/add_icon.png");
        ADD_ICON = new ImageIcon(addImage);
        ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addImage, 0.53f));

        final BufferedImage cancelImage = ImageUtil.loadImageResource(PurchaseProgressPluginPanel.class, "/cancel_icon.png");
        CANCEL_ICON = new ImageIcon(cancelImage);
        CANCEL_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(cancelImage, 0.53f));

        final BufferedImage sortImage = ImageUtil.loadImageResource(PurchaseProgressPlugin.class, "/sort_icon.png");
        SORT_ICON = new ImageIcon(sortImage);
        SORT_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(sortImage, 0.53f));
    }

    @Inject
    PurchaseProgressPluginPanel(PurchaseProgressPlugin plugin, ClientThread clientThread, ItemManager itemManager, RuneLiteConfig runeLiteConfig)
    {
        super(false);
        this.plugin = plugin;
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.runeLiteConfig = runeLiteConfig;

        setLayout(new BorderLayout());

        /* Container Panel (contains title panel and center panel) */
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));

        /* Title Panel */
        title.setText("Purchase Progress");
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(0, 0, 10, 40));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

        /* Add Item Button */
        addItem.setToolTipText("Add an item from the Grand Exchange");
        addItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                switchToSearch();
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
        actions.add(addItem);

        /* Cancel Button */
        cancelItem.setToolTipText("Cancel");
        cancelItem.setVisible(false);
        cancelItem.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                switchToProgress();
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                cancelItem.setIcon(CANCEL_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                cancelItem.setIcon(CANCEL_ICON);
            }
        });
        actions.add(cancelItem);

        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(actions, BorderLayout.EAST);

        /* Value */
        value.setForeground(new Color(255, 202, 36));
        value.setBorder(new EmptyBorder(0, 0, 5, 0));

        /* Sort Button */
        JPopupMenu sortPopup = new JPopupMenu();

        JMenuItem sortAscending = new JMenuItem(new AbstractAction("Sort (Low -> High)")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                plugin.sort(true);
            }
        });
        sortPopup.add(sortAscending);

        JMenuItem sortDescending = new JMenuItem(new AbstractAction("Sort (High -> Low)")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                plugin.sort(false);
            }
        });
        sortPopup.add(sortDescending);

        sortButton.setIcon(SORT_ICON);
        sortButton.setBorder(new EmptyBorder(0, 0, 0, 2));
        sortButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                sortPopup.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                sortButton.setIcon(SORT_HOVER_ICON);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                sortButton.setIcon(SORT_ICON);
            }
        });

        /* Value Panel (contains value text and sort button) */
        valuePanel.add(value, BorderLayout.WEST);
        valuePanel.add(sortButton, BorderLayout.EAST);

        /* Progress Items Panel */
        progressItemsPanel.setLayout(new GridBagLayout());

        JPanel pWrapper = new JPanel(new BorderLayout());
        pWrapper.add(progressItemsPanel, BorderLayout.NORTH);

        JScrollPane progressWrapper = new JScrollPane(pWrapper);
        progressWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        progressWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        progressWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        progressWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

        /* Progress Panel (contains value panel and progress items panel) */
        progressPanel.add(valuePanel, BorderLayout.NORTH);
        progressPanel.add(progressWrapper, BorderLayout.CENTER);

        /* Search Results Panel */
        searchResultsPanel.setLayout(new GridBagLayout());

        JPanel sWrapper = new JPanel(new BorderLayout());
        sWrapper.add(searchResultsPanel, BorderLayout.NORTH);

        JScrollPane resultsWrapper = new JScrollPane(sWrapper);
        resultsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
        resultsWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
        resultsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        resultsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(5, 5, 0, 0));

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.gridwidth = 1;
        constraints.weightx = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;

        /* Search Error Panel */
        searchErrorPanel.setContent("Grand Exchange Search",
                "Search for an item to select");

        JPanel errorWrapper = new JPanel(new BorderLayout());
        errorWrapper.add(searchErrorPanel, BorderLayout.NORTH);

        /* Search Center Panel (contains results and error panels) */
        searchCenterPanel.add(resultsWrapper, RESULTS_PANEL);
        searchCenterPanel.add(errorWrapper, ERROR_PANEL);
        searchCard.show(searchCenterPanel, ERROR_PANEL);

        /* Search Panel (contains search bar and search center panel) */
        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 15, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addClearListener(this::searchForItems);
        searchBar.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    searchForItems();
                }
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }
        });
        searchPanel.add(searchBar, BorderLayout.NORTH);
        searchPanel.add(searchCenterPanel, BorderLayout.CENTER);

        /* Center Panel (contains progress items/search items panel) */
        centerPanel.add(progressPanel, PROGRESS_PANEL);
        centerPanel.add(searchPanel, SEARCH_PANEL);
        centerCard.show(centerPanel, PROGRESS_PANEL);

        container.add(titlePanel, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);
    }

    private void searchForItems()
    {
        searchResultsPanel.removeAll();
        if (searchBar.getText().isEmpty())
        {
            searchResultsPanel.removeAll();
            SwingUtilities.invokeLater(() -> searchResultsPanel.updateUI());
            return;
        }

        List<ItemPrice> results = itemManager.search(searchBar.getText());
        if (results.isEmpty())
        {
            searchErrorPanel.setContent("No results found", "No items were found with that name, please try again");
            searchCard.show(searchCenterPanel, ERROR_PANEL);
            return;
        }

        clientThread.invokeLater(() -> processResults(results));
    }

    private void processResults(List<ItemPrice> results)
    {
        searchItems.clear();
        searchCard.show(searchCenterPanel, RESULTS_PANEL);

        int count = 0;
        boolean useActivelyTradedPrice = runeLiteConfig.useWikiItemPrices();

        // Add each result to items list
        for (ItemPrice item : results)
        {
            if (count++ > MAX_SEARCH_ITEMS)
            {
                break;
            }

            int itemId = item.getId();
            AsyncBufferedImage itemImage = itemManager.getImage(itemId);
            int itemPrice = useActivelyTradedPrice ? itemManager.getWikiPrice(item) : item.getPrice();
            searchItems.add(new PurchaseProgressItem(itemImage, item.getName(), itemId, itemPrice));
        }

        // Add each item in list to panel
        SwingUtilities.invokeLater(() ->
        {
            int index = 0;
            for (PurchaseProgressItem item : searchItems)
            {
                PurchaseProgressResultPanel panel = new PurchaseProgressResultPanel(plugin, item);

                if (index++ > 0)
                {
                    JPanel marginWrapper = new JPanel(new BorderLayout());
                    marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
                    marginWrapper.add(panel, BorderLayout.NORTH);
                    searchResultsPanel.add(marginWrapper, constraints);
                }
                else
                {
                    searchResultsPanel.add(panel, constraints);
                }

                constraints.gridy++;
            }

            validate();
        });
    }

    public void updateProgressPanels()
    {
        progressItemsPanel.removeAll();

        updateValue();

        constraints.gridy++;

        int index = 0;
        for (PurchaseProgressItem item : plugin.getItems())
        {
            PurchaseProgressItemPanel panel = new PurchaseProgressItemPanel(plugin, item);

            if (index++ > 0)
            {
                JPanel marginWrapper = new JPanel(new BorderLayout());
                marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
                marginWrapper.add(panel, BorderLayout.NORTH);
                progressItemsPanel.add(marginWrapper, constraints);
            }
            else
            {
                progressItemsPanel.add(panel, constraints);
            }

            constraints.gridy++;
        }

        validate();
    }

    private void updateValue()
    {
        long progressValue = plugin.getValue();
        if (progressValue == 0)
        {
            value.setText("Visit a bank to calculate value");
        }
        else
        {
            value.setText("Value: " + QuantityFormatter.formatNumber(plugin.getValue()) + " gp");
        }

        // Hide sort button if no items
        sortButton.setVisible(!plugin.getItems().isEmpty());
    }

    public void containsItemWarning()
    {
        JOptionPane.showConfirmDialog(this,
                CONTAINS_ITEM_MESSAGE, CONTAINS_ITEM_TITLE, JOptionPane.DEFAULT_OPTION);
    }

    public void switchToProgress()
    {
        cancelItem.setVisible(false);
        addItem.setVisible(true);
        centerCard.show(centerPanel, PROGRESS_PANEL);
    }

    private void switchToSearch()
    {
        addItem.setVisible(false);
        cancelItem.setVisible(true);
        centerCard.show(centerPanel, SEARCH_PANEL);
    }
}
