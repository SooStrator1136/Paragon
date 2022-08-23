package com.paragon.client.managers.social

import com.paragon.Paragon

/**
 * @author SooStrator1136
 */
class SocialManager {

    val players: MutableList<Player> = ArrayList()

    init {
        Paragon.INSTANCE.logger.info("Loaded Social Manager")
    }

    /**
     * Checks if a given player name is a friend
     *
     * @param name The name to check
     * @return If the given player is a friend
     */
    fun isFriend(name: String): Boolean {
        for (player in players) {
            if (player.relationship == Relationship.FRIEND && player.name.equals(name, true)) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a given player name is neutral
     *
     * @param name The name to check
     * @return If the given player is neutral
     */
    fun isNeutral(name: String): Boolean {
        for (player in players) {
            if (player.relationship == Relationship.NEUTRAL && player.name.equals(name, true)) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if a given player name is an enemy
     *
     * @param name The name to check
     * @return If the given player is an enemy
     */
    fun isEnemy(name: String): Boolean {
        for (player in players) {
            if (player.relationship == Relationship.ENEMY && player.name.equals(name, true)) {
                return true
            }
        }
        return false
    }

    /**
     * Adds a player to our players list
     *
     * @param player The player to add
     */
    fun addPlayer(player: Player) {
        players.removeIf { player1: Player -> player1.name.equals(player.name, true) }
        players.add(player)
    }

    /**
     * Removes a player from our players list
     *
     * @param name The name of the player to remove
     */
    fun removePlayer(name: String) {
        players.removeIf { player: Player -> player.name.equals(name, true) }
    }

}