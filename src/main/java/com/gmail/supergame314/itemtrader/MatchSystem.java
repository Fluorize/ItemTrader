package com.gmail.supergame314.itemtrader;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

import static com.gmail.supergame314.itemtrader.ItemTrader.*;



public class MatchSystem {

    static private List<MatchSystem> matchList = new ArrayList<>();

    private List<Player> chat = new ArrayList<>();
    private Player from;
    private Player to;
    private double fromMoney;
    private double toMoney;

    /**
     *
     * processF is process of player "from"
     * processT is process of player "to"
     *
     * 0:何もしてない状態
     * 1:招待に応じた状態
     * 2:取引内容を確定した状態
     * 3:取引内容を承認した状態
     * 4:終了
     *
     */

    private int processF = 1;
    private int processT = 0;

    private Inventory inventory;
    static private ItemTrader it;


    static public void setItemTrader(ItemTrader it){
        MatchSystem.it = it;
    }

    static public MatchSystem getMatchF(Player from){
        for(MatchSystem m:matchList){
            if(m.from == from) {
                return m;
            }
        }
        return null;
    }

    static public MatchSystem getMatchT(Player to){
        for(MatchSystem m:matchList){
            if(m.to == to) {
                return m;
            }
        }
        return null;
    }

    static public boolean containsFrom(Player p){
        return getMatchF(p)!=null;
    }

    static public boolean containsTo(Player p){
        return getMatchT(p)!=null;
    }

    static public MatchSystem getMatchI(Inventory i){
        for(MatchSystem m:matchList){
            if(m.inventory == i) {
                return m;
            }
        }
        return null;
    }


    /**
     * please create new instance of this when a player invites another player.
     *
     * @param from The player who invited
     * @param to   Invited player
     */


    public MatchSystem(Player from,Player to){
        matchList.add(this);
        this.from = from;
        this.to = to;
        new BukkitRunnable(){
            @Override
            public void run(){
                MatchSystem ms = getMatchF(from);
                if(ms == null)return;
                if(ms.processT!=0)return;
                from.sendMessage(prefix+"§c§l取引の有効期限が切れました");
                to.sendMessage(prefix+"§c§l取引の有効期限が切れました");
                matchList.remove(getMatchF(from));
            }
        }.runTaskLater(it,20*10);
    }

    public void accept(){
        processT = 1;
        inventory = Bukkit.createInventory(null,54,"[ITrader] 取引");
        inventory.setItem(27,getItem(Material.GOLD_INGOT,1,Component.text("§6§lお金"),Component.text("§7クリックしてチャットで金額を入力します")));
        inventory.setItem(35,getItem(Material.GOLD_INGOT,1,Component.text("§6§lお金"),Component.text("§7クリックしてチャットで金額を入力します")));
        gui(processF,processT);
        for (int i = 4;i<54;i+=9){
            inventory.setItem(i,getItem(Material.WHITE_STAINED_GLASS_PANE,1,Component.text("")));
        }
        ItemStack i = getItem(Material.PLAYER_HEAD,1,Component.text("§e§l"+from.getName()));
        SkullMeta m = (SkullMeta) i.getItemMeta();
        m.setOwningPlayer(from);
        i.setItemMeta(m);
        inventory.setItem(30,i);
        i = getItem(Material.PLAYER_HEAD,1,Component.text("§e§l"+to.getName()));
        m = (SkullMeta) i.getItemMeta();
        m.setOwningPlayer(to);
        i.setItemMeta(m);
        inventory.setItem(32,i);
        from.openInventory(inventory);
        to.openInventory(inventory);
    }

    public void refuse(){
        matchList.remove(this);
        from.sendMessage(prefix+"§c§l取引を拒否されました");
        to.sendMessage(prefix+"§c§l取引を拒否しました");
    }

