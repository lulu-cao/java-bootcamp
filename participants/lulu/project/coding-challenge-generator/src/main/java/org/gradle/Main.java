package org.gradle;

import java.util.List;
import org.gradle.model.Challenge;
import org.gradle.repository.ChallengeRepository;
import org.gradle.service.ChallengeGenerator;

public class Main {
  static void main(String[] args) {

    ChallengeRepository repo = new ChallengeRepository();
    List<Challenge> challenges = repo.loadChallenges();

    ChallengeGenerator generator = new ChallengeGenerator();
    Challenge challenge = generator.getRandomChallenge(challenges);

    if (challenge != null) {
      System.out.println("Title: " + challenge.getTitle());
      System.out.println("Difficulty: " + challenge.getDifficulty());
      System.out.println("Description: " + challenge.getDescription());
      System.out.println("Hint: " + challenge.getHint());
    } else {
      System.out.println("No challenges available.");
    }
  }
}
