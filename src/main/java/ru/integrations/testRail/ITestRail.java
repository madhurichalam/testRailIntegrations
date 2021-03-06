package ru.integrations.testRail;

import com.codepine.api.testrail.TestRail;
import com.codepine.api.testrail.model.*;
import ru.integrations.testRail.config.ProjectConfig;

import java.util.ArrayList;
import java.util.List;


public class ITestRail {
    private TestRail testRail;
    private Project PROJECT = null;
    private Section SECTION = null;
    private List<Section> SECTIONS;
    private List<Integer> CASES;
    private Milestone MILESTONE = null;
    private Run RUN = null;

    public ITestRail() {
    }

    public ITestRail(String nameProject, List<String> nameSection, ProjectConfig config) {
        testRail = TestRail.builder(config.hostTR(), config.loginTR(), config.passTR()).build();
        searchProjectByName(nameProject);
        searchSectionByName(nameSection);

    }

    public ITestRail(String nameProject, ProjectConfig config) {
        testRail = TestRail.builder(config.hostTR(), config.loginTR(), config.passTR()).build();
        searchProjectByName(nameProject);
        searchSectionByName(config.section());
    }

    public void searchMilestones(String name) {
        List<Milestone> milestones = testRail.milestones().list(PROJECT.getId()).execute();
        for (Milestone m : milestones) {
            if (!m.isCompleted()) {
                if (m.getName().equals(name)) {
                    MILESTONE = m;
                }
            }
        }
    }

    public void createMilestones(String name) {
        Milestone m = new Milestone();
        m.setName(name);
        m.setDescription("Test automation");
        MILESTONE = testRail.milestones().add(PROJECT.getId(), m).execute();
    }

    public void searchTestRun(String name) {
        List<Run> runs = testRail.runs().list(PROJECT.getId()).execute();
        for (Run r : runs) {
            if (r.getName().equals(name)) {
                RUN = r;
            }
        }
    }

    public void createTestRun(String name, String description) {
        Run run = new Run();
        run.setName(name);
        run.setDescription(description);
        if (!(SECTION == null)) {
            run.setIncludeAll(false);
            run.setCaseIds(CASES);
        }
        run.setMilestoneId(MILESTONE.getId());
        RUN = testRail.runs().add(PROJECT.getId(), run).execute();
    }

    private List<Case> getCaseList() {
        return testRail.cases().list(PROJECT.getId(), testRail.caseFields().list().execute()).execute();
    }

    public boolean setCaseStatus(String title, int status, String comment) {
        Result result = new Result();
        result.setComment(comment);
        result.setStatusId(status);
        boolean find = false;
        for (Case c : getCaseList()) {
            if (c.getTitle().equals(title)) {
                testRail.results().addForCase(
                        RUN.getId(),
                        c.getId(),
                        result,
                        testRail.resultFields().list().execute()).execute();
                find = true;
            }
        }
        return find;
    }

    public void closeRun() {
        testRail.runs().close(RUN.getId()).execute();
    }

    public void closeMilestone() {
        testRail.milestones().update(MILESTONE.setCompleted(true)).execute();
    }

    public void searchProjectByName(String name) {
        List<Project> projects = testRail.projects().list().execute();
        for (Project project : projects) {
            if (project.getName().equals(name)) {
                PROJECT = project;
            }
        }
    }

    public void searchSectionByName(String name) {
        List<Section> sections = testRail.sections().list(PROJECT.getId()).execute();
        for (Section section : sections) {
            if (section.getName().equals(name)) {
                SECTION = section;
            }
        }
        searchCaseBySection(SECTION.getId());
    }

    public void searchSectionByName(List<String> array) {
        List<Section> sections = new ArrayList<>();
        for (Section section : testRail.sections().list(PROJECT.getId()).execute()) {
            for (int i = 0; i < array.size(); i++) {
                if (section.getName().equals(array.get(i))) {
                    sections.add(section);
                }
            }
        }
        SECTIONS = sections;
        searchCaseBySection();
    }

    private void searchCaseBySection() {
        List<Integer> cases = new ArrayList<>();
        for (int i = 0; i < SECTIONS.size(); i++) {
            for (Case c : getCaseList()) {
                if (c.getSectionId() == SECTIONS.get(i).getId()) {
                    cases.add(c.getId());
                }
            }
        }
        CASES = cases;
    }

    private void searchCaseBySection(int sectionId) {
        List<Integer> cases = new ArrayList<>();
        for (Case c : getCaseList()) {
            if (c.getSectionId() == sectionId) {
                cases.add(c.getId());
            }
        }
        CASES = cases;
    }
}
