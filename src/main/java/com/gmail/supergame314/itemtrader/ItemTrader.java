package com.gmail.supergame314.itemtrader;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class ItemTrader extends JavaPlugin implements Listener {

    public VaultManager vault;


    static String prefix = "§f§l[§2§lI§7§lTrade§f§l]";
    static boolean isEnable = true;
    static int ver;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!isEnable){
            sender.sendMessage(prefix+"§c§l ITradeシステム停止中です");
            return true;
        }
        if(command.getName().equals("trade")){
            if(!(sender instanceof Player)) {
                sender.sendMessage(prefix+"§c§l このコマンドはプレイヤー専用です。");
                return true;
            }
            Player p = (Player) sender;
            if(args.length != 0 && args[0].equals("acc")){
                MatchSystem ms = MatchSystem.getMatchT(p);
                if(ms!=null){
                    ms.accept();
                    return true;
                }

            }else if(args.length != 0 && args[0].equals("ref")){
                MatchSystem ms = MatchSystem.getMatchT(p);
                if(ms!=null){
                    ms.refuse();
                    return true;
                }
            }
            if(args.length<=1){
                sender.sendMessage(prefix+"§2§l================================================");
                sender.sendMessage(prefix+"§2§l TRADEコマンド -- 他のプレイヤーとアイテムを交換します");
                sender.sendMessage(prefix+"§2 >>>使い方 /trade new プレイヤー");
                if(sender.isPermissionSet("itemTrader.aboutPermission")){
                    sender.sendMessage(prefix+"§7 ---権限 itemTrader.canTrade");
                }
                sender.sendMessage(prefix+"§2§l================================================");
                return true;
            }
            Player target = getServer().getPlayer(args[1]);
            if(args[0].equals("new")) {
                if (target == null) {
                    sender.sendMessage(prefix + "§c§l プレイヤー、§e§l" + args[1] + "§c§lは見つかりませんでした");
                    return true;
                }
                if(MatchSystem.getMatchF(p)!=null){
                    sender.sendMessage(prefix+" §c§lあなたは他の取引を実行中です！");
                    return true;
                }
                if(MatchSystem.getMatchT(target)!=null){
                    sender.sendMessage(prefix+" §c§lその人は他の取引を実行中です！");
                    return true;
                }
                if(p==target){
                    sender.sendMessage(prefix+" §c§l自分と取引することはできません！");
                    return true;
                }
                new MatchSystem(p,target);
                ask(p,target);
                sender.sendMessage(prefix+" §a§l取引に誘いました。");
            }
        }
        return true;
    }




    @Override
    public void onEnable() {
        isEnable = true;
        vault = new VaultManager(this);
        getServer().getPluginManager().registerEvents(this,this);
        MatchSystem.setItemTrader(this);
        ver = Integer.parseInt(getServer().getClass().getName().split("\\.")[3].split("_")[1]);
        if(ver<=12){
            isEnable=false;
            getLogger().warning("This plugin is for 1.13 or later versions.");
            getServer().getPluginManager().disablePlugin(this);
        }
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void click(InventoryClickEvent event){
        MatchSystem ms = MatchSystem.getMatchI(event.getClickedInventory());
        if (ms != null){
            if(event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            ms.clickInv(event);
        }
    }

    @EventHandler
    public void closeInv(InventoryCloseEvent event){
        MatchSystem ms = MatchSystem.getMatchI(event.getInventory());
        if(ms != null){
            if(!ms.isChatting((Player) event.getPlayer()))
                ms.cancelMatch();
                //event.getPlayer().sendMessage("aaaa");
        }
    }

    @EventHandler
    public void chat(AsyncChatEvent event) {
        MatchSystem ms = MatchSystem.getMatchF(event.getPlayer());
        if (ms != null) {
            event.setCancelled(true);
            ms.chat(event.getPlayer(), event.message());
        } else {
            ms = MatchSystem.getMatchT(event.getPlayer());
            if (ms != null) {
                event.setCancelled(true);
                ms.chat(event.getPlayer(), event.message());
            }
        }
    }

    // https://www.spigotmc.org/threads/api-for-parsing-a-text-component.512539/
    void ask(Player p,Player target){
        Component text =
                MiniMessage.miniMessage().deserialize("<gold><bold>" + p.getName() + "に取引に誘われました。<reset> <green><bold><underlined><hover:show_text:'クリックで応じる'><click:run_command:/trade acc>[応じる]<reset> <red><bold><underlined><hover:show_text:'クリックで断る'><click:run_command:/trade ref>[断る]<reset>");
        target.sendMessage(text);
    }

    static ItemStack getItem(Material m,int amount,Component name,Component... lore){
        ItemStack i =new ItemStack(m,amount);
        ItemMeta meta = i.getItemMeta();
        meta.displayName(name);
        meta.lore(Arrays.asList(lore));
        i.setItemMeta(meta);
        return i;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(!command.getName().equals("trade")){
            return null;
        }
        if(args.length==0){
            return Arrays.asList("new","acc","ref");
        }
        if(args.length<=1) {
            List<String> l = new ArrayList<>();
            if ("new".startsWith(args[0])) {
                l.add("new");
            }
            if ("acc".startsWith(args[0])) {
                l.add("acc");
            }
            if ("ref".startsWith(args[0])) {
                l.add("ref");
            }
            return l;
        }else{
            return null;
        }
    }
}
