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
import net.risingworld.api.database.WorldDatabase;
import net.risingworld.api.events.EventMethod;
import net.risingworld.api.events.Listener;
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
    
    @EventMethod
    public void onPlayerConnect(PlayerConnectEvent event) {
        WorldDatabase database = plugin.getWorldDatabase();
        String playername = event.getPlayer().getName();

        if (event.isNewPlayer()) {
            try {
                database.executeQuery("INSERT INTO Economy (PlayerName, Balance) VALUES ('" + playername + "',0); END");
            } catch (SQLException ex) {
                System.out.println("Error adding player to Economy table");
            }
        }
    }

    @EventMethod
    public void onCommand(PlayerCommandEvent event) {
        Player player = event.getPlayer();
        String command = event.getCommand();
        String playername = player.getName();

        if (command.equals("/balance")) {
            long balance = plugin.wallet.accountBalance(playername);
            player.sendTextMessage("[#00FF00]Balance: " + balance + " Coins.");
        }

        if (command.startsWith("/givecoins") && player.isAdmin()) {
            String args[] = command.split(" ");

            if (args.length == 3) {
                Long amount = 0L;
                try {
                    amount = Long.parseLong(args[2]);
                } catch (NumberFormatException e) {
                    player.sendTextMessage("[#FF0000]Invalid amount!");
                    return;
                }
                if (plugin.wallet.creditAccount(playername, amount, true)) {
                    plugin.wallet.creditAccount(playername, amount, false);
                    player.sendTextMessage("[#00FF00]Gave " + playername + " " +  amount + " Coins");
                } else {
                    player.sendTextMessage("[#FF0000]Command failed");
                }
            } else {
                player.sendTextMessage("[#FF0000]Command usage: /givecoins <playername> <amount>");
            }
        }

        if (command.equals("/price")) {
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]No item in hand.");
                return;
            }
            String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]No item in hand.");
            } else {
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemAttribute = getItemAttribute(testItem.toString());
                String itemName = testItem.getName();
                try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
                    int itemPrice = result.getInt("ItemPrice");
                    if (itemPrice > 0) {
                        player.sendTextMessage("[#00FF00]" + itemName + " is " + itemPrice + " Coins.");
                    } else {
                        player.sendTextMessage("[#FF0000]" + itemName + " has no price");
                    }
                } catch (SQLException e) {
                    player.sendTextMessage("[#FF0000]SQL Error. Item not found.");
                }
            }
        }

        if (command.startsWith("/sell")) {
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]No item in hand.");
                return;
            }
                String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]No item in hand.");
            } else {
                int amount = testItem.getStacksize();
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemAttribute = getItemAttribute(testItem.toString());
                int stackValue = 0;
                String[] args = command.split(" ");
                if (args.length > 1) {
                    try {
                       amount = Math.min(Integer.parseInt(args[1]), testItem.getStacksize());
                    } catch (NumberFormatException e) {
                        player.sendTextMessage("[#FF0000]Invalid amount!");
                        return;
                    }
                }
                try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
                    int itemPrice = result.getInt("ItemPrice");
                    if (itemPrice > 0) {
                        stackValue = amount * itemPrice;
                    } else {
                        player.sendTextMessage("[#FF0000]Item has no price");
                        return;
                    }
                } catch (SQLException e) {
                    player.sendTextMessage("[#FF0000]SQL Error. Item not found.");
                    return;
                }
                if (plugin.wallet.creditAccount(playername, stackValue, true)) {
                    plugin.wallet.creditAccount(playername, stackValue, false);
                    player.getInventory().removeItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot, amount);
                    player.sendTextMessage("[#00FF00]Sold " + amount + " " + testItem.getName());
                }
            }
        }
        
         if (command.startsWith("/buy")) {
             Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]No item in hand.");
                return;
            }
            //we can't handle buying objects due to the attribute not being accessible
            if (testItem.getName().matches("objectkit")) {
                player.sendTextMessage("[#FF0000]This item cannot be bought");
                return;
            }
                String test = testItem.toString();
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]No item in hand.");
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
                        player.sendTextMessage("[#FF0000]Invalid amount!");
                        return;
                    }
                }
                 try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "' AND ItemAttribute='" + itemAttribute + "'")) {
                    int itemPrice = result.getInt("ItemPrice");
                    if (itemPrice > 0) {
                        if (plugin.wallet.debitAccount(playername, amount * itemPrice, true)) {
                            if (testItem.getStacksize() < testItem.getMaxStacksize()) {
                                amount = Math.min(testItem.getMaxStacksize() - testItem.getStacksize(), amount);
                                player.sendTextMessage("[#FF0000]Modified amount: " + amount);
                            }
                            amount = Math.min(amount, testItem.getMaxStacksize());
                            Item invAdd = player.getInventory().insertNewItem(itemID, itemVariation, amount);
                            player.sendTextMessage("[#00FF00]Bought " + amount + " " + invAdd.getName());
                            plugin.wallet.debitAccount(playername, amount * itemPrice, false);
                    } else {
                            player.sendTextMessage("[#FF0000]Not enough coins");
                        }
                    } else {
                        player.sendTextMessage("[#FF0000]Item has no price");
                        return;
                    }
                } catch (SQLException e) {
                    player.sendTextMessage("[#FF0000]SQL Error. Item not found.");
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
                    player.sendTextMessage("[#FF0000]Invalid price!");
                    return;
                }
            } else {
                player.sendTextMessage("[#FF0000]Command usage: /economy set <price>");
            }
            Item testItem = player.getInventory().getItem(player.getInventory().getQuickslotFocus(), Inventory.SlotType.Quickslot);
            if (testItem == null) {
                player.sendTextMessage("[#FF0000]No item in hand.");
                return;
            }
            String test = testItem.toString();
            String itemAttribute = getItemAttribute(testItem.toString());
            if (test.split(",")[1].contentEquals(" name: null")) {
                player.sendTextMessage("[#FF0000]No item in hand.");
            } else {
                int itemVariation = testItem.getVariation();
                int itemID = testItem.getTypeID();
                String itemName = testItem.getName();
                plugin.getWorldDatabase().executeUpdate("REPLACE INTO Pricelist (ItemID, ItemVariation, ItemAttribute, ItemName, ItemPrice) VALUES ( '" + itemID + "', '" + itemVariation + "','" + itemAttribute + "','" + itemName + "','" + itemPrice + "')");
                player.sendTextMessage("[#00FF00]Set " + itemName + " " + itemPrice + " Coins");             
            }
        }
    }
    
    private String getItemAttribute(String itemString) {
        String tempString = itemString.split("ute: ")[1];
        String attribute = tempString.split(",")[0];
        return attribute;
    }
}
