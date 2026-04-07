package org.gradle.repository;

import java.util.ArrayList;
import java.util.List;
import org.gradle.model.Challenge;

public class ChallengeRepository {
  private final List<Challenge> challenges = new ArrayList<>();

  public void addChallenge(Challenge challenge) {
    challenges.add(challenge);
  }

  public List<Challenge> getAllChallenges() {
    return challenges;
  }
}
