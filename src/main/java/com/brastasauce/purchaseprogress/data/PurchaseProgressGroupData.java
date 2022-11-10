package com.brastasauce.purchaseprogress.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PurchaseProgressGroupData
{
    private String name;
    private List<Integer> items;
}
