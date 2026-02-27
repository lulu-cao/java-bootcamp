package com.wcc.platform.model;

import java.io.IOException;
import java.util.List;

public interface MemberRepository {

    void add(Member member);

    List<Member> findAll();

    void saveToCsv(String filePath) throws IOException;

    void loadFromCsv(String filePath) throws IOException;
}