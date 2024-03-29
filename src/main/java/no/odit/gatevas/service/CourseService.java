package no.odit.gatevas.service;

import lombok.extern.slf4j.Slf4j;
import no.odit.gatevas.dao.CourseApplicationRepo;
import no.odit.gatevas.dao.CourseRepo;
import no.odit.gatevas.dao.CourseTypeRepo;
import no.odit.gatevas.misc.SheetImportCSV;
import no.odit.gatevas.model.*;
import no.odit.gatevas.type.ApplicationStatus;
import no.odit.gatevas.type.CanvasStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CourseService {

    @Autowired
    private CourseApplicationRepo courseApplicationRepo;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private CourseTypeRepo courseTypeRepo;

    @Autowired
    private SheetImportCSV sheetImportCSV;

    // Save changes for course application
    public void updateCourseApplication(CourseApplication courseApplication) {
        courseApplicationRepo.saveAndFlush(courseApplication);
    }

    // Get course applications by course
    public List<CourseApplication> getCourseApplications(Classroom course) {
        Set<RoomLink> enrollments = course.getEnrollments();
        List<CourseApplication> applications = getCourseTypeApplications(course.getType());
        List<Student> students = enrollments.stream().map(enrollment -> enrollment.getStudent()).collect(Collectors.toList());
        return applications.stream().filter(application ->
                        students.stream().anyMatch(student -> student.getId().equals(application.getStudent().getId())))
                .collect(Collectors.toList());
    }

    // Get course applications by type
    public List<CourseApplication> getCourseTypeApplications(CourseType courseType) {
        return courseApplicationRepo.findByCourse(courseType);
    }

    // Imports students from single course list
    public Optional<Set<Student>> importStudents(File csvFile, Classroom course, boolean useComma) {
        try {
            Set<Student> students = sheetImportCSV.processSheet(csvFile, course.getType(), "Cp1252", useComma);
            return Optional.of(students);
        } catch (Exception ex) {
            log.error("Failed to import students.", ex);
            return Optional.empty();
        }
    }

    // Saves changes for course to storage
    public void saveChanges(Classroom course) {
        courseRepo.saveAndFlush(course);
    }

    // Creates a new course in storage
    public Classroom addCourse(Classroom course) {
        course.setCanvasStatus(CanvasStatus.UNKNOWN);
        course = courseRepo.saveAndFlush(course);
        log.info("CREATE COURSE -> " + course);
        return course;
    }

    // Deletes a course from storage
    public void removeCourse(Classroom course) {
        log.info("DELETE COURSE -> " + course.toString());
        courseRepo.delete(course);
    }

    // Find all courses
    public List<Classroom> getAllCourses() {
        return courseRepo.findAll();
    }

    // Gets course from storage
    public Optional<Classroom> getCourse(String name) {
        return getAllCourses().stream()
                .filter(course -> course.getShortName().equalsIgnoreCase(name) || course.getLongName().equalsIgnoreCase(name))
                .findFirst();
    }

    // Gets course type from storage
    public Optional<CourseType> getCourseType(String name) {
//        return courseTypeRepo.findAll().stream().findFirst()
//                .filter(courseType -> courseType.getShortName().contains("*") && name.startsWith(courseType.getShortName().replaceAll("\\*", ""))
//                        || courseType.getLongName().contains("*") && name.startsWith(courseType.getLongName().replaceAll("\\*", ""))
//                        || courseType.getAliasName().contains("*") && name.startsWith(courseType.getAliasName().replaceAll("\\*", "")))
//                .or(() ->
//                        Optional.ofNullable(courseTypeRepo.findByShortName(name)
//                                .orElse(courseTypeRepo.findByLongName(name)
//                                        .orElse(courseTypeRepo.findByAliasName(name).orElse(null)))
//                        ));
        String alternative = name.contains(" - ") ? name.split(" - ")[0] : name;
        return Optional.ofNullable(courseTypeRepo.findByShortName(name)
                .orElse(courseTypeRepo.findByLongName(alternative)
                        .orElse(courseTypeRepo.findByAliasName(name).orElse(null))));
    }

    public List<CourseType> getCourseTypes() {
        return courseTypeRepo.findAll();
    }

    public CourseApplication createCourseApplication(Student student, CourseType courseType, Boolean uptake) {

        CourseApplication apply = new CourseApplication();
        Optional<CourseApplication> optCourseApply = courseApplicationRepo.findByStudentAndCourse(student, courseType);
        if (optCourseApply.isPresent()) {
            apply = optCourseApply.get();
        } else {
            apply.setCourse(courseType);
            apply.setStudent(student);
            apply.setUptake(true);
            apply.setStatus(ApplicationStatus.WAITLIST);
        }

        // Update status
        if (apply.getStatus() == ApplicationStatus.WAITLIST) {
            List<Classroom> courses = courseRepo.findByType(courseType);
            for (Classroom course : courses) {
                Optional<RoomLink> optEnroll = enrollmentService.getEnrollment(student, course);
                if (optEnroll.isPresent()) {
                    apply.setUptake(uptake);
                    apply.setStatus(ApplicationStatus.ACCEPTED);
                }
            }
        }

        return courseApplicationRepo.findById(courseApplicationRepo.saveAndFlush(apply).getId()).get();
    }

}