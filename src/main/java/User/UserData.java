package main.java.User;

public class UserData {
    private String username;
    private int totalScore;
    private int wins;
    private int losses;
    private int gamesPlayed;

    public UserData(String username, int totalScore, int wins, int losses, int gamesPlayed) {
        this.username = username;
        this.totalScore = totalScore;
        this.wins = wins;
        this.losses = losses;
        this.gamesPlayed = gamesPlayed;
    }

    public String getUsername() {
        return username;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }
}
