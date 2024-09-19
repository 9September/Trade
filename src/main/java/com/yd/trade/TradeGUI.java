package com.yd.trade;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class TradeGUI implements Listener {
    private final Trade plugin;
    private final ItemStack confirmPane;
    private boolean tradeSuccessful = false;

    private final Map<String, Map<String, TradeData>> tradeDataMap = new HashMap<>();

    public TradeGUI(Trade plugin) {
        this.plugin = plugin;
        this.confirmPane = createConfirmPane();
        initializeTradeData();
    }

    private ItemStack createConfirmPane() {
        ItemStack pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a계승 확인");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private void initializeTradeData() {
        String[] classNames = {"어쌔신", "아처", "드래곤워리어", "워리어", "화염술사", "바드", "거너", "무투가"};
        String[] tiers = {"0차", "1차", "2차", "3차", "4차"};
        int[] requiredLevels = {5, 10, 20, 30, 40};

        String[][] commands = {
                {"mi give SWORD ASSASSIN_BLADE_1 %player% 1", "mi give SWORD ASSASSIN_BLADE_2 %player% 1", "mi give SWORD ASSASSIN_BLADE_3 %player% 1", "mi give SWORD ASSASSIN_BLADE_4 %player% 1", "mi give SWORD ASSASSIN_BLADE_SHINY %player% 1"},
                {"mi give SWORD ARCHER_BOW_1 %player% 1", "mi give SWORD ARCHER_BOW_2 %player% 1", "mi give SWORD ARCHER_BOW_3 %player% 1", "mi give SWORD ARCHER_BOW_4 %player% 1", "mi give SWORD ARCHER_BOW_SHINY %player% 1"},
                {"mi give SWORD DRAGON_WARRIOR_SPEAR1 %player% 1", "mi give SWORD DRAGON_WARRIOR_SPEAR2 %player% 1", "mi give SWORD DRAGON_WARRIOR_SPEAR3 %player% 1", "mi give SWORD DRAGON_WARRIOR_SPEAR4 %player% 1", "mi give SWORD DRAGON_WARRIOR_SPEAR_SHINY %player% 1"},
                {"mi give SWORD WARRIOR_SWORD_1 %player% 1", "mi give SWORD WARRIOR_SWORD_2 %player% 1", "mi give SWORD WARRIOR_SWORD_3 %player% 1", "mi give SWORD WARRIOR_SWORD_4 %player% 1", "mi give SWORD WARRIOR_SWORD_SHINY %player% 1"},
                {"mi give SWORD FIREMAGE_STAFF_1 %player% 1", "mi give SWORD FIREMAGE_STAFF_2 %player% 1", "mi give SWORD FIREMAGE_STAFF_3 %player% 1", "mi give SWORD FIREMAGE_STAFF_4 %player% 1", "mi give SWORD FIREMAGE_STAFF_SHINY %player% 1"},
                {"mi give SWORD BARD_HARP_1 %player% 1", "mi give SWORD BARD_HARP_2 %player% 1", "mi give SWORD BARD_HARP_3 %player% 1", "mi give SWORD BARD_HARP_4 %player% 1", "mi give SWORD BARD_HARP_SHINY %player% 1"},
                {"mi give SWORD GUNNER_PISTOL_1 %player% 1", "mi give SWORD GUNNER_PISTOL_2 %player% 1", "mi give SWORD GUNNER_PISTOL_3 %player% 1", "mi give SWORD GUNNER_PISTOL_4 %player% 1", "mi give SWORD GUNNER_PISTOL_SHINY %player% 1"},
                {"mi give SWORD MARTIAL_ARTIST_GAUNTLET1 %player% 1", "mi give SWORD MARTIAL_ARTIST_GAUNTLET2 %player% 1", "mi give SWORD MARTIAL_ARTIST_GAUNTLET3 %player% 1", "mi give SWORD MARTIAL_ARTIST_GAUNTLET4 %player% 1", "mi give SWORD MARTIAL_ARTIST_GAUNTLET_SHINY %player% 1"}
        };

        for (int i = 0; i < classNames.length; i++) {
            String className = classNames[i];
            Map<String, TradeData> classTrades = new HashMap<>();
            for (int j = 0; j < tiers.length; j++) {
                String tier = tiers[j];
                int requiredLevel = requiredLevels[j];
                String command = commands[i][j];
                classTrades.put(tier, new TradeData(requiredLevel, command));
            }
            tradeDataMap.put(className, classTrades);
        }
    }

    public void openTradeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "무기 계승");
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < gui.getSize(); i++)
            gui.setItem(i, grayPane);
        gui.setItem(11, new ItemStack(Material.AIR));
        gui.setItem(15, this.confirmPane);
        player.openInventory(gui);
        this.tradeSuccessful = false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("무기 계승")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // 아이템을 슬롯 11에 넣는 처리
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            if (clickedItem != null && clickedItem.getItemMeta() != null) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName.contains("+7")) {
                    event.getView().getTopInventory().setItem(11, clickedItem.clone());
                    player.getInventory().removeItem(clickedItem);
                } else {
                    player.sendMessage("§c강화 수치가 부족합니다. +7 이상의 아이템만 교환할 수 있습니다.");
                }
            }
        }

        // 확인 버튼 클릭 처리
        if (event.getSlot() == 15) {
            ItemStack itemToTrade = event.getInventory().getItem(11);
            if (itemToTrade != null && itemToTrade.getItemMeta() != null) {
                String itemName = itemToTrade.getItemMeta().getDisplayName();

                String className = null;
                for (String cls : tradeDataMap.keySet()) {
                    if (itemName.contains(cls)) {
                        className = cls;
                        break;
                    }
                }

                String tier = null;
                for (String tr : tradeDataMap.getOrDefault(className, Collections.emptyMap()).keySet()) {
                    if (itemName.contains(tr)) {
                        tier = tr;
                        break;
                    }
                }

                if (className != null && tier != null) {
                    TradeData tradeData = tradeDataMap.get(className).get(tier);
                    if (player.getLevel() >= tradeData.requiredLevel) {
                        String command = tradeData.command.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        player.sendMessage("§f<< §a성공적으로 계승을 완료하였습니다! §f>>");
                        this.tradeSuccessful = true;
                        player.closeInventory();
                    } else {
                        player.sendMessage("§c플레이어 레벨이 충족되지 않았습니다!");
                        this.tradeSuccessful = false;
                        player.closeInventory();
                    }
                } else {
                    player.sendMessage("§c계승 조건에 맞지 않습니다!");
                    this.tradeSuccessful = false;
                    player.closeInventory();
                }
            } else {
                player.sendMessage("§c유효한 아이템을 선택해주세요.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("무기 계승")) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            if (!this.tradeSuccessful) {
                ItemStack itemInSlot11 = inventory.getItem(11);
                if (itemInSlot11 != null && itemInSlot11.getType() != Material.AIR)
                    player.getInventory().addItem(itemInSlot11);
            }
        }
    }

    private static class TradeData {
        int requiredLevel;
        String command;

        public TradeData(int requiredLevel, String command) {
            this.requiredLevel = requiredLevel;
            this.command = command;
        }
    }
}
