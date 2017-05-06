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

import economy.api.IWallet;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author notabadminer
 */
public class Wallet implements IWallet {
    private final Economy plugin;

    public Wallet(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean debitAccount(String playername, long amount, boolean simulate) {

        try (ResultSet result = plugin.database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            long balance = result.getLong("Balance");
            if (balance >= amount) {
                balance -= amount;
                if (!simulate) {
                    plugin.database.execute("UPDATE Economy SET balance='" + balance + "' WHERE PlayerName='" + playername + "'");
                }
                return true;
            }
        } catch (SQLException e) {
            createAccount(playername);
        }
        return false; 
    }

    @Override
    public boolean creditAccount(String playername, long amount, boolean simulate) {

        try (ResultSet result = plugin.database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            long balance = result.getLong("Balance");
            if (Long.MAX_VALUE - balance >= amount) {
                balance += amount;
                if (!simulate) {
                    plugin.database.execute("UPDATE Economy SET balance='" + balance + "' WHERE PlayerName='" + playername + "'");
                }
                return true;
            }            
        } catch (SQLException e) {
            createAccount(playername);
        }
        return false;      
    }

    @Override
    public long accountBalance(String playername) {

        try (ResultSet result = plugin.database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            return result.getLong("Balance");
        } catch (SQLException e) {
            createAccount(playername);
            return 0;
        }
    }
    
    private void createAccount(String playername) {
        plugin.database.executeUpdate("REPLACE INTO `Economy` (`PlayerName`, `Balance`) VALUES ('" + playername + "',0)");
    }
}
