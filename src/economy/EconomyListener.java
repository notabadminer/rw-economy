/*
 * Copyright (C) 2016 notabadminer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package economy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
import net.risingworld.api.events.Threading;
import net.risingworld.api.events.player.PlayerCommandEvent;
import net.risingworld.api.events.player.PlayerConnectEvent;
import net.risingworld.api.objects.Inventory;
import net.risingworld.api.objects.Item;
import net.risingworld.api.objects.Player;

/**
 *
 * @author notabadminer
 */
public class EconomyListener implements Listener {

    private final Economy plugin;

    public EconomyListener(Economy plugin) {
        this.plugin = plugin;
    }
    
    @EventMethod(Threading.Sync)
    public void onPlayerConnect(PlayerConnectEvent event) {
        String playername = event.getPlayer().getName();
        
        if (event.isNewPlayer()) {
            plugin.database.executeUpdate("REPLACE INTO `Economy` (`PlayerName`, `Balance`) VALUES ('" + playername + "',0)");
        }
    }

    @EventMethod
    public void onCommand(PlayerCommandEvent event) {
        Player player = event.getPlayer();
        String command = event.getCommand();
        String playername = player.getName();
        String lang = player.getLanguage();

        if (command.equals("/ebalance")) {
            long balance = plugin.wallet.accountBalance(playername);
            DecimalFormat formatter = new DecimalFormat("#,###,###,###,###,###,###");
            player.sendTextMessage("[#00FF00]" + LangSupport.getLocalTranslation("economy.message.balance", lang) + formatter.format(balance) + " "  + LangSupport.getLocalTranslation("economy.label.coin", lang));
        }

        if (command.startsWith("/egivecoins") && player.isAdmin()) {
            String args[] = command.split(" ");

            if (args.length == 3) {
                Long amount;
                try {
                    amount = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.invalid", lang));
                    return;
                }
                if (plugin.getServer().getPlayer(args[1]) == null) {
                    player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.notfound", lang));
                    return;
                }
                if (plugin.wallet.creditAccount(args[1], amount, true)) {
                    plugin.wallet.creditAccount(args[1], amount, false);
                    player.sendTextMessage("[#00FF00]" + LangSupport.getLocalTranslation("economy.message.gave", lang) + args[1] + " " +  amount + " "  + LangSupport.getLocalTranslation("economy.label.coin", lang));
                } else {
                    player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.command", lang));
                }
            } else {
                player.sendTextMessage("[#00FF00]" + LangSupport.getLocalTranslation("command.givecoins.help", lang));
            }
        }

        if (command.equals("/eprice")) {
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
                return;
            }
            String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
            } else {
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemAttribute = getItemAttribute(testItem.toString());
                String itemName = testItem.getName();
                try (ResultSet result = plugin.database.executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
                    int itemPrice = result.getInt("ItemPrice");
                    if (itemPrice > 0) {
                        player.sendTextMessage("[#00FF00]" + itemName + ": " + itemPrice + " "  + LangSupport.getLocalTranslation("economy.label.coin", lang));
                    } else {
                        player.sendTextMessage("[#FF0000]" + itemName + " " + LangSupport.getLocalTranslation("economy.error.noprice", lang));
                    }
                } catch (SQLException e) {
                    player.sendTextMessage("[#FF0000]" + itemName + " " + LangSupport.getLocalTranslation("economy.error.noprice", lang));
                }
            }
        }

        if (command.startsWith("/esell")) {
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", player.getLanguage()));
                return;
            }
                String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", player.getLanguage()));
            } else {
                String itemName = testItem.getName();
                int amount = testItem.getStacksize();
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemAttribute = getItemAttribute(testItem.toString());
                int stackValue;
                String[] args = command.split(" ");
                if (args.length > 1) {
                    try {
                       amount = Math.min(Integer.parseInt(args[1]), testItem.getStacksize());
                    } catch (NumberFormatException e) {
                        player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.invalid", lang));
                        return;
                    }
                }
                int itemPrice = getItemPrice(itemID, itemVariation, itemAttribute);
                if (itemPrice > 0) {
                    stackValue = amount * itemPrice;
                } else {
                    player.sendTextMessage("[#FF0000]"+ itemName + " " + LangSupport.getLocalTranslation("economy.error.noprice", lang));
                    return;
                }
               
                if (plugin.wallet.creditAccount(playername, stackValue, true)) {
                    plugin.wallet.creditAccount(playername, stackValue, false);
                    player.getInventory().removeItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot, amount);
                    player.sendTextMessage("[#00FF00]" + LangSupport.getLocalTranslation("economy.message.sold", lang) + amount + " " + testItem.getName());
                } else {
                    player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.command", lang));
                }
            }
        }
        
         if (command.startsWith("/ebuy")) {
             Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
                return;
            }
            //we can't handle buying objects due to the attribute not being accessible
            if (testItem.getName().matches("objectkit")) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.objectkit", lang));
                return;
            }
                String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
            } else {
                int amount = 1;
                int itemVariation = testItem.getVariation();
                short itemID = testItem.getTypeID();
                String itemName = testItem.getName();
                String itemAttribute = getItemAttribute(testItem.toString());
                String[] args = command.split(" ");
                if (args.length > 1) {
                    try {
                       amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.invalid", lang));
                        return;
                    }
                }
                
                int itemPrice = getItemPrice(itemID, itemVariation, itemAttribute);
                if (itemPrice > 0) {
                    if (plugin.wallet.debitAccount(playername, amount * itemPrice, true)) {
                        if (testItem.getStacksize() < testItem.getMaxStacksize()) {
                            amount = Math.min(testItem.getMaxStacksize() - testItem.getStacksize(), amount);
                        }
                        amount = Math.min(amount, testItem.getMaxStacksize());
                        Item invAdd = player.getInventory().insertNewItem(itemID, itemVariation, amount);
                        player.sendTextMessage("[#00FF00]" + LangSupport.getLocalTranslation("economy.message.bought", lang) + amount + " " + invAdd.getName());
                        plugin.wallet.debitAccount(playername, amount * itemPrice, false);
                } else {
                        player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.notenough", lang));
                    }
                } else {
                    player.sendTextMessage("[#FF0000]" + itemName + " " + LangSupport.getLocalTranslation("economy.error.noprice", lang));
                    return;
                }
            }
         }

        if (command.equals("/economy import") && player.isAdmin()) {
            plugin.fileutil.importPriceData();
        }

        if (command.equals("/economy export") && player.isAdmin()) {
            plugin.fileutil.exportPriceData();
        }
        
        if (command.startsWith("/economy set") && player.isAdmin()) {
            String[] args = command.split(" ");
            int itemPrice = 0;
            if (args.length > 2) {
                try {
                   itemPrice = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.invalid", lang));
                    return;
                }
            } else {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("command.economy.set.help", lang));
            }
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
                return;
            }
            String test = testItem.toString();
            String itemAttribute = getItemAttribute(testItem.toString());
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]" + LangSupport.getLocalTranslation("economy.error.emptyhand", lang));
            } else {
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemName = testItem.getName();
                plugin.database.executeUpdate("REPLACE INTO Pricelist (ItemID, ItemVariation, ItemAttribute, ItemName, ItemPrice) VALUES ( '" + itemID + "', '" + itemVariation + "','" + itemAttribute + "','" + itemName + "','" + itemPrice + "')");
                player.sendTextMessage("[#00FF00]" + itemName + ": " + itemPrice + " " + LangSupport.getLocalTranslation("economy.label.coin", lang));             
            }
        }
    }
    
    private String getItemAttribute(String itemString) {
        String tempString = itemString.split("ute: ")[1];
        String attribute = tempString.split(",")[0];
        return attribute;
    }
    
    private int getItemPrice(int itemID, int itemVariation, String itemAttribute) {
        try (ResultSet result = plugin.database.executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
            int itemPrice = result.getInt("ItemPrice");
            return itemPrice;
        } catch (SQLException e) {
            if (itemVariation > 0) {
                //try variation 0 if specific variation isn't priced
                try (ResultSet result = plugin.database.executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
                    int itemPrice = result.getInt("ItemPrice");
                    return itemPrice;
                } catch (SQLException ex) {
                    return 0;
                }
            }
            return 0;
        }
    }
}
