package com.example.ivlinereporting

data class Section(val title: String, val items: List<Item>)

sealed class Item{
    data class LayoutItem(val layoutId: Int):Item()
    data class SubSection(val title: String, val subItems: List<LayoutItem>):Item()
}