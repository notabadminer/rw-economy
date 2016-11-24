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
import net.risingworld.api.Plugin;
import net.risingworld.api.database.WorldDatabase;

public class Economy extends Plugin {
    
    protected FileUtil fileutil = new FileUtil(this);
    protected Wallet wallet = new Wallet(this);

    @Override
    public void onEnable() {
        System.out.println("Enabling Economy");
        //Register event listener
        registerEventListener(new EconomyListener(this));

        //create our database table if it doesn't exist
        WorldDatabase database = getWorldDatabase();
        database.execute("CREATE TABLE IF NOT EXISTS `Economy` (`ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `PlayerName` VARCHAR(32), `Balance` BIGINT);");
        database.execute("CREATE TABLE IF NOT EXISTS Pricelist (`ItemID` INT, `ItemVariation` INT, `ItemAttribute` VARCHAR(32), `ItemName` VARCHAR(32), `ItemPrice` INT,  UNIQUE(ItemID, ItemVariation, ItemAttribute) ON CONFLICT REPLACE)");
        
        SQLThread sqlThread = new SQLThread();
        Thread sql_update = new Thread(sqlThread);
        sql_update.start();
    }

    @Override
    public void onDisable() {
        System.out.println("Disabling Economy");
    }
    
    public class SQLThread implements Runnable { 
        //This method will be executed when this thread is executed
        public void run() {
            //sleep a bit to allow database update
        try {
           Thread.sleep (1000);
        } catch (InterruptedException ie) {
        }
        
        //check table pricelist. update if neccessary
        try (ResultSet result = getWorldDatabase().executeQuery("SELECT * FROM Pricelist")) {
            if (!result.next()) {
                System.out.println("Pricelist table is empty. Initializing...");
                fileutil.initializePriceData();
            }
        } catch (SQLException e) {
            System.out.println("Error initializing Pricelist table");
        }
            
        }
    }
}
