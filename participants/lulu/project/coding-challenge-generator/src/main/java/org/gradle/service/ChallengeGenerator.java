package org.gradle.service;

import java.util.List;
import java.util.Random;
import org.gradle.model.Challenge;

public class ChallengeGenerator {
    private Random random = new Random();

    public Challenge getRandomChallenge(List<Challenge> challenges) {
        if (challenges.isEmpty()) {
            return null;
        }
        int index = random.nextInt(challenges.size());
        return challenges.get(index);
    }
}