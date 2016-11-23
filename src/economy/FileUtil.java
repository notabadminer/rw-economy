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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author notabadminer
 */
public class FileUtil {
    private final Economy plugin;

    public FileUtil(Economy plugin) {
        this.plugin = plugin;
    }

    public void exportPriceData() {

        try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemID, ItemVariation, ItemName, ItemPrice FROM Pricelist")) {
            FileWriter fstream = null;
            fstream = new FileWriter(new File("pricelist.csv"));
            BufferedWriter out = new BufferedWriter(fstream);
            while (result.next()) {
                out.write(Integer.toString(result.getInt("ItemID")) + ", ");
                out.write(Integer.toString(result.getInt("ItemVariation")) + ", ");
                out.write(result.getString("ItemName") + ", ");
                out.write(result.getString("ItemPrice"));
                out.newLine();
            }
            System.out.println("Completed writing pricelist");
            out.close();
        } catch (SQLException e) {
            System.out.println("SQL Error accessing Items table");
        } catch (IOException ex) {
            System.out.println("Error writing to pricelist.csv");
        }
    }

    public void importPriceData() {
        String csvFile = "pricelist.csv";
        String line = "";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                String[] temp = line.split(",");
                if (temp.length < 4) {
                    System.out.println("Error reading from pricelist.csv. Line: " + lineNumber);
                    continue;
                }
                int itemID = Integer.parseInt(temp[0]);
                int itemVariation = Integer.parseInt(temp[1]);
                String itemName = temp[2];
                int itemPrice = Integer.parseInt(temp[3]);
                try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemPrice FROM Pricelist WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "'")) {
                    if (result.next()) {
                    plugin.getWorldDatabase().execute("UPDATE Pricelist SET ItemPrice='" + itemPrice + "' WHERE ItemID='" + itemID + "' AND ItemVariation='" + itemVariation + "'");
                    } else {
                    plugin.getWorldDatabase().execute("INSERT INTO Pricelist (ItemID, ItemVariation, ItemName, ItemPrice) VALUES ( '" + itemID + "', '" + itemVariation + "','" + itemName + "','" + itemPrice + "')");
                    }  
                } catch (SQLException e) {
                    System.out.println("Error accessing database");
                }               
            }
        } catch (IOException e) {
            System.out.println("Error reading from pricelist.csv");
        }

    }

}
