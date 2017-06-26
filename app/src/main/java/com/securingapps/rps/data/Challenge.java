package com.securingapps.rps.data;

import com.securingapps.rps.utils.ArrayUtils;

import java.util.Arrays;
import java.util.Date;

/**
 * @author ldavid
 * @created 4/6/17
 */
public class Challenge {

    private int gameId;

    private String opponentId;
    private int bet;

    private Date date;

    private int currentRound;
    private int totalRounds;

    private RoundStatus[] rounds;

    private boolean isComplete;

    public Challenge(Game game) {
        this.gameId = game.getId();
        this.date = game.getDateStarted();
        Game.Player self = game.getSelf();
        Game.Player opponent = game.getOpponent();
        this.opponentId = opponent.getClientId();
        this.totalRounds = game.getRoundsNum();
        this.currentRound = game.getCurrentRound();
        this.rounds = new RoundStatus[this.totalRounds];
        this.isComplete = game.isComplete();
        Arrays.fill(this.rounds, RoundStatus.UNKNOWN);

        for (Game.Round round : game.getRounds()) {
            if (round.getNumber() < currentRound || game.isComplete()) {
                int winner = round.getWinner();
                if (winner == opponent.getId()) {
                    this.rounds[round.getNumber() - 1] = RoundStatus.DEFEAT;
                } else if (winner == 0) {
                    this.rounds[round.getNumber() - 1] = RoundStatus.DRAW;
                } else {
                    this.rounds[round.getNumber() - 1] = RoundStatus.VICTORY;
                }
            } else if (round.getNumber() == currentRound) {
                int[] players = round.getPlayers();
                if (ArrayUtils.contains(players, self.getId())) {
                    this.rounds[round.getNumber() - 1] = RoundStatus.PLAYED;
                }
            }
        }
    }

    public int getGameId() {
        return gameId;
    }

    public void setCurrentRound(int currentRounds) {
        this.currentRound = currentRounds;
    }

    public void setRounds(RoundStatus[] rounds) {
        this.rounds = rounds;
    }

    public String getOpponentId() {
        return opponentId;
    }

    public int getBet() {
        return bet;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public RoundStatus[] getRounds() {
        return rounds;
    }

    public Date getDate() {
        return date;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean canPlay() {
        return this.rounds[getCurrentRound() - 1] == RoundStatus.UNKNOWN;
    }

    public enum RoundStatus {
        VICTORY("\uD83D\uDC4D"),
        // VICTORY("\u263A"),
        DRAW("\uD83D\uDE10"),
        DEFEAT("\uD83D\uDC4E"),
        // DEFEAT("\uD83D\uDE41"),
        UNKNOWN("\u2205"),
        PLAYED("\u2713");

        public final String symbol;

        RoundStatus(String symbol) {
            this.symbol = symbol;
        }
    }
}
