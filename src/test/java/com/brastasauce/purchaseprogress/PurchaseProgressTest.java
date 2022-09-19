package com.brastasauce.purchaseprogress;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PurchaseProgressTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PurchaseProgressPlugin.class);
		RuneLite.main(args);
	}
}