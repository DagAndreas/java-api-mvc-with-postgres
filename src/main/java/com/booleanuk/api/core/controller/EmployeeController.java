package com.booleanuk.api.core.controller;

import com.booleanuk.api.core.view.EmployeeRepository;
import com.booleanuk.api.core.model.Employee;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
	EmployeeRepository repo;

	public EmployeeController() throws SQLException {
		this.repo = new EmployeeRepository();
	}

	//get all
	@GetMapping
	public List<Employee> getAll() throws SQLException  {
		return repo.getAll();
	}

	// get by id
	@GetMapping("/{id}")
	public Employee getOne(@PathVariable(name = "id") long id) throws SQLException {
		try{
			Employee employee = repo.get(id);
		if (employee == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
		return employee;

		} catch (Exception e){
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Couldn't find by id");		}
	}


	static void isValidEmployee(Employee employee){
		if(employee.getId() == -1 ||
				employee.getName() == null || employee.getName().isEmpty() ||
				employee.getDepartment() == null || employee.getDepartment().isEmpty() ||
				employee.getJobName() == null || employee.getJobName().isEmpty() ||
				employee.getSalaryGrade() == null || employee.getSalaryGrade().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid value from employee");
		}
	}

	// add new
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Employee create(@RequestBody Optional<Employee> emp) throws SQLException {
		if(emp.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emp is empty");
		}

		Employee newemp = emp.get();
		isValidEmployee(newemp); // throws

		Employee employee = repo.add(emp.get());
		if (employee == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create the specified Customer");
		}
		return employee;
	}

	// edit by id
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Employee update(@PathVariable (name = "id") long id, @RequestBody Optional<Employee> employee) throws SQLException {
		// check if exists by id
		try {
			repo.get(id);
		} catch (SQLException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

		// check for valid employee
		employee.ifPresent(EmployeeController::isValidEmployee);

		return repo.update(id, employee.get());
	}


	// delete by id
	@DeleteMapping("/{id}")
	public Employee delete(@PathVariable (name = "id") long id) throws SQLException {
		try{
			Employee toBeDeleted = repo.get(id);
			return repo.delete(id);
		} catch (SQLException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}

	}



}
