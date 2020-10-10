package no.odit.gatevas.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import no.odit.gatevas.dao.StudentRepo;
import no.odit.gatevas.misc.GeneralUtil;
import no.odit.gatevas.model.Phone;
import no.odit.gatevas.model.Student;

@Service
public class StudentService {

	private static final Logger log = LoggerFactory.getLogger(StudentService.class);

	@Autowired
	private StudentRepo studentRepo;
	
	@Autowired
	private PhoneService phoneService;

	public Student createStudent(String email, String firstName, String lastName, int phoneNum) {
		Phone phone = phoneService.createPhone(phoneNum);
		Student student = new Student();
		student.setEmail(email);
		student.setFirstName(firstName);
		student.setLastName(lastName);
		student.setTmpPassword(GeneralUtil.generatePassword());		
		student.setPhone(phone);
		return studentRepo.saveAndFlush(student);
	}

	public Optional<Student> getUserByName(String firstName, String lastName) {
		return studentRepo.findByFirstNameAndLastName(firstName, lastName);
	}

	public Optional<Student> getUserByEmail(String email) {
		return studentRepo.findByEmail(email);
	}
}