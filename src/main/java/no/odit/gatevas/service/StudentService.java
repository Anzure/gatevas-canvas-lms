package no.odit.gatevas.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.odit.gatevas.dao.StudentRepo;
import no.odit.gatevas.misc.GeneralUtil;
import no.odit.gatevas.misc.SheetGeneratorCSV;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.Student;

@Service
public class StudentService {

	private static final Logger log = LoggerFactory.getLogger(StudentService.class);

	@Autowired
	private StudentRepo studentRepo;

	@Autowired
	private PhoneService phoneService;

	@Autowired
	private SheetGeneratorCSV sheetGeneratorCSV;

	public Student createStudent(String email, String firstName, String lastName, int phoneNum) {

		// Return existing student (email)
		Optional<Student> existingEmail = getUserByEmail(email);
		if (existingEmail.isPresent()) {
			log.debug("EMAIL ALREADY EXIST -> " + existingEmail.get().toString());
			return existingEmail.get();
		}

		// Return existing student (name)
		Optional<Student> existingName = getUserByName(firstName, lastName);
		if (existingName.isPresent()) {
			log.debug("NAME ALREADY EXIST -> " + existingName.get().toString());
			return existingName.get();
		}

		// Create new student
		Phone phone = phoneService.createPhone(phoneNum);
		Student student = new Student();
		student.setEmail(email);
		student.setFirstName(firstName);
		student.setLastName(lastName);
		student.setTmpPassword(GeneralUtil.generatePassword());		
		student.setPhone(phone);
		student = studentRepo.saveAndFlush(student);
		log.debug("CREATED STUDENT -> " + student.toString());
		return student;
	}

	public boolean exportStudentsToCSV(List<Student> students, String path) {
		File file = new File(path);
		try {
			sheetGeneratorCSV.createCSVFile(file, students);
			return true;
		} catch (IOException e) {
			log.error("Failed to create CSV file.", e);
			return false;
		}
	}

	public Optional<Student> getUserByName(String firstName, String lastName) {
		return studentRepo.findByFirstNameAndLastName(firstName, lastName);
	}

	public Optional<Student> getUserByEmail(String email) {
		return studentRepo.findByEmail(email);
	}
}