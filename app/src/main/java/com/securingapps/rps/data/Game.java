package com.securingapps.rps.data;

import java.util.Date;

/**
 * @author ldavid
 * @created 5/10/17
 */
public class Game {

    private int id = 0;
    private Player[] player_set = new Player[0];
    private Round[] rounds = new Round[0];
    private Date date_started;
    private Date date_ended;
    private int rounds_num;
    private int current_round;
    private String status;

    public int getId() {
        return id;
    }

    public Player[] getPlayerSet() {
        return player_set;
    }

    public Round[] getRounds() {
        return rounds;
    }

    public Date getDateStarted() {
        return date_started;
    }

    public Date getDateEnded() {
        return date_ended;
    }

    public int getRoundsNum() {
        return rounds_num;
    }

    public int getCurrentRound() {
        return current_round;
    }

    public String getStatus() { return status; }

    public Player getOpponent() {
        String self = DeviceData.getDeviceId();
        for (Player player : player_set) {
            if (!player.getClientId().equals(self)) {
                return player;
            }
        }
        return null;
    }

    public boolean isComplete() {
        return !status.equals("O");
    }

    public Player getSelf() {
        String self = DeviceData.getDeviceId();
        for (Player player : player_set) {
            if (player.getClientId().equals(self)) {
                return player;
            }
        }
        return null;
    }

    public static class Player {
        private int id;
        private String client_id;

        public int getId() {
            return id;
        }

        public String getClientId() {
            return client_id;
        }
    }

    public static class Round {
        private int number;
        private int winner_id;
        private int[] players;

        public int getNumber() {
            return number;
        }

        public int getWinner() {
            return winner_id;
        }

        public int[] getPlayers() { return players; }
    }
}
