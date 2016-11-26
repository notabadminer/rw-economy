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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 *
 * @author notabadminer
 */
public class LangSupport {

   static public String getLocalTranslation(String message, String lang) {
 
        ResourceBundle messages = null;
        
        try (FileInputStream fis = new FileInputStream("plugins/Economy/" + lang + ".lang")) {
              messages = new PropertyResourceBundle(fis);
            } catch (IOException ex) {
                System.out.println("Cannot find lang from plugins folder");
                try {
                    InputStreamReader isr = new InputStreamReader(Economy.class.getResourceAsStream("/resources/assets/" + lang + ".lang"));
                    messages = new PropertyResourceBundle(isr);
                } catch (IOException | NullPointerException e) {
                    System.out.println("Cannot find lang from jar. Using default lang");
                    FileInputStream fis;
                    try {
                        InputStreamReader isr = new InputStreamReader(Economy.class.getResourceAsStream("/resources/assets/en.lang"));
                    messages = new PropertyResourceBundle(isr);
                    } catch (IOException ex1) {
                        System.out.println("Everything is broken in i18n");
                    }
                }
            }
        return messages.getString(message);
    }    
}
