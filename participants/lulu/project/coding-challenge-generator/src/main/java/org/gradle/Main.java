package org.gradle;

import org.gradle.model.Challenge;
import org.gradle.repository.ChallengeRepository;
import org.gradle.service.ChallengeGenerator;

public class Main {
  static void main(String[] args) {
    ChallengeRepository repo = new ChallengeRepository();

    repo.addChallenge(
        new Challenge(
            "Two Sum", "Find two numbers that add up to target.", "Easy", "Use a hashmap"));

    repo.addChallenge(
        new Challenge("Reverse String", "Reverse a string.", "Easy", "Use two pointers"));

    ChallengeGenerator generator = new ChallengeGenerator();
    Challenge challenge = generator.getRandomChallenge(repo.getAllChallenges());

    if (challenge != null) {
      System.out.println("Title: " + challenge.title());
      System.out.println("Difficulty: " + challenge.difficulty());
      System.out.println("Description: " + challenge.description());
      System.out.println("Hint: " + challenge.hint());
    }
  }
}
