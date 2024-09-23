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

    // 중첩된 맵 구조: 클래스명 -> 아이템 타입 (무기/방어구) -> 아이템 이름 -> 등급 -> TradeData
    private final Map<String, Map<String, Map<String, Map<String, TradeData>>>> tradeDataMap = new HashMap<>();

    // 클래스, 무기, 방어구, 등급, 요구 레벨 정의
    private final String[] classNames = {"어쌔신", "아처", "드래곤워리어", "워리어", "화염술사", "바드", "거너", "무투가", "빙결사", "윈더"};
    private final String[] weaponNames = {"활", "검", "대검", "지팡이", "권총", "하프", "건틀릿", "창"};
    private final String[] armorNames = {"투구", "갑옷", "바지", "신발"};
    private final String[] tiers = {"0차", "1차", "2차", "3차", "4차"};
    private final int[] requiredLevels = {5, 10, 20, 30, 40};

    // 클래스 및 아이템 이름 매핑
    private final Map<String, String> classCommandMap = new HashMap<>();
    private final Map<String, String> weaponCommandMap = new HashMap<>();
    private final Map<String, String> armorCommandMap = new HashMap<>();

    public TradeGUI(Trade plugin) {
        this.plugin = plugin;
        this.confirmPane = createConfirmPane();
        initializeNameMappings();
        initializeTradeData();
    }

    // 클래스 및 아이템 이름 매핑 초기화
    private void initializeNameMappings() {
        // 클래스 이름 매핑 (한글 -> 영어 코드)
        classCommandMap.put("어쌔신", "ASSASSIN");
        classCommandMap.put("아처", "ARCHER");
        classCommandMap.put("드래곤워리어", "DRAGONWARRIOR");
        classCommandMap.put("워리어", "WARRIOR");
        classCommandMap.put("화염술사", "FIREMAGE");
        classCommandMap.put("바드", "BARD");
        classCommandMap.put("거너", "GUNNER");
        classCommandMap.put("무투가", "MARTIALARTIST");
        classCommandMap.put("빙결사", "FROST");
        classCommandMap.put("윈더", "WINDREAVER");

        // 무기 이름 매핑 (한글 -> 영어 코드)
        weaponCommandMap.put("활", "BOW");
        weaponCommandMap.put("검", "SWORD");
        weaponCommandMap.put("단검", "BLADE");
        weaponCommandMap.put("대검", "SWORD");
        weaponCommandMap.put("지팡이", "STAFF");
        weaponCommandMap.put("권총", "PISTOL");
        weaponCommandMap.put("하프", "HARP");
        weaponCommandMap.put("건틀릿", "GAUNTLET");
        weaponCommandMap.put("창", "SPEAR");
        weaponCommandMap.put("룬", "RUNE");

        // 방어구 이름 매핑 (한글 -> 영어 코드)
        armorCommandMap.put("투구", "HELMET");
        armorCommandMap.put("갑옷", "CHESTPLATE");
        armorCommandMap.put("바지", "LEGGINGS");
        armorCommandMap.put("신발", "BOOTS");
    }

    // 확인 버튼 패널 생성
    private ItemStack createConfirmPane() {
        ItemStack pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§a계승 확인");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    // 무기 및 방어구에 대한 TradeData 초기화
    private void initializeTradeData() {
        for (String className : classNames) {
            tradeDataMap.put(className, new HashMap<>());

            // 무기 데이터 초기화
            Map<String, Map<String, Map<String, TradeData>>> itemTypeMap = tradeDataMap.get(className);
            itemTypeMap.put("무기", new HashMap<>());
            Map<String, Map<String, TradeData>> weaponItemMap = itemTypeMap.get("무기");

            for (String weaponName : weaponNames) {
                String weaponCommandName = weaponCommandMap.get(weaponName);
                if (weaponCommandName == null) {
                    plugin.getLogger().warning("무기 이름 매핑을 찾을 수 없습니다: " + weaponName);
                    continue;
                }

                weaponItemMap.put(weaponName, new HashMap<>());

                for (int j = 0; j < tiers.length; j++) {
                    String tier = tiers[j];
                    int requiredLevel = requiredLevels[j];
                    // 명령어 포맷 수정: 첫 번째 "SWORD"는 고정된 아이템 타입
                    String command = String.format("mi give SWORD %s_%s_%d %%player%% 1",
                            classCommandMap.get(className),
                            weaponCommandName,
                            j+1); // 0부터 시작하는 등급 번호 (0차는 0)

                    weaponItemMap.get(weaponName).put(tier, new TradeData(requiredLevel, command));
                }
            }

            // 방어구 데이터 초기화
            itemTypeMap.put("방어구", new HashMap<>());
            Map<String, Map<String, TradeData>> armorItemMap = itemTypeMap.get("방어구");

            for (String armorName : armorNames) {
                String armorCommandName = armorCommandMap.get(armorName);
                if (armorCommandName == null) {
                    plugin.getLogger().warning("방어구 이름 매핑을 찾을 수 없습니다: " + armorName);
                    continue;
                }

                armorItemMap.put(armorName, new HashMap<>());

                for (int j = 0; j < tiers.length; j++) {
                    String tier = tiers[j];
                    int requiredLevel = requiredLevels[j];
                    // 명령어 포맷 수정: 첫 번째 "ARMOR"는 고정된 아이템 타입
                    String command = String.format("mi give ARMOR %s_%s_%d %%player%% 1",
                            classCommandMap.get(className),
                            armorCommandName,
                            j+1); // 0부터 시작하는 등급 번호 (0차는 0)

                    armorItemMap.get(armorName).put(tier, new TradeData(requiredLevel, command));
                }
            }
        }
    }

    // Trade GUI 열기
    public void openTradeGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "아이템 계승");
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        if (grayMeta != null) {
            grayMeta.setDisplayName(" ");
            grayPane.setItemMeta(grayMeta);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, grayPane);
        }
        gui.setItem(11, new ItemStack(Material.AIR)); // 아이템 넣는 슬롯
        gui.setItem(15, this.confirmPane); // 확인 버튼
        player.openInventory(gui);
        this.tradeSuccessful = false;
    }

    // 인벤토리 클릭 이벤트 처리
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("아이템 계승")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // 플레이어 인벤토리에서 아이템을 슬롯 11로 이동
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            if (clickedItem != null && clickedItem.getItemMeta() != null) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName.contains("+7")) {
                    event.getView().getTopInventory().setItem(11, clickedItem.clone());
                    player.getInventory().removeItem(clickedItem);
                } else {
                    player.sendMessage("§c강화 수치가 부족합니다. 강화 +7 아이템만 계승할 수 있습니다.");
                }
            }
        }

        // 확인 버튼 클릭 처리
        if (event.getSlot() == 15) {
            ItemStack itemToTrade = event.getInventory().getItem(11);
            if (itemToTrade != null && itemToTrade.getItemMeta() != null) {
                String itemName = itemToTrade.getItemMeta().getDisplayName();

                String className = null;
                String itemType = null;
                String tier = null;
                String itemNameKorean = null;

                // 클래스명 식별
                for (String cls : tradeDataMap.keySet()) {
                    if (itemName.contains(cls)) {
                        className = cls;
                        break;
                    }
                }

                if (className == null) {
                    player.sendMessage("§c계승 조건에 맞지 않는 클래스입니다!");
                    this.tradeSuccessful = false;
                    player.closeInventory();
                    return;
                }

                // 아이템 타입 및 등급 식별
                // 먼저 무기 검사
                outerLoop:
                for (String weaponName : weaponNames) {
                    String weaponCommandName = weaponCommandMap.get(weaponName);
                    if (weaponCommandName == null) continue;

                    for (String t : tiers) {
                        if (itemName.contains(weaponName) && itemName.contains(t)) {
                            itemType = "무기";
                            tier = t;
                            itemNameKorean = weaponName;
                            break outerLoop;
                        }
                    }
                }

                // 무기가 아니면 방어구 검사
                if (itemType == null) {
                    outerLoop:
                    for (String armorName : armorNames) {
                        String armorCommandName = armorCommandMap.get(armorName);
                        if (armorCommandName == null) continue;

                        for (String t : tiers) {
                            if (itemName.contains(armorName) && itemName.contains(t)) {
                                itemType = "방어구";
                                tier = t;
                                itemNameKorean = armorName;
                                break outerLoop;
                            }
                        }
                    }
                }

                if (itemType == null || tier == null || itemNameKorean == null) {
                    player.sendMessage("§c계승 조건에 맞지 않는 아이템입니다!");
                    this.tradeSuccessful = false;
                    player.closeInventory();
                    return;
                }

                // TradeData 조회
                TradeData tradeData = null;
                if (itemType.equals("무기")) {
                    Map<String, Map<String, TradeData>> weaponItemMap = tradeDataMap.get(className).get("무기");
                    if (weaponItemMap != null) {
                        Map<String, TradeData> tierMap = weaponItemMap.get(itemNameKorean);
                        if (tierMap != null) {
                            tradeData = tierMap.get(tier);
                        }
                    }
                } else if (itemType.equals("방어구")) {
                    Map<String, Map<String, TradeData>> armorItemMap = tradeDataMap.get(className).get("방어구");
                    if (armorItemMap != null) {
                        Map<String, TradeData> tierMap = armorItemMap.get(itemNameKorean);
                        if (tierMap != null) {
                            tradeData = tierMap.get(tier);
                        }
                    }
                }

                if (tradeData == null) {
                    player.sendMessage("§c계승 정보를 찾을 수 없습니다!");
                    this.tradeSuccessful = false;
                    player.closeInventory();
                    return;
                }

                // 플레이어 레벨 확인 및 명령어 실행
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
                player.sendMessage("§c유효한 아이템을 선택해주세요.");
            }
        }
    }

    // 인벤토리 닫힘 이벤트 처리
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("아이템 계승")) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            if (!this.tradeSuccessful) {
                ItemStack itemInSlot11 = inventory.getItem(11);
                if (itemInSlot11 != null && itemInSlot11.getType() != Material.AIR)
                    player.getInventory().addItem(itemInSlot11);
            }
        }
    }

    // TradeData 클래스 정의
    private static class TradeData {
        int requiredLevel;
        String command;

        public TradeData(int requiredLevel, String command) {
            this.requiredLevel = requiredLevel;
            this.command = command;
        }
    }
}
