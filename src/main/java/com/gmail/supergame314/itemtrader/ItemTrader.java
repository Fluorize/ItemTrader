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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ItemTrader extends JavaPlugin implements Listener {

    public VaultManager vault = new VaultManager(this);


    static String prefix = "§f§l[§2§lI§7§lTrade§f§l]";
    static boolean isEnable = true;

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
            if(args.length<=1){
                sender.sendMessage(prefix+"§2§l TRADEコマンド -- 他のプレイヤーとアイテムを交換します");
                sender.sendMessage(prefix+"§2 >>>使い方 /trade new プレイヤー");
                return true;
            }
            Player target = getServer().getPlayer(args[1]);
            Player p = (Player) sender;
            if(args[0].equals("new")) {
                if (target == null) {
                    sender.sendMessage(prefix + "§c§l プレイヤー、§e§l" + args[1] + "§c§lは見つかりませんでした");
                    return true;
                }
                new MatchSystem(p,target);
                target.sendMessage(prefix + "§e§l" + p.getName() + "§aによってトレードに誘われました。");
            }else if(args[0].equals("acc")){
                MatchSystem ms = MatchSystem.getMatchT(p);
                if(ms!=null){
                    ms.accept();
                    return true;
                }

            }else if(args[0].equals("ref")){
                MatchSystem ms = MatchSystem.getMatchT(p);
                if(ms!=null){
                    ms.refuse();
                    return true;
                }

            }
        }
        return true;
    }




    @Override
    public void onEnable() {
        isEnable = true;
        getServer().getPluginManager().registerEvents(this,this);
        MatchSystem.setItemTrader(this);
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
            ms.clickInv(event);
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

    void askVdContinue(Player p, int diceTotal){
        if(diceTotal<=1000){
            p.sendMessage(prefix+"選択してください！！");
            //TextComponent text=new TextComponent(DataClass.MSGs[2]);
            TextComponent text=new TextComponent(getText("§l§n取引に応じる ","","/trade acc",ChatColor.GREEN));
            text.addExtra(getText(" §l§n断る","続けます","/trade ref",ChatColor.RED));
            p.spigot().sendMessage(text);
        }
    }

    static ItemStack getItem(Material m,int amount,String name,String... lore){
        ItemStack i =new ItemStack(m,amount);
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        i.setItemMeta(meta);
        return i;
    }

}
