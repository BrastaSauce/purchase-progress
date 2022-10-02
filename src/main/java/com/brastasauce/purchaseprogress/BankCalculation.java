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

import com.google.common.collect.ImmutableList;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankCalculation
{
    private int hash;
    private Long bankValue;

    private static final List<Integer> TAB_VARBITS = ImmutableList.of(
            Varbits.BANK_TAB_ONE_COUNT,
            Varbits.BANK_TAB_TWO_COUNT,
            Varbits.BANK_TAB_THREE_COUNT,
            Varbits.BANK_TAB_FOUR_COUNT,
            Varbits.BANK_TAB_FIVE_COUNT,
            Varbits.BANK_TAB_SIX_COUNT,
            Varbits.BANK_TAB_SEVEN_COUNT,
            Varbits.BANK_TAB_EIGHT_COUNT,
            Varbits.BANK_TAB_NINE_COUNT
    );

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private PurchaseProgressConfig config;

    long calculateValue()
    {
        long value = 0;

        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        final ItemContainer bank = client.getItemContainer(InventoryID.BANK);

        // Add inventory GP/tokens
        if (inventory != null)
        {
            value += inventory.count(ItemID.COINS_995);
            value += inventory.count(ItemID.PLATINUM_TOKEN) * 1000L;
        }

        if (bank == null)
        {
            return value;
        }

        // Add bank GP/tokens
        value += bank.count(ItemID.COINS_995);
        value += bank.count(ItemID.PLATINUM_TOKEN) * 1000L;

        // Add loot tab value if selected
        if (!config.includeBankTab())
        {
            return value;
        }

        final Item[] items = bank.getItems();
        int lootTab = config.bankTab();

        if (lootTab != 0)
        {
            int startIndex = 0;

            for (int i = lootTab - 1; i > 0; i--)
            {
                startIndex += client.getVarbitValue(TAB_VARBITS.get(i - 1));
            }

            int itemCount = client.getVarbitValue(TAB_VARBITS.get(lootTab - 1));
            value += calculateItemValues(Arrays.copyOfRange(items, startIndex, startIndex + itemCount));
        }
        else
        {
            value += calculateItemValues(items);
        }

        return value;
    }

    private long calculateItemValues(Item[] items)
    {
        // Return last calculation if bank tab hasn't changed
        final int newHash = hashItems(items);
        if (bankValue != null && hash == newHash)
        {
            return bankValue;
        }

        hash = newHash;
        long value = 0;

        for (final Item item : items)
        {
            final int qty = item.getQuantity();
            final int id = item.getId();

            if (id <= 0 || qty == 0)
            {
                continue;
            }

            switch (id)
            {
                case ItemID.COINS_995:
                    break; // Inventory and Bank coins already calculated
                case ItemID.PLATINUM_TOKEN:
                    break; // Inventory and Bank tokens already calculated
                default:
                    value += (long) itemManager.getItemPrice(id) * qty;
                    break;
            }
        }

        bankValue = value;
        return value;
    }

    private int hashItems(final Item[] items)
    {
        final Map<Integer, Integer> mapCheck = new HashMap<>(items.length);
        for (Item item : items)
        {
            mapCheck.put(item.getId(), item.getQuantity());
        }

        return mapCheck.hashCode();
    }
}
