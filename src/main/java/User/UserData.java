package main.java.User;

public class UserData {
    private String username;
    private String password;
    private String sex;  // Add sex attribute
    private String age;  // Add age attribute
    private int totalScore;
    private int wins;
    private int losses;
    private int gamesPlayed;

    public UserData(String username, String password, String sex, String age, int totalScore, int wins, int losses, int gamesPlayed) {
        this.username = username;
        this.password = password;
        this.sex = sex;  // Initialize sex
        this.age = age;  // Initialize age
        this.totalScore = totalScore;
        this.wins = wins;
        this.losses = losses;
        this.gamesPlayed = gamesPlayed;
    }

    // Getters and setters for all attributes
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSex() {
        return sex;
    }

    public String getAge() {
        return age;
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

    public void addWin(int score) {
        this.wins++;
        this.totalScore += score;
        this.gamesPlayed++;
    }

    public void addLoss() {
        this.losses++;
        this.gamesPlayed++;
    }

    public double getAverageScore() {
        return gamesPlayed == 0 ? 0 : (double) totalScore / gamesPlayed;
    }

    public double getWinLossRatio() {
        return losses == 0 ? wins : (double) wins / losses;
    }
}
