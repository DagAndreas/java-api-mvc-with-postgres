package com.booleanuk.api.core;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.List;

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
		Employee employee = repo.get(id);
//		if (employee == null) {
//			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
//		}
		return employee;
	}


	// add new
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Employee create(@RequestBody Employee emp) throws SQLException {
		Employee employee = repo.add(emp);
		if (employee == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create the specified Customer");
		}
		return employee;
	}

	// edit by id
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.CREATED)
	public Employee update(@PathVariable (name = "id") long id, @RequestBody Employee employee) throws SQLException {
		Employee toBeUpdated = repo.get(id);
		if (toBeUpdated == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
		return this.repo.update(id, employee);
	}


	// delete by id
	@DeleteMapping("/{id}")
	public Employee delete(@PathVariable (name = "id") long id) throws SQLException {
		Employee toBeDeleted = repo.get(id);
		if (toBeDeleted == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
		return repo.delete(id);
	}



}
