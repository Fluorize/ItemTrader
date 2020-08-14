package com.gmail.supergame314.itemtrader;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
                    sender.sendMessage(prefix+" §c§l自分と取引することはできません");
                    return true;
                }
                new MatchSystem(p,target);
                ask(p,target);
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
    public void chat(AsyncPlayerChatEvent event){
        MatchSystem ms = MatchSystem.getMatchF(event.getPlayer());
        if(ms != null){
            event.setCancelled(true);
            ms.chat(event.getPlayer(),event.getMessage());
        }else {
            ms = MatchSystem.getMatchT(event.getPlayer());
            if (ms != null) {
                event.setCancelled(true);
                ms.chat(event.getPlayer(),event.getMessage());
            }
        }
    }


    public static TextComponent getText(String text, String hoverText, String command, ChatColor color){
        TextComponent tc = new TextComponent(text);
        if(color!=null)
            tc.setColor(color);
        if(command!=null)
            tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,command));
        if(hoverText!=null)
            tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder(hoverText).create()));
        return tc;
    }

    void ask(Player p,Player target){
        TextComponent text=new TextComponent(prefix + "§e§l" + p.getName() + "§aに取引に誘われました。");
        text.addExtra(getText("§l§n[応じる] ","取引する","/trade acc",ChatColor.GREEN));
        text.addExtra(getText(" §l§n[断る]","取引しない","/trade ref",ChatColor.RED));
        target.spigot().sendMessage(text);
    }

    static ItemStack getItem(Material m,int amount,String name,String... lore){
        ItemStack i =new ItemStack(m,amount);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
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