    public void clickInv(InventoryClickEvent event){
        Player p = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        if(27<=slot && slot<36){
            if(slot == 27 && p==from) {
                setChatting(p);
                p.sendMessage(prefix+"§a§l金額を入力してください");
                p.closeInventory();
            }
            if(slot == 35 && p==to) {
                setChatting(p);
                p.sendMessage(prefix+"§a§l金額を入力してください");
                p.closeInventory();
            }
            event.setCancelled(true);
            return;
        }
        if(slot%9==4){
            event.setCancelled(true);
            return;
        }
        if(slot%9<4){
            //左側
            if(p!=from){
                p.sendMessage(prefix+"§c§lそこはあなたのスペースではありません");
                event.setCancelled(true);
                return;
            }
            if(36<=slot) {
                event.setCancelled(true);
                if (processF == 1) {
                    processF=2;
                }else if (processF == 2) {
                    if(processT>=2) {
                        if (slot % 9 < 2) {
                            processF = 3;
                            if (processT == 3) {
                                finish();
                                return;
                            }
                        } else {
                            //承認されない
                            processT=processF=1;
                            to.sendMessage(prefix+"取引相手が拒否しましたヽ(ﾟ∀｡)ﾉ");
                        }
                    }else{
                        processF=1;
                    }
                }
                gui(processF,processT);
                from.updateInventory();
                to.updateInventory();
            }else if(processF != 1){
                event.setCancelled(true);
            }
        }else{
            //右側
            if(p!=to){
                p.sendMessage(prefix+"§c§lそこはあなたのスペースではありません");
                event.setCancelled(true);
                return;
            }
            if(36<=slot) {
                event.setCancelled(true);
                if (processT == 1) {
                    processT=2;
                }else if (processT == 2) {
                    if(processF>=2) {
                        if (slot % 9 >= 7) {
                            processT = 3;
                            if (processF == 3) {
                                finish();
                                return;
                            }
                        } else {
                            //承認されない
                            processT=processF=1;
                            to.sendMessage(prefix+"取引相手が拒否しましたヽ(ﾟ∀｡)ﾉ");
                        }
                    }else{
                        processT=1;
                    }
                }
                gui(processF,processT);
                from.updateInventory();
                to.updateInventory();
            }else if(processT != 1){
                event.setCancelled(true);
            }
        }

    }


    private void gui(int processF,int processT){
        switch (processF){
            case 1:
                for (int i = 36;i<40;i++){
                    inventory.setItem(i,getItem(Material.RED_STAINED_GLASS_PANE,1,Component.text("§c§l取引内容変更中"),Component.text("§7完了したらここをクリックしてください")));
                }
                for (int i = 45;i<49;i++){
                    inventory.setItem(i,getItem(Material.RED_STAINED_GLASS_PANE,1,Component.text("§c§l取引内容変更中"),Component.text("§7完了したらここをクリックしてください")));
                }
                break;
            case 2:
                if(processT>=2) {
                    for (int i = 36; i < 40;i++) {
                        if (i % 9 <= 1)
                            inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認する"), Component.text("§7これでいいですか？")));
                        else
                            inventory.setItem(i, getItem(Material.YELLOW_STAINED_GLASS_PANE, 1, Component.text("§a§l承認する"), Component.text("§7これでいいですか？")));
                    }
                    for (int i = 45; i < 49;i++) {
                        if (i % 9 <= 1)
                            inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認する"), Component.text("§7これでいいですか？")));
                        else
                            inventory.setItem(i, getItem(Material.YELLOW_STAINED_GLASS_PANE, 1, Component.text("§c§l承認しない"), Component.text("§7これでいいですか？")));
                    }
                }else{
                    for (int i = 36; i < 40; i++) {
                        inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§lOK!"), Component.text("§7完了(クリックしてキャンセル)")));
                    }
                    for (int i = 45; i < 49; i++) {
                        inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§lOK!"), Component.text("§7完了(クリックしてキャンセル)")));
                    }
                }
                break;
            case 3:
                for (int i = 36; i < 40; i++) {
                    inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認!"), Component.text("§7取引を認めました")));
                }
                for (int i = 45; i < 49; i++) {
                    inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認!"), Component.text("§7取引を認めました")));
                }
                break;
        }
        switch (processT){
            case 1:
                for (int i = 41;i<45;i++){
                    inventory.setItem(i,getItem(Material.RED_STAINED_GLASS_PANE,1,Component.text("§c§l取引内容変更中"),Component.text("§7完了したらここをクリックしてください")));
                }
                for (int i = 50;i<54;i++){
                    inventory.setItem(i,getItem(Material.RED_STAINED_GLASS_PANE,1,Component.text("§c§l取引内容変更中"),Component.text("§7完了したらここをクリックしてください")));
                }
                break;
            case 2:
                if(processF>=2) {
                    for (int i = 41; i < 45; i++) {
                        if (i % 9 <= 6)
                            inventory.setItem(i, getItem(Material.YELLOW_STAINED_GLASS_PANE, 1, Component.text("§c§l承認しない"), Component.text("§7これでいいですか？")));
                        else
                            inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認する"), Component.text("§7これでいいですか？")));
                    }
                    for (int i = 50; i < 54; i++) {
                        if (i % 9 <= 6)
                            inventory.setItem(i, getItem(Material.YELLOW_STAINED_GLASS_PANE, 1,  Component.text("§c§l承認しない"), Component.text("§7これでいいですか？")));
                        else
                            inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認する"), Component.text("§7これでいいですか？")));
                    }
                }else {
                    for (int i = 41; i < 45; i++) {
                        inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§lOK!"), Component.text("§7完了(クリックしてキャンセル)")));
                    }
                    for (int i = 50; i < 54; i++) {
                        inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§lOK!"), Component.text("§7完了(クリックしてキャンセル)")));
                    }
                }
                break;
            case 3:
                for (int i = 41; i < 45; i++) {
                    inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認!"), Component.text("§7取引を認めました")));
                }
                for (int i = 50; i < 54; i++) {
                    inventory.setItem(i, getItem(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("§a§l承認!"), Component.text("§7取引を認めました")));
                }
                break;
        }


    }

