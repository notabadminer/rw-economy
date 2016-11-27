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
    
    static ResourceBundle en_messages = null;
    static ResourceBundle de_messages = null;
    
    public void init() {
        try (FileInputStream fis = new FileInputStream("plugins/Economy/en.lang")) {
              en_messages = new PropertyResourceBundle(fis);
              fis.close();
            } catch (IOException ex) {
                try {
                    InputStreamReader isr = new InputStreamReader(Economy.class.getResourceAsStream("/resources/assets/en.lang"));
                    en_messages = new PropertyResourceBundle(isr);
                    isr.close();
                } catch (IOException | NullPointerException e) {
                    System.out.println("Loading en.lang failed");
                }
            }
        try (FileInputStream fis = new FileInputStream("plugins/Economy/de.lang")) {
              de_messages = new PropertyResourceBundle(fis);
              fis.close();
            } catch (IOException ex) {
                try {
                    InputStreamReader isr = new InputStreamReader(Economy.class.getResourceAsStream("/resources/assets/de.lang"));
                    de_messages = new PropertyResourceBundle(isr);
                    isr.close();
                } catch (IOException | NullPointerException e) {
                    System.out.println("Loading de.lang failed");
                }
            }
    }

   static public String getLocalTranslation(String message, String lang) {
       if (lang.matches("de")) {
           return en_messages.getString(message);
       }
        return en_messages.getString(message);
    }    
}
