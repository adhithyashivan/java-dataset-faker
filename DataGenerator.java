import com.github.javafaker.Faker;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DataGenerator {

    private static final Faker faker = new Faker(new Locale("en-US")); // Or your preferred locale
    private static final Random random = new Random();
    private static final String OUTPUT_FOLDER_NAME = "java_libraries";

    // --- Configuration ---
    private static List<String> USER_NAMES;
    private static final List<String> TEAM_NAMES = List.of("Team Phoenix", "Team Griffin", "Team Hydra", "Marketing", "Cross-functional", "Operations");
    private static final List<String> JIRA_TYPES = List.of("Story", "Task", "Bug", "Feature", "Epic", "Project", "Business Outcome");
    private static final List<String> JIRA_PRIORITIES = List.of("Minor", "Major", "Critical", "Low", "Medium", "High");
    private static final List<String> JIRA_STATUSES = List.of("Pending", "Development", "Review", "Release", "Closed", "Blocked", "Open", "In Progress");
    private static final List<String> CR_STATES = List.of("New", "Assess", "Authorise", "Scheduled", "Implement", "Closed");
    private static final List<String> CR_TYPES = List.of("Standard", "Emergency", "Normal");
    private static final List<String> CR_CATEGORIES = List.of("Enhancement", "BugFix", "Security", "Infrastructure", "Deployment", "Audit", "Maintenance", "New Feature", "Communication");
    private static final List<String> CR_RISKS = List.of("Low", "Medium", "High");
    private static final List<String> JIRA_LINK_TYPES = List.of("blocks", "relates to", "duplicates", "sub-task of", "cloned by");
    private static final List<String> CONFLUENCE_SPACES = List.of("Project Nova", "Team Phoenix KB", "Team Griffin Design", "Team Hydra Compliance", "General Fintech Policies");

    // --- Data Storage for Linking ---
    private static final List<String> generatedCrIds = new ArrayList<>();
    private static final List<String> generatedJiraIdsUnique = new ArrayList<>();
    private static final List<String> generatedConfluenceIds = new ArrayList<>();

    // --- Date Formatters ---
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public static void initializeUserNames(int count) {
        USER_NAMES = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();
        while (uniqueNames.size() < count) {
            String firstName = faker.name().firstName();
            String lastNameInitial = faker.name().lastName().substring(0, 1);
            uniqueNames.add(firstName + "_" + lastNameInitial);
        }
        USER_NAMES.addAll(uniqueNames);
    }

    // --- Helper Functions ---
    private static LocalDate generateRandomLocalDate(LocalDate startDate, LocalDate endDate) {
        Date randomUtilDate = faker.date().between(
                Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        );
        return randomUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    
    private static LocalDateTime generateRandomLocalDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Date randomUtilDate = faker.date().between(
                Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())
        );
        return randomUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    private static String generateSemicolonDelimitedList(List<String> sourceList, int maxItems) {
        if (sourceList == null || sourceList.isEmpty()) {
            return "";
        }
        int numItems = random.nextInt(Math.min(maxItems, sourceList.size()) + 1);
        if (numItems == 0) {
            return "";
        }
        List<String> tempList = new ArrayList<>(sourceList);
        Collections.shuffle(tempList, random);
        return tempList.subList(0, numItems).stream().collect(Collectors.joining(";"));
    }

    private static <T> T randomChoice(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null; // Or throw an exception
        }
        return list.get(random.nextInt(list.size()));
    }

    private static void ensureOutputDirectoryExists() {
        File directory = new File(OUTPUT_FOLDER_NAME);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    // --- CSV Generation Functions ---

    public static void generateCrMainCsv(String filename, int numUniqueCrs) {
        ensureOutputDirectoryExists();
        String[] header = {
                "CR_ID", "CR_Title", "Linked_Jira_ID", "Linked_Confluence_ID", "CR_State",
                "CR_Requested_By", "CR_Team_Assignment_Group", "CR_Assigned_To_User",
                "CR_Impacted_Environment", "CR_Impacted_Departments", "CR_Type", "CR_Category",
                "CR_Risk", "CR_Risk_Percentage", "CR_Lead_Time_Days", "CR_Conflict_Status",
                "CR_Description", "CR_Start_Date", "CR_End_Date",
                "CR_Implementation_Plan_Summary", "CR_Backout_Plan_Summary",
                "CR_Updated_By_User_From_CSV_Example", "CR_Created_At_From_CSV_Example"
        };
        List<List<String>> allRows = new ArrayList<>();
        int crIdCounter = 1;

        Map<String, Integer> teamCrCounts = new HashMap<>();
        List<String> mainTeams = List.of("Team Phoenix", "Team Griffin", "Team Hydra");
        mainTeams.forEach(team -> teamCrCounts.put(team, 0));
        int targetTeamCrs = 7; // Target CRs per main team (adjust if numUniqueCrs is small)
        if (numUniqueCrs < mainTeams.size() * targetTeamCrs) {
            targetTeamCrs = Math.max(1, numUniqueCrs / mainTeams.size());
        }


        for (int i = 0; i < numUniqueCrs; i++) {
            String crIdBase = String.format("CR-FS-%03d", crIdCounter++);
            generatedCrIds.add(crIdBase);

            String title = String.join(" ", faker.lorem().words(random.nextInt(3) + 3));
            title = title.substring(0, 1).toUpperCase() + title.substring(1);

            String assignedTeam = null;
            for (String team : mainTeams) {
                if (teamCrCounts.get(team) < targetTeamCrs) {
                    assignedTeam = team;
                    teamCrCounts.put(team, teamCrCounts.get(team) + 1);
                    break;
                }
            }
            if (assignedTeam == null) {
                assignedTeam = randomChoice(TEAM_NAMES);
            }

            LocalDate overallStartDate = generateRandomLocalDate(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2024, 3, 1)
            );
            
            int numStatusUpdates = random.nextInt(3) + 1; // 1 to 3 updates
            int currentStateIndex = 0;
            LocalDate lastStatusUpdateDate = overallStartDate;

            for (int j = 0; j < numStatusUpdates; j++) {
                if (currentStateIndex >= CR_STATES.size()) break;
                String state = CR_STATES.get(currentStateIndex++);

                LocalDate createdAtStatus = generateRandomLocalDate(
                    lastStatusUpdateDate,
                    lastStatusUpdateDate.plusDays(random.nextInt(29) + 1) // 1 to 30 days later
                );
                lastStatusUpdateDate = createdAtStatus;

                LocalDate crEndDate = generateRandomLocalDate(
                    createdAtStatus,
                    createdAtStatus.plusDays(random.nextInt(55) + 5) // 5 to 60 days later
                );

                List<String> row = new ArrayList<>();
                row.add(crIdBase);
                row.add(title);
                row.add(generatedJiraIdsUnique.isEmpty() || random.nextDouble() <= 0.5 ? "" : randomChoice(generatedJiraIdsUnique));
                row.add(generatedConfluenceIds.isEmpty() || random.nextDouble() <= 0.7 ? "" : randomChoice(generatedConfluenceIds));
                row.add(state);
                row.add(randomChoice(USER_NAMES));
                row.add(assignedTeam);
                row.add(randomChoice(USER_NAMES));
                row.add(randomChoice(List.of("Production", "Staging", "Development", "N/A")));
                row.add(generateSemicolonDelimitedList(List.of("Payments", "Mobile Banking", "Security", "Core API", "Marketing"), 3));
                row.add(randomChoice(CR_TYPES));
                row.add(randomChoice(CR_CATEGORIES));
                row.add(randomChoice(CR_RISKS));
                row.add(random.nextDouble() > 0.3 ? String.valueOf(random.nextInt(101)) : "");
                row.add(String.valueOf(random.nextInt(90) + 1));
                row.add(randomChoice(List.of("No Conflict", "Conflict Detected", "Resolved")));
                row.add(faker.company().bs() + " " + faker.company().bs());
                row.add(overallStartDate.format(DATE_FORMATTER)); // Use overall start for the CR concept
                row.add(crEndDate.format(DATE_FORMATTER));
                row.add(random.nextDouble() > 0.5 ? "Details in Confluence" : String.join(" ", faker.lorem().words(6)));
                row.add(random.nextDouble() > 0.5 ? "Standard rollback" : String.join(" ", faker.lorem().words(5)));
                row.add(randomChoice(USER_NAMES));
                row.add(createdAtStatus.format(DATE_FORMATTER)); // This is the timestamp for this specific status
                allRows.add(row);
            }
        }
        writeCsv(Paths.get(OUTPUT_FOLDER_NAME, filename).toString(), header, allRows);
        System.out.printf("Generated %s with %d data rows (%d unique CRs, %d total rows due to status updates).%n", filename, allRows.size(), numUniqueCrs);
    }

    public static void generateCrCtasksCsv(String filename, int numRows) {
        ensureOutputDirectoryExists();
        String[] header = {
                "CTASK_ID", "CR_ID", "CTASK_Assigned_To_User", "CTASK_Start_Time",
                "CTASK_End_Time", "CTASK_Description"
        };
        List<List<String>> rows = new ArrayList<>();
        if (generatedCrIds.isEmpty()) {
            System.out.println("Cannot generate CR_CTasks.csv: No CR_IDs available.");
            return;
        }

        for (int i = 0; i < numRows; i++) {
            LocalDateTime startTime = LocalDateTime.now().minusDays(random.nextInt(60)).minusHours(random.nextInt(24));
            LocalDateTime endTime = startTime.plusHours(random.nextInt(46) + 2); // 2 to 48 hours later

            rows.add(List.of(
                    String.format("CTASK%03d", i + 1),
                    randomChoice(generatedCrIds),
                    randomChoice(USER_NAMES),
                    startTime.format(DATETIME_FORMATTER),
                    endTime.format(DATETIME_FORMATTER),
                    faker.company().catchPhrase()
            ));
        }
        writeCsv(Paths.get(OUTPUT_FOLDER_NAME, filename).toString(), header, rows);
        System.out.printf("Generated %s with %d data rows.%n", filename, rows.size());
    }

    public static void generateJiraIssuesDetailedCsv(String filename, int numUniqueIssues) {
        ensureOutputDirectoryExists();
        String[] header = {
                "JIRA_ID", "JIRA_Type", "JIRA_Priority", "JIRA_Components", "JIRA_Labels",
                "JIRA_Sprint", "JIRA_App_Name", "JIRA_Reporter", "JIRA_Assignee",
                "JIRA_Start_Date", "JIRA_End_Date", "JIRA_Status", "JIRA_Title",
                "JIRA_Description", "JIRA_Release_Fix_Version", "JIRA_Team", "JIRA_Confidence",
                "JIRA_Created_Date", "JIRA_Updated_Date", "JIRA_Effort_Story_Points",
                "CR_ID_Link_From_CSV_Example", "JIRA_Linked_Issue_ID_Target", "JIRA_Link_Type", "JIRA_Watcher_User"
        };
        List<List<String>> allJiraRows = new ArrayList<>();
        int jiraIdCounter = 1;

        for (int i = 0; i < numUniqueIssues; i++) {
            String jiraIdBase;
            String prefix = random.nextDouble() < 0.2 ?
                            randomChoice(List.of("LOG", "PERF", "BUG", "FEAT")) : "NOVA";
            jiraIdBase = String.format("%s-%03d", prefix, jiraIdCounter++);
            generatedJiraIdsUnique.add(jiraIdBase);

            LocalDate createdDate = generateRandomLocalDate(LocalDate.of(2023, 1, 1), LocalDate.of(2024, 4, 1));
            LocalDate startDate = generateRandomLocalDate(createdDate, createdDate.plusDays(10));
            LocalDate endDate = generateRandomLocalDate(startDate, startDate.plusDays(random.nextInt(55) + 5)); // 5 to 60 days
            LocalDate updatedDate = generateRandomLocalDate(createdDate,
                    endDate.isBefore(LocalDate.now()) ? endDate : LocalDate.now().minusDays(1)
            );
            if (updatedDate.isBefore(createdDate)) updatedDate = createdDate; // Ensure updated is not before created


            List<String> baseJiraData = new ArrayList<>(List.of(
                    jiraIdBase, randomChoice(JIRA_TYPES), randomChoice(JIRA_PRIORITIES),
                    generateSemicolonDelimitedList(List.of("API", "Mobile UI", "Database", "Auth", "Payments", "NFC"), 3),
                    generateSemicolonDelimitedList(List.of("performance", "security", "sprint-goal", "ProjectNova", "tech-debt", "ux"), 3),
                    String.format("Sprint %d - %s", random.nextInt(5) + 1, randomChoice(List.of("Nova", "General", "Infra"))),
                    random.nextDouble() > 0.3 ? randomChoice(List.of("CoreBankingApp_Wallet", "MobileApp_Global", "AdminPortal", "")) : "",
                    randomChoice(USER_NAMES), randomChoice(USER_NAMES),
                    startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER),
                    randomChoice(JIRA_STATUSES),
                    faker.company().bs().substring(0, 1).toUpperCase() + faker.company().bs().substring(1), // Capitalized
                    String.join(" ", faker.lorem().sentences(1)),
                    String.format("v%d.%d.%d%s", random.nextInt(3), random.nextInt(9) + 1, random.nextInt(6), randomChoice(List.of("-beta", "-RC", "", "-hotfix"))),
                    randomChoice(TEAM_NAMES.subList(0, Math.min(3, TEAM_NAMES.size()))), // Primary dev teams
                    random.nextDouble() > 0.5 ? String.valueOf(random.nextInt(51) + 50) : "", // 50-100
                    createdDate.format(DATE_FORMATTER), updatedDate.format(DATE_FORMATTER),
                    random.nextDouble() > 0.2 ? String.valueOf(randomChoice(List.of(1, 2, 3, 5, 8, 13, 21))) : "",
                    generatedCrIds.isEmpty() || random.nextDouble() <= 0.6 ? "" : randomChoice(generatedCrIds)
            ));

            // Add base row (no links/watchers)
            List<String> baseRowWithEmptyLinks = new ArrayList<>(baseJiraData);
            baseRowWithEmptyLinks.add(""); // JIRA_Linked_Issue_ID_Target
            baseRowWithEmptyLinks.add(""); // JIRA_Link_Type
            baseRowWithEmptyLinks.add(""); // JIRA_Watcher_User
            allJiraRows.add(baseRowWithEmptyLinks);

            // Add rows for Links
            int numLinks = random.nextInt(3); // 0 to 2 links
            if (numLinks > 0 && generatedJiraIdsUnique.size() > 1) {
                List<String> potentialTargets = new ArrayList<>(generatedJiraIdsUnique);
                potentialTargets.remove(jiraIdBase); // Cannot link to itself
                Collections.shuffle(potentialTargets);

                for (int l = 0; l < Math.min(numLinks, potentialTargets.size()); l++) {
                    List<String> linkRow = new ArrayList<>(baseJiraData);
                    linkRow.add(potentialTargets.get(l)); // JIRA_Linked_Issue_ID_Target
                    linkRow.add(randomChoice(JIRA_LINK_TYPES));   // JIRA_Link_Type
                    linkRow.add("");                              // JIRA_Watcher_User
                    allJiraRows.add(linkRow);
                }
            }

            // Add rows for Watchers
            int numWatchers = random.nextInt(4); // 0 to 3 watchers
            if (numWatchers > 0) {
                for (int w = 0; w < numWatchers; w++) {
                    List<String> watcherRow = new ArrayList<>(baseJiraData);
                    watcherRow.add("");                             // JIRA_Linked_Issue_ID_Target
                    watcherRow.add("");                             // JIRA_Link_Type
                    watcherRow.add(randomChoice(USER_NAMES)); // JIRA_Watcher_User
                    allJiraRows.add(watcherRow);
                }
            }
        }
        writeCsv(Paths.get(OUTPUT_FOLDER_NAME, filename).toString(), header, allJiraRows);
        System.out.printf("Generated %s with %d data rows (%d unique JIRA issues).%n", filename, allJiraRows.size(), numUniqueIssues);
    }

    public static void generateJiraActivitiesCsv(String filename, int numRows) {
        ensureOutputDirectoryExists();
        String[] header = {
                "Activity_ID", "JIRA_ID", "Activity_Comment", "Activity_Timestamp", "Activity_User"
        };
        List<List<String>> rows = new ArrayList<>();
        if (generatedJiraIdsUnique.isEmpty()) {
            System.out.println("Cannot generate JIRA_Activities.csv: No JIRA_IDs available.");
            return;
        }

        for (int i = 0; i < numRows; i++) {
            Date activityTimeUtil = faker.date().past(90, TimeUnit.DAYS);
            LocalDateTime activityTime = activityTimeUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            rows.add(List.of(
                    String.format("ACT%03d", i + 1),
                    randomChoice(generatedJiraIdsUnique),
                    randomChoice(List.of(
                            String.join(" ", faker.lorem().words(7)),
                            "Status changed to " + randomChoice(JIRA_STATUSES),
                            "Comment added."
                    )),
                    activityTime.format(DATETIME_FORMATTER),
                    randomChoice(USER_NAMES)
            ));
        }
        writeCsv(Paths.get(OUTPUT_FOLDER_NAME, filename).toString(), header, rows);
        System.out.printf("Generated %s with %d data rows.%n", filename, rows.size());
    }

    public static void generateConfluencePagesDetailedCsv(String filename, int numRows) {
        ensureOutputDirectoryExists();
        String[] header = {
                "Confluence_ID", "Confluence_Title", "Confluence_Owner_Member", "Confluence_Last_Edited_By",
                "Confluence_Space", "Confluence_Team_Association", "Confluence_Content_Summary",
                "Confluence_Linked_Jira_ID", "Confluence_Linked_CR_ID", "Confluence_Parent_Page_ID",
                "Confluence_Created_Date", "Confluence_Last_Modified_Date"
        };
        List<List<String>> rows = new ArrayList<>();
        List<String> potentialParentPageIds = new ArrayList<>();

        for (int i = 0; i < numRows; i++) {
            String confId = String.format("CONF-%s-%03d", randomChoice(List.of("PN", "LOG", "SEC", "ARCH", "KB")), i + 1);
            generatedConfluenceIds.add(confId);
            if (random.nextDouble() > 0.3) {
                potentialParentPageIds.add(confId);
            }

            LocalDate createdDate = generateRandomLocalDate(LocalDate.of(2023, 1, 1), LocalDate.of(2024, 5, 1));
            LocalDate modifiedDate = generateRandomLocalDate(createdDate, LocalDate.now());
             if (modifiedDate.isBefore(createdDate)) modifiedDate = createdDate;


            String parentPageId = "";
            if (!potentialParentPageIds.isEmpty() && random.nextDouble() > 0.6) {
                String tempParentId = randomChoice(potentialParentPageIds);
                if (!tempParentId.equals(confId)) { // Avoid self-parenting
                    parentPageId = tempParentId;
                }
            }

            rows.add(List.of(
                    confId, faker.company().catchPhrase() + " Documentation", randomChoice(USER_NAMES), randomChoice(USER_NAMES),
                    randomChoice(CONFLUENCE_SPACES), randomChoice(TEAM_NAMES),
                    String.join(" ", faker.lorem().sentences(2)),
                    generateSemicolonDelimitedList(generatedJiraIdsUnique, 4),
                    generateSemicolonDelimitedList(generatedCrIds, 2),
                    parentPageId,
                    createdDate.format(DATE_FORMATTER), modifiedDate.format(DATE_FORMATTER)
            ));
        }
        writeCsv(Paths.get(OUTPUT_FOLDER_NAME, filename).toString(), header, rows);
        System.out.printf("Generated %s with %d data rows.%n", filename, rows.size());
    }


    private static void writeCsv(String filePath, String[] header, List<List<String>> dataRows) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header))) {
            for (List<String> row : dataRows) {
                csvPrinter.printRecord(row);
            }
            csvPrinter.flush();
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + filePath);
            e.printStackTrace();
        }
    }

    // --- Main Generation Call ---
    public static void main(String[] args) {
        // -----vvv ALTER THESE VALUES TO CHANGE NUMBER OF ROWS vvv-----
        int numUserNamesToGenerate = 25; // Number of unique user names for USER_NAMES list
        int numUniqueCrs = 22;
        int numUniqueJiras = 72;
        int numConfluencePages = 23;
        int numCrCtasks = 18;
        int numJiraActivities = 35;
        // -----^^^ ALTER THESE VALUES TO CHANGE NUMBER OF ROWS ^^^-----

        System.out.println("Starting CSV data generation...");
        initializeUserNames(numUserNamesToGenerate); // Initialize user names first

        // Order is important for linking
        // Generate JIRAs first to get IDs for linking
        generateJiraIssuesDetailedCsv("JIRA_Issues_Detailed.csv", numUniqueJiras);
        
        // Confluence can link to JIRAs
        generateConfluencePagesDetailedCsv("Confluence_Pages_Detailed.csv", numConfluencePages);
        
        // CRs can link to JIRAs & Confluence
        generateCrMainCsv("CR_Main.csv", numUniqueCrs);
        
        // These depend on CRs or JIRAs
        generateCrCtasksCsv("CR_CTasks.csv", numCrCtasks);
        generateJiraActivitiesCsv("JIRA_Activities.csv", numJiraActivities);

        System.out.println("CSV data generation complete. Files saved in '" + OUTPUT_FOLDER_NAME + "' folder.");
    }
}