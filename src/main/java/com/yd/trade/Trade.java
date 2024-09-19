package com.yd.trade;

import org.bukkit.plugin.java.JavaPlugin;

public final class Trade extends JavaPlugin {
    private ConfigManager configManager;

    public void onEnable() {
        this.configManager = new ConfigManager(this);
        if (getCommand("trade") != null) {
            getCommand("trade").setExecutor(new TradeCommand(this));
        } else {
            getLogger().severe("Command 'trade' not found in plugin.yml. Check your plugin.yml configuration.");
        }
        getServer().getPluginManager().registerEvents(new TradeGUI(this), this);
    }
}