    public void setChatting(Player p){
        chat.add(p);
    }


    public boolean isChatting(Player p){
        return chat.contains(p);
    }

    public void chat(Player p, Component msg){
        if(!isChatting(p))return;
        final MatchSystem ms1 = getMatchF(p);
        try {
            if (ms1 != null) {
                fromMoney = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(msg));
                if(fromMoney>it.vault.getBal(p.getUniqueId())) {
                    p.sendMessage(prefix+"§c§l必要なお金を持っていません！");
                    return;
                }
                if(fromMoney != 0)
                    inventory.setItem(28,getItem(Material.GOLD_NUGGET,1,Component.text("§6§l"+msg+"円"),Component.text("§7提示しています")));
                else
                    inventory.clear(28);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(ms1.inventory);
                    }
                }.runTask(it);
                chat.remove(p);
                return;
            }
            final MatchSystem ms2 = getMatchT(p);
            if (ms2 != null) {
                toMoney = Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(msg));
                if(fromMoney>it.vault.getBal(p.getUniqueId())) {
                    p.sendMessage(prefix+"§c§l必要なお金を持っていません！");
                    return;
                }
                if(toMoney != 0)
                    inventory.setItem(33,getItem(Material.GOLD_NUGGET,1,Component.text("§6§l"+msg+"円"),Component.text("§7提示しています")));
                else
                    inventory.clear(33);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        p.openInventory(ms2.inventory);
                    }
                }.runTask(it);
                chat.remove(p);
            }
        }catch (NumberFormatException e){
            p.sendMessage(prefix+"§c§l値が不正です！整数を入力してください");
        }
    }



    public void cancelMatch(){
        matchList.remove(this);
        from.closeInventory();
        to.closeInventory();
        from.sendMessage(prefix+"§c§l片方がインベントリを閉じたため取引が中断されました");
        to.sendMessage(prefix+"§c§l片方がインベントリを閉じたため取引が中断されました");
        for(int i =0;i<27;i++){
            if(inventory.getItem(i)==null)continue;
            if(i%9>4){
                to.getInventory().addItem(inventory.getItem(i));
            }
            if(i%9<4){
                from.getInventory().addItem(inventory.getItem(i));
            }
        }
    }



    private void finish(){
        for(int i =0;i<27;i++){
            if(inventory.getItem(i)==null)continue;
            if(i%9>4){
                from.getInventory().addItem(inventory.getItem(i));
            }
            if(i%9<4){
                to.getInventory().addItem(inventory.getItem(i));
            }
        }
        it.vault.withdraw(from,fromMoney);
        it.vault.deposit(to,fromMoney);
        it.vault.withdraw(to,toMoney);
        it.vault.deposit(from,toMoney);
        from.sendMessage(prefix+"§a§l取引が成立しました！");
        to.sendMessage(prefix+"§a§l取引が成立しました！");
        matchList.remove(this);
        from.closeInventory();
        to.closeInventory();
    }
}
