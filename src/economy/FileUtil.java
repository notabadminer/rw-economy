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
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        try (ResultSet result = plugin.getWorldDatabase().executeQuery("SELECT ItemID, ItemVariation, ItemAttribute, ItemName, ItemPrice FROM Pricelist")) {
            FileWriter fstream = null;
            fstream = new FileWriter(new File("plugins/Economy/pricelist.csv"));
            BufferedWriter out = new BufferedWriter(fstream);
            while (result.next()) {
                out.write(Integer.toString(result.getInt("ItemID")) + ",");
                out.write(Integer.toString(result.getInt("ItemVariation")) + ",");
                out.write(result.getString("ItemAttribute") + ",");
                out.write(result.getString("ItemName") + ",");
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
        String csvFile = "plugins/Economy/pricelist.csv";
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            readCSV(br);
        } catch (IOException e) {
            System.out.println("Error reading from pricelist.csv");
        }
    }
    
     public void initializePriceData() {
        String csvFile = "/resources/assets/pricelist.csv";
        
        try {
            InputStreamReader isr = new InputStreamReader(getClass().getResourceAsStream(csvFile));
            BufferedReader br = new BufferedReader(isr);
            readCSV(br);
        } catch (IOException e) {
            System.out.println("Error reading from pricelist.csv");
        }
     }
     
     private void readCSV(BufferedReader br) throws IOException {
         String line = "";
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
                String itemAttribute = temp[2];
                String itemName = temp[3];
                int itemPrice = Integer.parseInt(temp[4]);
                plugin.getWorldDatabase().executeUpdate("REPLACE INTO Pricelist (ItemID, ItemVariation, ItemAttribute, ItemName, ItemPrice) VALUES ('" + itemID + "', '" + itemVariation + "','" + itemAttribute + "','" + itemName + "','" + itemPrice + "')");
            }
     }
}
