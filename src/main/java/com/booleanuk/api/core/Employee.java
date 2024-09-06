package com.booleanuk.api.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class Employee {
	private int id; //pk
	private String name;
	private String jobName;
	private String salaryGrade;
	private String department;
}
