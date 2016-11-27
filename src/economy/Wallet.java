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
import net.risingworld.api.database.WorldDatabase;

/**
 *
 * @author notabadminer
 */
public class Wallet implements IWallet {
    private Economy plugin;

    public Wallet(Economy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean debitAccount(String playername, long amount, boolean simulate) {
        WorldDatabase database = plugin.getWorldDatabase();

        try (ResultSet result = database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            long balance = result.getLong("Balance");
            if (balance >= amount) {
                balance -= amount;
                if (!simulate) {
                    database.execute("UPDATE Economy SET balance='" + balance + "' WHERE PlayerName='" + playername + "'");
                }
                return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false; 
    }

    @Override
    public boolean creditAccount(String playername, long amount, boolean simulate) {
        WorldDatabase database = plugin.getWorldDatabase();

        try (ResultSet result = database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            long balance = result.getLong("Balance");
            if (Long.MAX_VALUE - balance >= amount) {
                balance += amount;
                if (!simulate) {
                    database.execute("UPDATE Economy SET balance='" + balance + "' WHERE PlayerName='" + playername + "'");
                }
                return true;
            }            
        } catch (SQLException e) {
            return false;
        }
        return false;      
    }

    @Override
    public long accountBalance(String playername) {
        WorldDatabase database = plugin.getWorldDatabase();

        try (ResultSet result = database.executeQuery("SELECT Balance FROM Economy WHERE PlayerName='" + playername + "'")) {
            return result.getLong("Balance");
        } catch (SQLException e) {
            return 0;
        }
    }
}
