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

import com.brastasauce.purchaseprogress.data.PurchaseProgressDataManager;
import com.brastasauce.purchaseprogress.data.PurchaseProgressGroup;
import com.brastasauce.purchaseprogress.data.PurchaseProgressItem;
import com.brastasauce.purchaseprogress.ui.PurchaseProgressPluginPanel;
import com.google.gson.Gson;
import com.google.inject.Provides;

import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Purchase Progress"
)
public class PurchaseProgressPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "purchaseprogress";
	private static final String PLUGIN_NAME = "Purchase Progress";
	private static final String ICON_IMAGE = "/panel_icon.png";
	private static final int MAX_GROUP_NAME_LENGTH = 50;

	@Getter
	@Setter
	private List<PurchaseProgressItem> items = new ArrayList<>();

	@Getter
	@Setter
	private List<PurchaseProgressGroup> groups = new ArrayList<>();

	@Getter
	@Setter
	private long value = 0;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BankCalculation bankCalculation;

	@Inject
	private Gson gson;

	@Inject
	private PurchaseProgressDataManager dataManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private Client client;

	@Inject
	private PurchaseProgressConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private PurchaseProgressPluginPanel panel;
	private NavigationButton navButton;

	public void addItem(PurchaseProgressItem item)
	{
		clientThread.invokeLater(() ->
		{
			if (!containsItem(item))
			{
				items.add(item);
				dataManager.saveData();
				SwingUtilities.invokeLater(() ->
				{
					panel.switchToProgress();
					panel.updateProgressPanels();
				});
			}
			else
			{
				SwingUtilities.invokeLater(() -> panel.containsItemWarning());
			}
		});
	}

	public void removeItem(PurchaseProgressItem item)
	{
		clientThread.invokeLater(() -> {
			items.remove(item);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	public void addGroup()
	{
		final String msg = "Enter the name of this group (max " + MAX_GROUP_NAME_LENGTH + " chars).";
		String name = JOptionPane.showInputDialog(panel, msg, "Add New Group", JOptionPane.PLAIN_MESSAGE);

		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > MAX_GROUP_NAME_LENGTH)
		{
			name = name.substring(0, MAX_GROUP_NAME_LENGTH);
		}

		String groupName = name;
		clientThread.invokeLater(() -> {
			PurchaseProgressGroup group = new PurchaseProgressGroup(groupName, new ArrayList<>());

			if (!groups.contains(group))
			{
				groups.add(group);
				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
			}
		});
	}

	public void editGroup(PurchaseProgressGroup group)
	{
		final String msg = "Enter the name of this group (max " + MAX_GROUP_NAME_LENGTH + " chars).";
		String name = JOptionPane.showInputDialog(panel, msg, "Edit Group", JOptionPane.PLAIN_MESSAGE);

		if (name == null || name.isEmpty())
		{
			return;
		}

		if (name.length() > MAX_GROUP_NAME_LENGTH)
		{
			name = name.substring(0, MAX_GROUP_NAME_LENGTH);
		}

		String groupName = name;
		clientThread.invokeLater(() -> {
			PurchaseProgressGroup nameCheck = groups.stream().filter(o -> o.getName().equals(groupName)).findFirst().orElse(null);

			if (nameCheck == null)
			{
				group.setName(groupName);
				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
			}
		});
	}

	public void removeGroup(PurchaseProgressGroup group)
	{
		clientThread.invokeLater(() -> {
			// Move items out of group and delete
			items.addAll(group.getItems());
			groups.remove(group);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	public void addItemsToGroup(PurchaseProgressGroup group, List<String> itemNames)
	{
		clientThread.invokeLater(() -> {
			for (String itemName : itemNames)
			{
				PurchaseProgressItem item = items.stream().filter(o -> o.getName().equals(itemName)).findFirst().orElse(null);
				group.getItems().add(item);
				items.remove(item);
			}
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	public void removeItemFromGroup(PurchaseProgressGroup group, PurchaseProgressItem item)
	{
		clientThread.invokeLater(() -> {
			group.getItems().remove(item);
			items.add(item);
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	public void switchGroupCollapse(PurchaseProgressGroup group)
	{
		clientThread.invokeLater(() -> {
			group.setCollapsed(!group.isCollapsed());
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	@Schedule(
			period = 5,
			unit = ChronoUnit.MINUTES
	)
	public void updateItemPrices()
	{
		// Group item prices
		for (PurchaseProgressGroup group : groups)
		{
			for (PurchaseProgressItem item : group.getItems())
			{
				item.setGePrice(itemManager.getItemPrice(item.getItemId()));
			}
		}

		// Individual prices
		for (PurchaseProgressItem item : items)
		{
			item.setGePrice(itemManager.getItemPrice(item.getItemId()));
		}

		SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
	}

	public void sort(boolean sortAscending)
	{
		clientThread.invokeLater(() -> {
			if (!items.isEmpty())
			{
				if (sortAscending)
				{
					items.sort(Comparator.naturalOrder());
				}
				else
				{
					items.sort(Comparator.reverseOrder());
				}

				dataManager.saveData();
				SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
			}
		});
	}

	public void shiftItem(int itemIndex, boolean shiftUp)
	{
		clientThread.invokeLater(() -> {
			PurchaseProgressItem shiftedItem = items.get(itemIndex);

			// Out of bounds is checked before call in item panel
			if (shiftUp)
			{
				items.set(itemIndex, items.get(itemIndex - 1));
				items.set(itemIndex - 1, shiftedItem);
			}
			else
			{
				items.set(itemIndex, items.get(itemIndex + 1));
				items.set(itemIndex + 1, shiftedItem);
			}

			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	public void shiftItemInGroup(PurchaseProgressGroup group, int itemIndex, boolean shiftUp)
	{
		clientThread.invokeLater(() -> {
			List<PurchaseProgressItem> groupItems = group.getItems();
			PurchaseProgressItem shiftedItem = group.getItems().get(itemIndex);

			// Out of bounds is checked before call in group item panel
			if (shiftUp)
			{
				groupItems.set(itemIndex, groupItems.get(itemIndex - 1));
				groupItems.set(itemIndex -1, shiftedItem);
			}
			else
			{
				groupItems.set(itemIndex, groupItems.get(itemIndex + 1));
				groupItems.set(itemIndex +1, shiftedItem);
			}

			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		});
	}

	private boolean containsItem(PurchaseProgressItem newItem)
	{
		for (PurchaseProgressGroup group : groups)
		{
			if (group.getItems().contains(newItem))
			{
				return true;
			}
		}
		return items.contains(newItem);
	}

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(PurchaseProgressPluginPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(PurchaseProgressPlugin.class, ICON_IMAGE);

		navButton = NavigationButton.builder()
				.tooltip(PLUGIN_NAME)
				.icon(icon)
				.priority(9)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		this.dataManager = new PurchaseProgressDataManager(this, configManager, itemManager, gson);

		clientThread.invokeLater(() ->
		{
			dataManager.loadData();
			updateItemPrices();
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	@Provides
	PurchaseProgressConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PurchaseProgressConfig.class);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD)
		{
			value = bankCalculation.calculateValue();
			dataManager.saveData();
			SwingUtilities.invokeLater(() -> panel.updateProgressPanels());
		}
	}
}
