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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
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

    private final PurchaseProgressPlugin plugin;
    private final ConfigManager configManager;
    private final ItemManager itemManager;
    private final Gson gson;

    private final Type itemsType = new TypeToken<ArrayList<Integer>>(){}.getType();

    private List<Integer> itemIds = new ArrayList<>();

    @Inject
    public PurchaseProgressDataManager(PurchaseProgressPlugin plugin, ConfigManager configManager, ItemManager itemManager, Gson gson)
    {
        this.plugin = plugin;
        this.configManager = configManager;
        this.itemManager = itemManager;
        this.gson = gson;
    }

    public void loadData()
    {
        String value = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_VALUE);
        plugin.setValue(Long.parseLong(value));

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
                itemIds = (gson.fromJson(itemsJson, itemsType));
                convertIds();
            }
            catch (Exception e)
            {
                log.error("Exception occurred while loading purchase progress data", e);
                plugin.setItems(new ArrayList<>());
            }
        }
    }

    public void saveData()
    {
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_VALUE, String.valueOf(plugin.getValue()));

        itemIds.clear();
        for (PurchaseProgressItem item : plugin.getItems())
        {
            itemIds.add(item.getItemId());
        }

        final String itemsJson = gson.toJson(itemIds);
        configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_ITEMIDS, itemsJson);
    }

    private void convertIds()
    {
        List<PurchaseProgressItem> progressItems = new ArrayList<>();

        for (Integer itemId : itemIds)
        {
            AsyncBufferedImage itemImage = itemManager.getImage(itemId);
            String itemName = itemManager.getItemComposition(itemId).getName();

            // Item prices get updated after load
            PurchaseProgressItem progressItem = new PurchaseProgressItem(itemImage, itemName, itemId, 0);
            progressItems.add(progressItem);
        }

        plugin.setItems(progressItems);
    }
}
