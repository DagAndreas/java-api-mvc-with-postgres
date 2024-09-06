package com.booleanuk.api.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Employee {
	private long id; //pk
	private String name;
	private String jobName;
	private String salaryGrade;
	private String department;
}
