package com.wcc.platform.model;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

//repository is responsible for saving, retrieving data
public class MemberRepository {

    //create object
    private final List<Member> members = new ArrayList<>();

    public void add(Member member) {
        members.add(member);
    }

    public List<Member> findAll() {
        return members;
    }

    public void saveToCsv(String filePath) throws IOException {
        FileWriter writer = new FileWriter(filePath, true);

        writer.write("name,email,location,joinDate%n");

        for (Member member : members) {
            writer.write(String.format("%s,%s,%s,%s%n",
                    member.getName(),
                    member.getEmail(),
                    member.getLocation(),
                    member.getJoinDate()
            ));
        }
        writer.close();
    }
}

