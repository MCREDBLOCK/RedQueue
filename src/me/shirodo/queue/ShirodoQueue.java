package me.shirodo.queue;


import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import me.shirodo.commands.COMMAND_liste;
import me.shirodo.discord.MessageListener;
import me.shirodo.queue.data.DataInspector;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import me.shirodo.queue.data.Lists;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.security.auth.login.LoginException;

import static me.shirodo.discord.MessageListener.botEnable;

public class ShirodoQueue extends Plugin implements Listener{

    private int advert = 0;
    public static String tellMsg;
    public static String prefix_Info = ChatColor.DARK_RED+""+ChatColor.BOLD+"> "+ChatColor.RESET;
    public static String prefix_Warn = ChatColor.GOLD+""+ChatColor.BOLD+"> "+ChatColor.RESET;
    public static String prefix_Danger = ChatColor.RED+""+ChatColor.BOLD+"> "+ChatColor.RESET;
    public static String prefix_Error = ChatColor.DARK_RED+""+ChatColor.BOLD+"> "+ChatColor.RESET;
    public static String prefix_Success = ChatColor.DARK_GREEN+""+ChatColor.BOLD+"> "+ChatColor.RESET;

    @Override
    public void onEnable(){
        try {
            botEnable();
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, e.toString(), prefix_Danger);
        }
        RegisterCommands();
        getProxy().getPluginManager().registerListener(this, this);
        ProxyServer.getInstance().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                int i = 0;
                if (!Lists.queue.isEmpty()) {
                    for (String pname : Lists.queue) {
                        i++;
                        getProxy().getPlayer(pname).sendMessage(new TextComponent(prefix_Info+"Your position in the queue is "+i+Lists.queue.size()));
                        if (advert == 6) {
                            getProxy().getPlayer(pname).sendMessage(new TextComponent(prefix_Warn+"You can buy a rank on store.redblock6.com to get a higher priority in the queue"));
                            advert = 0;
                        }
                        advert++;
                    }
                }
                else{
                    getLogger().log(Level.INFO, "S\u0131ra tamamen bo\u015f. Mesaj g\u00f6nderilecek \u00fcye yok.", prefix_Info);
                }
                int j = 0;
                if (!Lists.prioQueue.isEmpty()) {
                    for (String pname : Lists.prioQueue) {
                        j++;
                        getProxy().getPlayer(pname).sendMessage(new TextComponent(prefix_Warn+"Your queue priority is "+ChatColor.GOLD+j+Lists.prioQueue.size()));
                    }
                }
                else{
                    getLogger().log(Level.INFO, "The queue is empty. (No players to send a message to.)", prefix_Info);
                }
                sendPlayerToGame();
            }
        }, 20, 20, TimeUnit.SECONDS);
    }
    @Override
    public void onDisable(){

    }
    /**
     *
     */
    public void sendPlayerToGame(){
        ProxiedPlayer prioPlayer = null;
        ProxiedPlayer player = null;
        if (!Lists.queue.isEmpty()) {
            String pl = Lists.queue.get(0);
            player = getProxy().getPlayer(pl);
            DataInspector.removeFromQueue(pl);
        }
        if (!Lists.prioQueue.isEmpty()) {
            String prpl = Lists.prioQueue.get(0);
            prioPlayer = getProxy().getPlayer(prpl);
            DataInspector.removeFromQueue(prpl);
        }

        ServerInfo anasunucu = ProxyServer.getInstance().getServerInfo("hub");
        if (prioPlayer !=null) {
            prioPlayer.connect(anasunucu);
            prioPlayer.sendMessage(new TextComponent(ChatColor.GOLD +""+ ChatColor.BOLD+"Sending you to hub"));
        }
        if (player!=null) {
            player.connect(anasunucu);
            player.sendMessage(new TextComponent(ChatColor.BOLD+"Sending you to hub"));
        }

    }
    public static void getMessage(String message){
        tellMsg = message;
    }
    public void tellConsole(){
        if (!tellMsg.isEmpty()) {
            getLogger().info(tellMsg);
            getMessage(null);
        }
    }
    public void tellLogger(String msg){
        getLogger().info(msg);
    }

    /*+Events+*/
    @EventHandler
    public void onPostLogin(PostLoginEvent joined) {
        try{
            if (joined.getPlayer().hasPermission("shirodo.queue.priority")) {

                DataInspector.toPriority(joined.getPlayer().getName());
            }
            else{
                DataInspector.toQueue(joined.getPlayer().getName());
            }
        }
        catch(NullPointerException ex){
            tellLogger("HATA, NULLPOINTEREXCEPTION : " + joined);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent quit){
        DataInspector.removeFromQueue(quit.getPlayer().getName());
    }
    /*-Events-*/

    private void RegisterCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new COMMAND_liste("liste"));
        getLogger().info(prefix_Info + "Commands registered!");
    }
}