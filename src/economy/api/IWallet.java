/*
 * Copyright (C) 2016 mikom
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
package economy.api;

/**
 *
 * @author mikom
 */
public interface IWallet {
    
    
    /**
     * withdraw coins from player account. returns true if successful
     */
    boolean debitAccount(String playername, long amount, boolean simulate);
    
    /**
     * add coins to player account. returns true if successful
     */
    boolean creditAccount(String playername, long amount, boolean simulate);
    
    /**
     * returns account balance if player found or -1
     */
    long accountBalance(String playername);
}
