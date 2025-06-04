package com.mycompany.datagenerator;

// Getters and setters are crucial for Jackson to map YAML properties.
// Ensure property names in YAML match field names here (or use @JsonProperty).

public class AppConfig {
    private Output output;
    private Generation generation;

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

    public static class Output {
        private String folderName;

        public String getFolderName() {
            return folderName;
        }

        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }
    }

    public static class Generation {
        private int userNamesCount;
        private int uniqueCrs;
        private int uniqueJiras;
        private int confluencePages;
        private int crCtasks;
        private int jiraActivities;

        public int getUserNamesCount() {
            return userNamesCount;
        }

        public void setUserNamesCount(int userNamesCount) {
            this.userNamesCount = userNamesCount;
        }

        public int getUniqueCrs() {
            return uniqueCrs;
        }

        public void setUniqueCrs(int uniqueCrs) {
            this.uniqueCrs = uniqueCrs;
        }

        public int getUniqueJiras() {
            return uniqueJiras;
        }

        public void setUniqueJiras(int uniqueJiras) {
            this.uniqueJiras = uniqueJiras;
        }

        public int getConfluencePages() {
            return confluencePages;
        }

        public void setConfluencePages(int confluencePages) {
            this.confluencePages = confluencePages;
        }

        public int getCrCtasks() {
            return crCtasks;
        }

        public void setCrCtasks(int crCtasks) {
            this.crCtasks = crCtasks;
        }

        public int getJiraActivities() {
            return jiraActivities;
        }

        public void setJiraActivities(int jiraActivities) {
            this.jiraActivities = jiraActivities;
        }
    }
}