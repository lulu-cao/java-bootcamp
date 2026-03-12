package com.wcc.platform.cli;

import com.wcc.platform.model.Member;
import com.wcc.platform.model.MemberRepository;
import com.wcc.platform.validation.EmailValidator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MemberCli {
    private final MemberRepository repository;
    private final Scanner scanner;

    public MemberCli(MemberRepository repository) {
        this.repository = repository;
        this.scanner = new Scanner(System.in);
    }

    public void start() throws IOException {
        boolean running = true;
        while(running) {
            printMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    addMember();
                    break;
                case "2":
                    viewMembers();
                    break;
                case "3":
                    updateMember();
                    break;
                case "4":
                    deleteMember();
                    break;


                case "5":
                    searchByLocation();
                    break;

                case "6":
                    searchBySkill();
                    break;

                case "7":
                    sortByName();
                    break;

                case "8":
                    sortByJoinDate();
                    break;


                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== Member Directory =====");
        System.out.println("1. Add Member");
        System.out.println("2. View Members");
        System.out.println("3. Update Member");
        System.out.println("4. Delete Member");
        System.out.println("5. Search by Location");
        System.out.println("6. Search by Skill");
        System.out.println("7. Sort by Name");
        System.out.println("8. Sort by Join Date");
        System.out.println("0. Exit");
        System.out.println("Choose option: ");
    }

    private void addMember() throws IOException {

        System.out.println("Enter name:");
        String name = scanner.nextLine();

        String email = EmailValidator.promptValidEmail(scanner);

        System.out.println("Enter location:");
        String location = scanner.nextLine();

        System.out.println("Enter skills (comma separated):");
        String skillsInput = scanner.nextLine();

        List<String> skills = List.of(skillsInput.split(","));

        Member member = new Member(
                name,
                email,
                location,
                skills,
                LocalDate.now()
        );

        repository.add(member);
        repository.saveToCsv("members.csv");

        System.out.println("Member added.");
    }

    private void viewMembers() {

        System.out.printf("%-15s %-25s %-15s %-20s%n", "Name", "Email", "Location", "Skills");
        System.out.println("---------------------------------------------------------------------------------");

        repository.findAll().forEach(member ->
                System.out.printf("%-15s %-25s %-15s %-20s%n",
                        member.getName(),
                        member.getEmail(),
                        member.getLocation(),
                        String.join("|", member.getSkills())
                )
        );
    }

    private void updateMember() throws IOException {

        System.out.println("Enter email of member to update:");
        String email = scanner.nextLine();

        System.out.println("Enter new name:");
        String newName = scanner.nextLine();

        System.out.println("Enter new email:");
        String newEmail = EmailValidator.promptValidEmail(scanner);

        System.out.println("Enter new location:");
        String newLocation = scanner.nextLine();

        System.out.println("Enter new skills:");
        String newSkills = scanner.nextLine();

        repository.updateMember(email, newName, newEmail, newLocation, newSkills);

        repository.saveToCsv("members.csv");
    }


    private void deleteMember() throws IOException {

        System.out.println("Enter email to delete:");

        String email = scanner.nextLine();

        repository.deleteByEmail(email);

        repository.saveToCsv("members.csv");

        System.out.println("Member deleted.");
    }


    private void searchByLocation() {

        System.out.println("Enter location:");
        String location = scanner.nextLine();

        List<Member> results = repository.findByLocation(location);

        results.forEach(member ->
                System.out.printf("%-15s %-25s %-15s%n",
                        member.getName(),
                        member.getEmail(),
                        member.getLocation()
                )
        );
    }

    private void searchBySkill() {

        System.out.println("Enter skill:");
        String skill = scanner.nextLine();

        List<Member> results = repository.findBySkill(skill);

        results.forEach(member ->
                System.out.printf("%-15s %-25s %-15s%n",
                        member.getName(),
                        member.getEmail(),
                        member.getLocation()
                )
        );
    }

    private void sortByName() {

        List<Member> results = repository.sortByName();

        results.forEach(member ->
                System.out.printf("%-15s %-25s %-15s%n",
                        member.getName(),
                        member.getEmail(),
                        member.getLocation()
                )
        );
    }

    private void sortByJoinDate() {

        List<Member> results = repository.sortByJoinDate();

        results.forEach(member ->
                System.out.printf("%-15s %-25s %-15s%n",
                        member.getName(),
                        member.getEmail(),
                        member.getLocation()
                )
        );
    }
}
