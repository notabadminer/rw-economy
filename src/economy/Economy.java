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
import net.risingworld.api.database.Database;

public class Economy extends Plugin {
    
    protected FileUtil fileutil = new FileUtil(this);
    protected Wallet wallet = new Wallet(this);
    protected LangSupport i18n = new LangSupport();
    protected Database database;

    @Override
    public void onEnable() {
        System.out.println("Enabling Economy");
        database = getSQLiteConnection(getPath() + "/" + getWorld().getName() + ".db");
        
        //create our database table if it doesn't exist
        database.execute("CREATE TABLE IF NOT EXISTS `Economy` (`PlayerName` VARCHAR(32) PRIMARY KEY UNIQUE, `Balance` BIGINT);");
        database.execute("CREATE TABLE IF NOT EXISTS `Pricelist` (`ItemID` INT, `ItemVariation` INT, `ItemAttribute` VARCHAR(32), `ItemName` VARCHAR(32), `ItemPrice` INT, UNIQUE(ItemID, ItemVariation, ItemAttribute) ON CONFLICT REPLACE)");

        //check table pricelist. update if neccessary
        try (ResultSet result = database.executeQuery("SELECT * FROM Pricelist")) {
            if (!result.next()) {
                System.out.println("Pricelist table is empty. Initializing...");
                fileutil.initializePriceData();
            } else {
                System.out.println("Pricelist table initialized");
            }
        } catch (SQLException e) {
            System.out.println("Error initializing Pricelist table");
        }
        
        //Register event listener
        registerEventListener(new EconomyListener(this));
        
        //load localization
        i18n.init();
    }

    @Override
    public void onDisable() {
        System.out.println("Disabling Economy");
        database.close();
    }
}
