package com.yd.trade;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final Trade plugin;

    public TradeCommand(Trade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            TradeGUI tradeGUI = new TradeGUI(this.plugin);
            tradeGUI.openTradeGUI(player);
        } else {
            sender.sendMessage("Only players can use this command.");
        }
        return true;
    }
}
