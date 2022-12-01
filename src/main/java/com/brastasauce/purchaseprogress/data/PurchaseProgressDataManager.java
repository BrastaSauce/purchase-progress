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
package com.brastasauce.purchaseprogress.data;

import com.brastasauce.purchaseprogress.PurchaseProgressPlugin;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.brastasauce.purchaseprogress.PurchaseProgressPlugin.CONFIG_GROUP;

@Slf4j
public class PurchaseProgressDataManager
{
    private static final String CONFIG_KEY_VALUE = "value";
    private static final String CONFIG_KEY_ITEMIDS = "itemIds";
    private static final String CONFIG_KEY_GROUPS = "groups";

    private final PurchaseProgressPlugin plugin;
    private final Client client;
    private final ConfigManager configManager;
    private final ItemManager itemManager;
    private final Gson gson;

    private List<Integer> itemIds = new ArrayList<>();
    private final Type itemsType = new TypeToken<ArrayList<Integer>>(){}.getType();

    private List<PurchaseProgressGroupData> groups = new ArrayList<>();
    private final Type groupsType = new TypeToken<ArrayList<PurchaseProgressGroupData>>(){}.getType();

    @Inject
    public PurchaseProgressDataManager(PurchaseProgressPlugin plugin, Client client, ConfigManager configManager, ItemManager itemManager, Gson gson)
    {
        this.plugin = plugin;
        this.client = client;
        this.configManager = configManager;
        this.itemManager = itemManager;
        this.gson = gson;
    }

    public boolean loadData()
    {
        // Load later if not at login screen to prevent data loss
        if (client.getGameState().getState() < GameState.LOGIN_SCREEN.getState())
        {
            return false;
        }

        // Value
        String value = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_VALUE);
        plugin.setValue(Long.parseLong(value));

        // Individual Items
        itemIds.clear();

        String itemsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_ITEMIDS);
        if (itemsJson == null || itemsJson.equals("[]"))
        {
            plugin.setItems(new ArrayList<>());
        }
        else
        {
            try
            {
                itemIds = gson.fromJson(itemsJson, itemsType);
                convertItems();
            }
            catch (Exception e)
            {
                log.error("Exception occurred while loading purchase progress items", e);
                plugin.setItems(new ArrayList<>());
            }
        }

        // Groups and their items
        groups.clear();

        String groupsJson = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_GROUPS);
        if (groupsJson == null || groupsJson.equals("[]"))
        {
            plugin.setGroups(new ArrayList<>());
        }
        else
        {
            try
            {
                groups = gson.fromJson(groupsJson, groupsType);
                convertGroups();
            }
            catch (Exception e)
            {
                log.error("Exception occurred while loading purchase progress groups", e);
                plugin.setGroups(new ArrayList<>());
            }
        }

        plugin.updateItemPrices();
        return true;
    }

    public void saveData()
    {
        // Value
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_VALUE, String.valueOf(plugin.getValue()));

        // Individual Items
        itemIds.clear();

        for (PurchaseProgressItem item : plugin.getItems())
        {
            itemIds.add(item.getItemId());
        }

        final String itemsJson = gson.toJson(itemIds);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_ITEMIDS, itemsJson);

        // Groups and their items
        groups.clear();

        for (PurchaseProgressGroup group : plugin.getGroups())
        {
            List<Integer> groupItems = new ArrayList<>();
            for (PurchaseProgressItem item : group.getItems())
            {
                groupItems.add(item.getItemId());
            }

            groups.add(new PurchaseProgressGroupData(group.getName(), group.isCollapsed(), groupItems));
        }

        final String groupsJson = gson.toJson(groups);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_GROUPS, groupsJson);
    }

    private void convertItems()
    {
        List<PurchaseProgressItem> progressItems = new ArrayList<>();

        for (Integer itemId : itemIds)
        {
            progressItems.add(convertIdToItem(itemId));
        }

        plugin.setItems(progressItems);
    }

    private void convertGroups()
    {
        List<PurchaseProgressGroup> progressGroups = new ArrayList<>();

        for (PurchaseProgressGroupData group : groups)
        {
            List<PurchaseProgressItem> groupItems = new ArrayList<>();
            for (Integer itemId : group.getItems())
            {
                groupItems.add(convertIdToItem(itemId));
            }

            progressGroups.add(new PurchaseProgressGroup(group.getName(), group.isCollapsed(), groupItems));
        }

        plugin.setGroups(progressGroups);
    }

    private PurchaseProgressItem convertIdToItem(int itemId)
    {
        AsyncBufferedImage itemImage = itemManager.getImage(itemId);
        String itemName = itemManager.getItemComposition(itemId).getName();
        return new PurchaseProgressItem(itemImage, itemName, itemId, 0); // Item prices updated after load
    }
}
