package main.java.User;

public class UserData {
    private String username;
    private String password;
    private String sex;
    private String age;
    private int totalScore;
    private int wins;
    private int losses;
    private int gamesPlayed;
    private String profilePicturePath;

    public UserData(String username, String password, String sex, String age, int totalScore, int wins, int losses, int gamesPlayed, String profilePicturePath) {
        this.username = username;
        this.password = password;
        this.sex = sex;
        this.age = age;
        this.totalScore = totalScore;
        this.wins = wins;
        this.losses = losses;
        this.gamesPlayed = gamesPlayed;
        this.profilePicturePath = profilePicturePath;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
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

    public void setUsername(String username) {
		this.username = username;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

	public void setWins(int wins) {
		this.wins = wins;
	}

	public void setLosses(int losses) {
		this.losses = losses;
	}

	public void setGamesPlayed(int gamesPlayed) {
		this.gamesPlayed = gamesPlayed;
	}

	public double getAverageScore() {
        return gamesPlayed == 0 ? 0 : (double) totalScore / gamesPlayed;
    }

    public double getWinLossRatio() {
        return losses == 0 ? wins : (double) wins / losses;
    }
}
