package com.gmail.supergame314.itemtrader;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.UUID;

public class VaultManager {

    String prefix = "§e[§bMZN§e] §e";
    private final JavaPlugin plugin;

    public VaultManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public static Economy economy = null;

    private void setupEconomy() {
        plugin.getLogger().info("setupEconomy");
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vaultが入っていません！");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Can't get vault service");
            return;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Vaultの連携が完了しました。");
    }

    //get balance 所持金を取得
    public double getBal(UUID uuid) {
        return economy.getBalance(Bukkit.getOfflinePlayer(uuid).getPlayer());
    }

    //show balance 所持金をプレイヤーに通知
    public void showBal(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid).getPlayer();
        double money = getBal(uuid);
        Objects.requireNonNull(Objects.requireNonNull(p).getPlayer()).sendMessage(prefix + "あなたの所持金は " + (int) money + "円です");
    }

    //withdraw 金額を引く
    public Boolean withdraw(Player player, double money) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(player.getUniqueId());
        EconomyResponse resp = economy.withdrawPlayer(p, money);
        if (resp.transactionSuccess()) {
            if (p.isOnline()) {
                Objects.requireNonNull(p.getPlayer()).sendMessage(prefix + (int) money + "円支払いました");
            }
            return true;
        }
        return false;
    }

    //deposit 金額を与える
    public Boolean deposit(Player player, double money) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(player.getUniqueId());
        EconomyResponse resp = economy.depositPlayer(p, money);
        if (resp.transactionSuccess()) {
            if (p.isOnline()) {
                Objects.requireNonNull(p.getPlayer()).sendMessage(prefix + (int) money + "円受取りました");
            }
            return true;
        }
        return false;
    }
}